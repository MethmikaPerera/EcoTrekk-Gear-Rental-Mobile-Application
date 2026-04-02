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
public class PublicFeaturedGearDTO {
    private Long id;
    private String name;
    private String category;
    private String description;
    private BigDecimal pricePerDay;
    private BigDecimal depositAmount;
    private String imageUrl;
    /** Available units in inventory (null = unknown / unlimited for legacy fallback) */
    private Integer availableQty;
}
