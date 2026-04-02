package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.dto.response.CustomerOrderHistoryResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.CustomerOrderItemResponseDTO;
import com.akigen.ecotrekk_backend.entity.Gear;
import com.akigen.ecotrekk_backend.entity.GearImage;
import com.akigen.ecotrekk_backend.entity.Invoice;
import com.akigen.ecotrekk_backend.entity.InvoiceItem;
import com.akigen.ecotrekk_backend.repository.GearImageRepository;
import com.akigen.ecotrekk_backend.repository.InvoiceItemRepository;
import com.akigen.ecotrekk_backend.repository.InvoiceRepository;
import com.akigen.ecotrekk_backend.service.CustomerOrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerOrderHistoryServiceImpl implements CustomerOrderHistoryService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final GearImageRepository gearImageRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerOrderHistoryResponseDTO> getCustomerOrderHistory(Long customerId) {
        List<Invoice> invoices = invoiceRepository.findByCustomerId(customerId);
        if (invoices.isEmpty()) {
            return List.of();
        }

        List<Long> invoiceIds = invoices.stream().map(Invoice::getId).toList();
        Map<Long, List<InvoiceItem>> invoiceItemsMap = invoiceItemRepository.findByInvoiceIdIn(invoiceIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getInvoice().getId()));

        return invoices.stream()
                .sorted(Comparator.comparing(Invoice::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(invoice -> {
                    List<InvoiceItem> items = invoiceItemsMap.getOrDefault(invoice.getId(), List.of());

                    String startDate = items.stream()
                            .map(InvoiceItem::getStartDate)
                            .filter(java.util.Objects::nonNull)
                            .min(LocalDate::compareTo)
                            .map(LocalDate::toString)
                            .orElse(null);

                    String endDate = items.stream()
                            .map(InvoiceItem::getEndDate)
                            .filter(java.util.Objects::nonNull)
                            .max(LocalDate::compareTo)
                            .map(LocalDate::toString)
                            .orElse(null);

                    List<CustomerOrderItemResponseDTO> orderItems = items.stream()
                            .map(item -> {
                                Gear gear = item.getGear();
                                return CustomerOrderItemResponseDTO.builder()
                                        .id(item.getId())
                                        .gearId(gear != null ? gear.getId() : null)
                                        .gearName(gear != null ? gear.getName() : null)
                                        .gearCategory(gear != null && gear.getCategory() != null ? gear.getCategory().getName() : "Other")
                                        .imageUrl(gear != null ? resolveGearImageUrl(gear) : null)
                                        .quantity(item.getQty())
                                    .rentPrice(item.getDailyRentPrice() != null ? item.getDailyRentPrice().doubleValue() : 0.0)
                                    .refundableDeposit(item.getDepositAmount() != null ? item.getDepositAmount().doubleValue() : 0.0)
                                        .build();
                            })
                            .toList();

                    return CustomerOrderHistoryResponseDTO.builder()
                            .id(invoice.getId())
                            .orderNumber(invoice.getInvoiceCode())
                            .customerId(invoice.getCustomer() != null ? invoice.getCustomer().getId() : null)
                            .startDate(startDate)
                            .endDate(endDate)
                            .pickupHubName(invoice.getHub() != null ? invoice.getHub().getName() : null)
                                .pickupHubLatitude(invoice.getHub() != null && invoice.getHub().getLatitude() != null
                                    ? invoice.getHub().getLatitude().doubleValue() : null)
                                .pickupHubLongitude(invoice.getHub() != null && invoice.getHub().getLongitude() != null
                                    ? invoice.getHub().getLongitude().doubleValue() : null)
                            .subtotal(invoice.getSubtotal() != null ? invoice.getSubtotal().doubleValue() : 0.0)
                            .refundableDeposit(invoice.getDepositTotal() != null ? invoice.getDepositTotal().doubleValue() : 0.0)
                            .totalAmount(invoice.getTotalAmount() != null ? invoice.getTotalAmount().doubleValue() : 0.0)
                            .status(invoice.getStatus() != null ? invoice.getStatus().name() : null)
                            .createdAt(invoice.getCreatedAt() != null ? invoice.getCreatedAt().toEpochMilli() : null)
                            .items(orderItems)
                            .build();
                })
                .toList();
    }

    private String resolveGearImageUrl(Gear gear) {
        List<GearImage> primaryImages = gearImageRepository.findByGearIdAndPrimaryTrue(gear.getId());
        if (!primaryImages.isEmpty()) {
            return primaryImages.get(0).getImageUrl();
        }

        List<GearImage> orderedImages = gearImageRepository.findByGearIdOrderBySortOrderAsc(gear.getId());
        if (!orderedImages.isEmpty()) {
            return orderedImages.get(0).getImageUrl();
        }

        return gear.getCategory() != null ? gear.getCategory().getImageUrl() : null;
    }
}
