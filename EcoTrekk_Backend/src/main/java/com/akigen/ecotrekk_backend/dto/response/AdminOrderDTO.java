package com.akigen.ecotrekk_backend.dto.response;

import com.akigen.ecotrekk_backend.enums.PaymentMethod;
import com.akigen.ecotrekk_backend.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderDTO {
    private String id;
    private AdminOrderCustomerDTO customer;
    private String hubId;
    private String hubName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private List<AdminOrderItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal depositTotal;
    private BigDecimal refundableDeposit;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private List<AdminOrderStatusHistoryDTO> statusHistory;
}
