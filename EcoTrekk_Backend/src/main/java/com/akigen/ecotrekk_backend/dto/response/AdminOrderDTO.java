package com.akigen.ecotrekk_backend.dto.response;

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
    private List<AdminOrderItemDTO> items;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private List<AdminOrderStatusHistoryDTO> statusHistory;
}
