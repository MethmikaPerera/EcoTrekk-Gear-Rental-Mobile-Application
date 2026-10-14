package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.request.AdminOrderStatusUpdateRequestDTO;
import com.akigen.ecotrekk_backend.dto.request.AdminOrderPaymentStatusUpdateRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminOrderCustomerDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminOrderDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminOrderItemDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminOrderPageResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.AdminOrderStatusHistoryDTO;
import com.akigen.ecotrekk_backend.entity.Invoice;
import com.akigen.ecotrekk_backend.entity.InvoiceItem;
import com.akigen.ecotrekk_backend.enums.InvoiceStatus;
import com.akigen.ecotrekk_backend.enums.PaymentMethod;
import com.akigen.ecotrekk_backend.enums.PaymentStatus;
import com.akigen.ecotrekk_backend.repository.InventoryRepository;
import com.akigen.ecotrekk_backend.repository.InvoiceItemRepository;
import com.akigen.ecotrekk_backend.repository.InvoiceRepository;
import com.akigen.ecotrekk_backend.service.AdminOrderService;
import com.akigen.ecotrekk_backend.service.NotificationService;
import com.akigen.ecotrekk_backend.service.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminOrderServiceImpl implements AdminOrderService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InventoryRepository inventoryRepository;
    private final RealtimeEventPublisher realtimeEventPublisher;
    private final NotificationService notificationService;

    @Override
    public AdminOrderPageResponseDTO getOrders(int page, int size, String status, String search, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        InvoiceStatus invoiceStatus = parseListStatus(status);
        String searchTerm = normalizeSearch(search);

        Page<Invoice> invoicePage = invoiceRepository.searchAdminOrders(invoiceStatus, searchTerm, startDate, endDate, pageable);

        List<AdminOrderDTO> content = invoicePage.getContent().stream()
                .map(this::mapToOrderSummary)
                .toList();

        return AdminOrderPageResponseDTO.builder()
                .content(content)
                .totalElements(invoicePage.getTotalElements())
                .totalPages(invoicePage.getTotalPages())
                .size(invoicePage.getSize())
                .number(invoicePage.getNumber())
                .build();
    }

    @Override
    public AdminOrderDTO getOrderById(String id) {
        Invoice invoice = findByExternalId(id);
        return mapToOrderWithDetails(invoice);
    }

    @Override
    @Transactional
    public void updateOrderStatus(String id, AdminOrderStatusUpdateRequestDTO request) {
        Invoice invoice = findByExternalId(id);
        InvoiceStatus newStatus = parseUpdateStatus(request.getStatus());
        InvoiceStatus oldStatus = invoice.getStatus();

        if (newStatus == InvoiceStatus.PICKED_UP && invoice.getPaymentStatus() != PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order payment must be marked as PAID before pickup");
        }

        handleInventoryTransitions(invoice, newStatus);

        invoice.setStatus(newStatus);
        invoiceRepository.save(invoice);

        log.info("Order {} status updated from {} to {}", id, oldStatus, newStatus);
        
        // Send notification for order approval
        if (newStatus == InvoiceStatus.APPROVED) {
            try {
                notificationService.sendOrderApprovalNotification(invoice);
                log.info("Approval notification sent for order: {}", id);
            } catch (Exception e) {
                log.error("Failed to send approval notification for order: {}", id, e);
                // Don't fail the order update if notification fails
            }
        } else {
            // Send general status update notification
            try {
                notificationService.sendOrderStatusNotification(invoice, newStatus.name());
                log.info("Status update notification sent for order: {}", id);
            } catch (Exception e) {
                log.error("Failed to send status notification for order: {}", id, e);
                // Don't fail the order update if notification fails
            }
        }
        
        // Publish real-time event
        AdminOrderDTO updatedOrder = mapToOrderWithDetails(invoice);
        realtimeEventPublisher.publishOrderEvent("ORDER_UPDATED", updatedOrder);
    }

    private void handleInventoryTransitions(Invoice invoice, InvoiceStatus newStatus) {
        // Return: Add back to inventory when completed or cancelled
        if ((newStatus == InvoiceStatus.COMPLETED || newStatus == InvoiceStatus.CANCELLED) 
                && invoice.isInventoryAllocated()) {
            releaseInventory(invoice);
        }
    }



    private void releaseInventory(Invoice invoice) {
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
        for (InvoiceItem item : items) {
            com.akigen.ecotrekk_backend.entity.Inventory inventory = inventoryRepository.findByGearId(item.getGear().getId())
                    .stream().findFirst().orElse(null);

            if (inventory != null) {
                int newQty = Math.min(inventory.getTotalQty(), inventory.getAvailableQty() + item.getQty());
                inventory.setAvailableQty(newQty);
                inventoryRepository.save(inventory);
                log.info("Released {} units of gear {} from invoice {}", item.getQty(), item.getGear().getId(), invoice.getInvoiceCode());
            }
        }
        invoice.setInventoryAllocated(false);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(String id, AdminOrderPaymentStatusUpdateRequestDTO request) {
        Invoice invoice = findByExternalId(id);

        if (invoice.getPaymentMethod() != PaymentMethod.PAY_AT_HUB) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only Pay at Hub orders can be updated manually");
        }

        PaymentStatus oldStatus = invoice.getPaymentStatus();
        PaymentStatus newStatus = Boolean.TRUE.equals(request.getPaid()) ? PaymentStatus.PAID : PaymentStatus.UNPAID;

        if (oldStatus == newStatus) {
            return;
        }

        invoice.setPaymentStatus(newStatus);
        invoiceRepository.save(invoice);

        log.info("Order {} payment status updated from {} to {}", id, oldStatus, newStatus);

        AdminOrderDTO updatedOrder = mapToOrderWithDetails(invoice);
        realtimeEventPublisher.publishOrderEvent("ORDER_UPDATED", updatedOrder);
    }

    private Invoice findByExternalId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order id is required");
        }

        if (id.chars().allMatch(Character::isDigit)) {
            Long numericId = Long.parseLong(id);
            return invoiceRepository.findById(numericId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        }

        return invoiceRepository.findByInvoiceCode(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private AdminOrderDTO mapToOrderSummary(Invoice invoice) {
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
        return mapInvoice(invoice, items, false);
    }

    private AdminOrderDTO mapToOrderWithDetails(Invoice invoice) {
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
        return mapInvoice(invoice, items, true);
    }

    private AdminOrderDTO mapInvoice(Invoice invoice, List<InvoiceItem> items, boolean includeStatusHistory) {
        LocalDate startDate = items.stream()
                .map(InvoiceItem::getStartDate)
                .min(Comparator.naturalOrder())
                .orElse(null);

        LocalDate endDate = items.stream()
                .map(InvoiceItem::getEndDate)
                .max(Comparator.naturalOrder())
                .orElse(null);

        List<AdminOrderItemDTO> orderItems = items.stream()
                .map(item -> {
                    BigDecimal depositAmount = item.getDepositAmount() != null
                            ? item.getDepositAmount()
                            : (item.getGear() != null && item.getGear().getDepositAmount() != null
                                    ? item.getGear().getDepositAmount()
                                    : BigDecimal.ZERO);
                    BigDecimal depositTotal = item.getDepositTotal() != null && item.getDepositTotal().compareTo(BigDecimal.ZERO) > 0
                            ? item.getDepositTotal()
                            : depositAmount.multiply(BigDecimal.valueOf(item.getQty()));

                    return AdminOrderItemDTO.builder()
                            .id(String.valueOf(item.getId()))
                            .gearId(String.valueOf(item.getGear().getId()))
                            .gearName(item.getGear().getName())
                            .imageUrl(null)
                            .quantity(item.getQty())
                            .pricePerDay(item.getDailyRentPrice())
                            .depositAmount(depositAmount)
                            .depositTotal(depositTotal)
                            .refundableDeposit(depositTotal)
                            .totalPrice(item.getLineTotal())
                            .build();
                })
                .toList();

        BigDecimal itemsSubtotal = items.stream()
                .map(InvoiceItem::getLineTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal itemsDepositTotal = orderItems.stream()
                .map(AdminOrderItemDTO::getDepositTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotal = invoice.getSubtotal() != null && invoice.getSubtotal().compareTo(BigDecimal.ZERO) > 0
                ? invoice.getSubtotal()
                : itemsSubtotal;

        BigDecimal depositTotal = invoice.getDepositTotal() != null && invoice.getDepositTotal().compareTo(BigDecimal.ZERO) > 0
                ? invoice.getDepositTotal()
                : itemsDepositTotal;

        if (depositTotal.compareTo(BigDecimal.ZERO) == 0 && invoice.getTotalAmount() != null && invoice.getTotalAmount().compareTo(subtotal) > 0) {
            depositTotal = invoice.getTotalAmount().subtract(subtotal);
        }

        BigDecimal calculatedTotal = subtotal.add(depositTotal);
        BigDecimal totalAmount = invoice.getTotalAmount() != null && invoice.getTotalAmount().compareTo(calculatedTotal) >= 0
                ? invoice.getTotalAmount()
                : calculatedTotal;

        return AdminOrderDTO.builder()
                .id(invoice.getInvoiceCode())
                .customer(AdminOrderCustomerDTO.builder()
                        .id(String.valueOf(invoice.getCustomer().getId()))
                        .name(invoice.getCustomer().getFullName())
                        .email(invoice.getCustomer().getEmail())
                        .phone(invoice.getCustomer().getPhone())
                        .build())
                .hubId(String.valueOf(invoice.getHub().getId()))
                .hubName(invoice.getHub().getName())
                .startDate(startDate)
                .endDate(endDate)
                .status(toFrontendStatus(invoice.getStatus()))
                .paymentMethod(invoice.getPaymentMethod())
                .paymentStatus(invoice.getPaymentStatus())
                .items(orderItems)
                .subtotal(subtotal)
                .depositTotal(depositTotal)
                .refundableDeposit(depositTotal)
                .totalAmount(totalAmount)
                .createdAt(invoice.getCreatedAt())
                .statusHistory(includeStatusHistory ? buildHistory(invoice.getStatus(), invoice.getCreatedAt(), invoice.getUpdatedAt()) : List.of())
                .build();
    }

    private List<AdminOrderStatusHistoryDTO> buildHistory(InvoiceStatus status, Instant createdAt, Instant updatedAt) {
        List<AdminOrderStatusHistoryDTO> history = new ArrayList<>();

        history.add(AdminOrderStatusHistoryDTO.builder()
                .status("PENDING")
                .timestamp(createdAt)
                .note("Order created")
                .build());

        String currentStatus = toFrontendStatus(status);
        if (!"PENDING".equals(currentStatus) && updatedAt != null) {
            history.add(AdminOrderStatusHistoryDTO.builder()
                    .status(currentStatus)
                    .timestamp(updatedAt)
                    .note("Status updated by admin")
                    .build());
        }

        return history;
    }

    private String normalizeSearch(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }
        return search.trim();
    }

    private InvoiceStatus parseListStatus(String status) {
        if (status == null || status.trim().isEmpty() || "ALL".equalsIgnoreCase(status)) {
            return null;
        }

        String normalized = status.trim().toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "PENDING" -> InvoiceStatus.PENDING;
            case "CONFIRMED" -> InvoiceStatus.APPROVED;
            case "PICKED_UP" -> InvoiceStatus.PICKED_UP;
            case "RETURNED" -> InvoiceStatus.COMPLETED;
            case "CANCELLED" -> InvoiceStatus.CANCELLED;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status filter");
        };
    }

    private InvoiceStatus parseUpdateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        String normalized = status.trim().toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "PENDING" -> InvoiceStatus.PENDING;
            case "CONFIRMED" -> InvoiceStatus.APPROVED;
            case "PICKED_UP" -> InvoiceStatus.PICKED_UP;
            case "RETURNED" -> InvoiceStatus.COMPLETED;
            case "CANCELLED" -> InvoiceStatus.CANCELLED;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
        };
    }

    private String toFrontendStatus(InvoiceStatus status) {
        if (status == null) {
            return "PENDING";
        }

        return switch (status) {
            case PENDING -> "PENDING";
            case APPROVED, READY -> "CONFIRMED";
            case PICKED_UP, DELIVERED -> "PICKED_UP";
            case COMPLETED -> "RETURNED";
            case CANCELLED -> "CANCELLED";
        };
    }
}
