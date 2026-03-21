package com.akigen.ecotrekk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderItemDTO {
    private String id;
    private String gearId;
    private String gearName;
    private String imageUrl;
    private int quantity;
    private BigDecimal pricePerDay;
    private BigDecimal totalPrice;
}
