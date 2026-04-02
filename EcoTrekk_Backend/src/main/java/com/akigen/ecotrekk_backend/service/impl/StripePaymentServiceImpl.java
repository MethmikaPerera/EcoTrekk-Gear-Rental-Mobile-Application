package com.akigen.ecotrekk_backend.service.impl;

import com.akigen.ecotrekk_backend.config.StripeProperties;
import com.akigen.ecotrekk_backend.dto.request.CreateOrderRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.CashOrderResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.StripePaymentIntentResponseDTO;
import com.akigen.ecotrekk_backend.entity.Cart;
import com.akigen.ecotrekk_backend.entity.CartItem;
import com.akigen.ecotrekk_backend.entity.Invoice;
import com.akigen.ecotrekk_backend.entity.InvoiceItem;
import com.akigen.ecotrekk_backend.enums.InvoiceStatus;
import com.akigen.ecotrekk_backend.enums.PaymentMethod;
import com.akigen.ecotrekk_backend.enums.PaymentStatus;
import com.akigen.ecotrekk_backend.repository.CartItemRepository;
import com.akigen.ecotrekk_backend.repository.CartRepository;
import com.akigen.ecotrekk_backend.repository.HubRepository;
import com.akigen.ecotrekk_backend.repository.InventoryRepository;
import com.akigen.ecotrekk_backend.repository.InvoiceItemRepository;
import com.akigen.ecotrekk_backend.repository.InvoiceRepository;
import com.akigen.ecotrekk_backend.service.StripePaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StripePaymentServiceImpl implements StripePaymentService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final HubRepository hubRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InventoryRepository inventoryRepository;
    private final StripeProperties stripeProperties;

    @Override
    @Transactional(readOnly = true)
    public StripePaymentIntentResponseDTO createPaymentIntentForCustomer(Long customerId) {
        validateStripeConfig();

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cart not found"));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Cart is empty");
        }

        int rentalDays = calculateRentalDays(cart.getStartDate(), cart.getEndDate());
        long amount = calculatePayableAmount(items, rentalDays);
        if (amount <= 0L) {
            throw new ResponseStatusException(BAD_REQUEST, "Calculated payment amount must be greater than zero");
        }

        Stripe.apiKey = stripeProperties.getSecretKey();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(stripeProperties.getCurrency().toLowerCase())
                .setDescription("EcoTrekk checkout for customer " + customerId)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .putMetadata("customerId", String.valueOf(customerId))
                .putMetadata("cartId", String.valueOf(cart.getId()))
                .build();

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return StripePaymentIntentResponseDTO.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .publishableKey(stripeProperties.getPublishableKey())
                    .amount(paymentIntent.getAmount())
                    .currency(paymentIntent.getCurrency())
                    .build();
        } catch (StripeException ex) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to create Stripe PaymentIntent: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public CashOrderResponseDTO createCashOrderForCustomer(Long customerId, CreateOrderRequestDTO request) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cart not found"));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Cart is empty");
        }

        // Extract dates from request or fallback to cart dates
        LocalDate startDate = extractLocalDate(request.getStartDateUtc());
        if (startDate == null) {
            startDate = cart.getStartDate() != null ? cart.getStartDate() : LocalDate.now();
        }
        
        LocalDate endDate = extractLocalDate(request.getEndDateUtc());
        if (endDate == null) {
            endDate = cart.getEndDate() != null ? cart.getEndDate() : startDate;
        }
        
        int rentalDays = calculateRentalDays(startDate, endDate);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal depositTotal = BigDecimal.ZERO;

        for (CartItem item : items) {
            com.akigen.ecotrekk_backend.entity.Gear gear = item.getGear();
            if (gear == null) {
                continue;
            }

            BigDecimal dailyPrice = gear.getDailyRentPrice() != null ? gear.getDailyRentPrice() : BigDecimal.ZERO;
            BigDecimal deposit = gear.getDepositAmount() != null ? gear.getDepositAmount() : BigDecimal.ZERO;
            int qty = Math.max(1, item.getQty());

            subtotal = subtotal.add(dailyPrice.multiply(BigDecimal.valueOf(qty)).multiply(BigDecimal.valueOf(rentalDays)));
            depositTotal = depositTotal.add(deposit.multiply(BigDecimal.valueOf(qty)));
        }

        BigDecimal totalAmount = subtotal.add(depositTotal);
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Calculated order amount must be greater than zero");
        }

        // Find hub by collection hub name, fallback to first item's hub if not found
        String collectionHubName = request.getCollectionHubName();
        com.akigen.ecotrekk_backend.entity.Hub selectedHub = items.get(0).getHub();
        if (collectionHubName != null && !collectionHubName.trim().isEmpty()) {
            var hubOptional = hubRepository.findByName(collectionHubName.trim());
            if (hubOptional.isPresent()) {
                selectedHub = hubOptional.get();
            }
        }

        Invoice invoice = Invoice.builder()
                .invoiceCode(generateInvoiceCode())
                .customer(cart.getCustomer())
                .hub(selectedHub)
                .status(InvoiceStatus.PENDING)
                .paymentMethod(PaymentMethod.PAY_AT_HUB)
                .paymentStatus(PaymentStatus.UNPAID)
                .subtotal(subtotal)
                .depositTotal(depositTotal)
                .totalAmount(totalAmount)
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        for (CartItem item : items) {
            com.akigen.ecotrekk_backend.entity.Gear gear = item.getGear();
            if (gear == null) {
                continue;
            }

            BigDecimal dailyPrice = gear.getDailyRentPrice() != null ? gear.getDailyRentPrice() : BigDecimal.ZERO;
            BigDecimal deposit = gear.getDepositAmount() != null ? gear.getDepositAmount() : BigDecimal.ZERO;
            int qty = Math.max(1, item.getQty());

            BigDecimal lineTotal = dailyPrice.multiply(BigDecimal.valueOf(qty)).multiply(BigDecimal.valueOf(rentalDays));
            BigDecimal lineDeposit = deposit.multiply(BigDecimal.valueOf(qty));

            InvoiceItem invoiceItem = InvoiceItem.builder()
                    .invoice(savedInvoice)
                    .gear(gear)
                    .qty(qty)
                    .startDate(startDate)
                    .endDate(endDate)
                    .dailyRentPrice(dailyPrice)
                    .depositAmount(deposit)
                    .lineTotal(lineTotal)
                    .depositTotal(lineDeposit)
                    .build();

            invoiceItemRepository.save(invoiceItem);
        }

        allocateInventoryForInvoice(savedInvoice, items);

        cartItemRepository.deleteByCartId(cart.getId());

        return CashOrderResponseDTO.builder()
                .invoiceId(savedInvoice.getId())
                .invoiceCode(savedInvoice.getInvoiceCode())
                .status(savedInvoice.getStatus())
                .paymentMethod(savedInvoice.getPaymentMethod())
                .paymentStatus(savedInvoice.getPaymentStatus())
                .subtotal(savedInvoice.getSubtotal())
                .depositTotal(savedInvoice.getDepositTotal())
                .totalAmount(savedInvoice.getTotalAmount())
                .build();
    }

    @Override
    @Transactional
    public CashOrderResponseDTO createCardOrderForCustomer(Long customerId, CreateOrderRequestDTO request) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cart not found"));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Cart is empty");
        }

        // Extract dates from request or fallback to cart dates
        LocalDate startDate = extractLocalDate(request.getStartDateUtc());
        if (startDate == null) {
            startDate = cart.getStartDate() != null ? cart.getStartDate() : LocalDate.now();
        }
        
        LocalDate endDate = extractLocalDate(request.getEndDateUtc());
        if (endDate == null) {
            endDate = cart.getEndDate() != null ? cart.getEndDate() : startDate;
        }
        
        int rentalDays = calculateRentalDays(startDate, endDate);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal depositTotal = BigDecimal.ZERO;

        for (CartItem item : items) {
            com.akigen.ecotrekk_backend.entity.Gear gear = item.getGear();
            if (gear == null) {
                continue;
            }

            BigDecimal dailyPrice = gear.getDailyRentPrice() != null ? gear.getDailyRentPrice() : BigDecimal.ZERO;
            BigDecimal deposit = gear.getDepositAmount() != null ? gear.getDepositAmount() : BigDecimal.ZERO;
            int qty = Math.max(1, item.getQty());

            subtotal = subtotal.add(dailyPrice.multiply(BigDecimal.valueOf(qty)).multiply(BigDecimal.valueOf(rentalDays)));
            depositTotal = depositTotal.add(deposit.multiply(BigDecimal.valueOf(qty)));
        }

        BigDecimal totalAmount = subtotal.add(depositTotal);
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Calculated order amount must be greater than zero");
        }

        // Find hub by collection hub name, fallback to first item's hub if not found
        String collectionHubName = request.getCollectionHubName();
        com.akigen.ecotrekk_backend.entity.Hub selectedHub = items.get(0).getHub();
        if (collectionHubName != null && !collectionHubName.trim().isEmpty()) {
            var hubOptional = hubRepository.findByName(collectionHubName.trim());
            if (hubOptional.isPresent()) {
                selectedHub = hubOptional.get();
            }
        }

        Invoice invoice = Invoice.builder()
                .invoiceCode(generateInvoiceCode())
                .customer(cart.getCustomer())
                .hub(selectedHub)
                .status(InvoiceStatus.PENDING)
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.PAID)
                .subtotal(subtotal)
                .depositTotal(depositTotal)
                .totalAmount(totalAmount)
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        for (CartItem item : items) {
            com.akigen.ecotrekk_backend.entity.Gear gear = item.getGear();
            if (gear == null) {
                continue;
            }

            BigDecimal dailyPrice = gear.getDailyRentPrice() != null ? gear.getDailyRentPrice() : BigDecimal.ZERO;
            BigDecimal deposit = gear.getDepositAmount() != null ? gear.getDepositAmount() : BigDecimal.ZERO;
            int qty = Math.max(1, item.getQty());

            BigDecimal lineTotal = dailyPrice.multiply(BigDecimal.valueOf(qty)).multiply(BigDecimal.valueOf(rentalDays));
            BigDecimal lineDeposit = deposit.multiply(BigDecimal.valueOf(qty));

            InvoiceItem invoiceItem = InvoiceItem.builder()
                    .invoice(savedInvoice)
                    .gear(gear)
                    .qty(qty)
                    .startDate(startDate)
                    .endDate(endDate)
                    .dailyRentPrice(dailyPrice)
                    .depositAmount(deposit)
                    .lineTotal(lineTotal)
                    .depositTotal(lineDeposit)
                    .build();

            invoiceItemRepository.save(invoiceItem);
        }

        allocateInventoryForInvoice(savedInvoice, items);

        cartItemRepository.deleteByCartId(cart.getId());

        return CashOrderResponseDTO.builder()
                .invoiceId(savedInvoice.getId())
                .invoiceCode(savedInvoice.getInvoiceCode())
                .status(savedInvoice.getStatus())
                .paymentMethod(savedInvoice.getPaymentMethod())
                .paymentStatus(savedInvoice.getPaymentStatus())
                .subtotal(savedInvoice.getSubtotal())
                .depositTotal(savedInvoice.getDepositTotal())
                .totalAmount(savedInvoice.getTotalAmount())
                .build();
    }

    private void allocateInventoryForInvoice(Invoice invoice, List<CartItem> items) {
        for (CartItem item : items) {
            com.akigen.ecotrekk_backend.entity.Inventory inventory = inventoryRepository.findByGearId(item.getGear().getId())
                    .stream().findFirst().orElse(null);

            if (inventory != null) {
                int newQty = Math.max(0, inventory.getAvailableQty() - item.getQty());
                inventory.setAvailableQty(newQty);
                inventoryRepository.save(inventory);
            }
        }
        invoice.setInventoryAllocated(true);
        invoiceRepository.save(invoice);
    }

    private void validateStripeConfig() {
        if (isBlank(stripeProperties.getSecretKey()) || isBlank(stripeProperties.getPublishableKey())) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Stripe keys are not configured");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private LocalDate extractLocalDate(Long utcMillis) {
        if (utcMillis == null || utcMillis <= 0) {
            return null;
        }
        return java.time.Instant.ofEpochMilli(utcMillis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
    }

    private int calculateRentalDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 1;
        }
        if (endDate.isBefore(startDate)) {
            return 1;
        }
        return (int) Math.max(1L, ChronoUnit.DAYS.between(startDate, endDate) + 1L);
    }

    private long calculatePayableAmount(List<CartItem> items, int rentalDays) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDeposit = BigDecimal.ZERO;

        for (CartItem item : items) {
            com.akigen.ecotrekk_backend.entity.Gear gear = item.getGear();
            if (gear == null) {
                continue;
            }

            BigDecimal dailyPrice = gear.getDailyRentPrice() != null ? gear.getDailyRentPrice() : BigDecimal.ZERO;
            BigDecimal deposit = gear.getDepositAmount() != null ? gear.getDepositAmount() : BigDecimal.ZERO;
            int qty = Math.max(1, item.getQty());

            subtotal = subtotal.add(dailyPrice.multiply(BigDecimal.valueOf(qty)).multiply(BigDecimal.valueOf(rentalDays)));
            totalDeposit = totalDeposit.add(deposit.multiply(BigDecimal.valueOf(qty)));
        }

        BigDecimal total = subtotal.add(totalDeposit);
        return total.multiply(BigDecimal.valueOf(100L)).longValue();
    }

    private String generateInvoiceCode() {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "INV-" + token;
    }
}
