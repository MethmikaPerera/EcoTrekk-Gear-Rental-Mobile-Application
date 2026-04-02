package com.akigen.ecotrekk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemDTO {
    private Long id;
    private Long gearId;
    private String gearName;
    private String category;
    private Double pricePerDay;
    private Double depositAmount;
    private String description;
    private String imageUrl;
    private Long addedAt;
}
