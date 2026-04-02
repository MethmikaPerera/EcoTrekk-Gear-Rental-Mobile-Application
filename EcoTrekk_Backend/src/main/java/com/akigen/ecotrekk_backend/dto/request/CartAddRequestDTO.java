package com.akigen.ecotrekk_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartAddRequestDTO {

    @NotNull(message = "Gear ID is required")
    private Long gearId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private Long hubId;

    @NotNull(message = "Start date is required")
    private String startDate;

    @NotNull(message = "End date is required")
    private String endDate;
}
