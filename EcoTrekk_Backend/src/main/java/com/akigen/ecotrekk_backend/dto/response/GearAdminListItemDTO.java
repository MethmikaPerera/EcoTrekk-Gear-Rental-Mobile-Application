package com.akigen.ecotrekk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GearAdminListItemDTO {
    private Long id;
    private Long gearId;
    private String name;
    private String category;
    private String description;
    private BigDecimal pricePerDay;
    private int stock;
    private String status;
    private Long hubId;
    private String hubName;
    private Instant updatedAt;
}
