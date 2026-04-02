package com.akigen.ecotrekk.dto;

public class CashOrderResponse {
    private Long invoiceId;
    private String invoiceCode;
    private String status;
    private String paymentMethod;
    private String paymentStatus;

    public Long getInvoiceId() {
        return invoiceId;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
