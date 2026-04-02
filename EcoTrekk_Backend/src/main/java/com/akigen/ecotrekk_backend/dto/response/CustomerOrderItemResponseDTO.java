package com.akigen.ecotrekk_backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerOrderItemResponseDTO {
    private Long id;
    private Long gearId;
    private String gearName;
    private String gearCategory;
    private String imageUrl;
    private Integer quantity;
    private Double rentPrice;
    private Double refundableDeposit;
}
