package com.akigen.ecotrekk_backend.service;

import com.akigen.ecotrekk_backend.dto.request.CreateOrderRequestDTO;
import com.akigen.ecotrekk_backend.dto.response.StripePaymentIntentResponseDTO;
import com.akigen.ecotrekk_backend.dto.response.CashOrderResponseDTO;

public interface StripePaymentService {
    StripePaymentIntentResponseDTO createPaymentIntentForCustomer(Long customerId);

    CashOrderResponseDTO createCashOrderForCustomer(Long customerId, CreateOrderRequestDTO request);

    CashOrderResponseDTO createCardOrderForCustomer(Long customerId, CreateOrderRequestDTO request);
}
