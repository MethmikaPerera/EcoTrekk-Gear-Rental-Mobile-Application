package com.akigen.ecotrekk.dto;

public class StripePaymentIntentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private String publishableKey;
    private Long amount;
    private String currency;

    public String getClientSecret() {
        return clientSecret;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
