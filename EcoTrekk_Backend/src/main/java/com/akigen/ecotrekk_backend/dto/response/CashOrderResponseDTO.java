package com.akigen.ecotrekk_backend.dto.response;

import com.akigen.ecotrekk_backend.enums.InvoiceStatus;
import com.akigen.ecotrekk_backend.enums.PaymentMethod;
import com.akigen.ecotrekk_backend.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CashOrderResponseDTO {
    private Long invoiceId;
    private String invoiceCode;
    private InvoiceStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal depositTotal;
    private BigDecimal totalAmount;
}
