package com.akigen.ecotrekk_backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StripePaymentIntentResponseDTO {
    private String clientSecret;
    private String paymentIntentId;
    private String publishableKey;
    private Long amount;
    private String currency;
}
