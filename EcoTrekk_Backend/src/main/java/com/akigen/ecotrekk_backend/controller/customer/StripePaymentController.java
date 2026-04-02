package com.akigen.ecotrekk_backend.controller.customer;

import com.akigen.ecotrekk_backend.dto.request.CreateOrderRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.CashOrderResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.StripePaymentIntentResponseDTO;
import com.akigen.ecotrekk_backend.service.StripePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/payments")
@RequiredArgsConstructor
public class StripePaymentController {

    private final StripePaymentService stripePaymentService;

    @PostMapping("/{customerId}/intent")
    public ResponseEntity<StripePaymentIntentResponseDTO> createPaymentIntent(@PathVariable Long customerId) {
        return ResponseEntity.ok(stripePaymentService.createPaymentIntentForCustomer(customerId));
    }

    @PostMapping("/{customerId}/cash-order")
    public ResponseEntity<CashOrderResponseDTO> createCashOrder(
            @PathVariable Long customerId,
            @RequestBody CreateOrderRequestDTO request) {
        return ResponseEntity.ok(stripePaymentService.createCashOrderForCustomer(customerId, request));
    }

    @PostMapping("/{customerId}/card-order")
    public ResponseEntity<CashOrderResponseDTO> createCardOrder(
            @PathVariable Long customerId,
            @RequestBody CreateOrderRequestDTO request) {
        return ResponseEntity.ok(stripePaymentService.createCardOrderForCustomer(customerId, request));
    }
}
