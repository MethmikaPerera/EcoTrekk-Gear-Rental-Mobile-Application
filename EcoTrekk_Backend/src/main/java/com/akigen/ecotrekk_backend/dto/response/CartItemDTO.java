package com.akigen.ecotrekk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long cartId;
    private Long gearId;
    private String gearName;
    private String category;
    private Double pricePerDay;
    private Double depositAmount;
    private String description;
    private Long hubId;
    private String hubName;
    private Integer quantity;
    private String startDate;
    private String endDate;
    private Long addedAt;
    /** Available stock for this gear (used by client for quantity cap) */
    private Integer availableQty;
    private String imageUrl;
}
