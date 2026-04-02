package com.akigen.ecotrekk_backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GearCreateRequestDTO {

    @NotBlank(message = "Gear name is required")
    @Size(max = 140, message = "Gear name cannot exceed 140 characters")
    private String name;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String description;

    @NotNull(message = "Daily rental price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Daily rental price must be non-negative")
    private BigDecimal dailyRentPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Deposit amount must be non-negative")
    private BigDecimal depositAmount;

    // Kept for frontend compatibility; current inventory model is global (not per-hub).
    private Long hubId;

    @NotNull(message = "Available count is required")
    @Min(value = 0, message = "Available count must be non-negative")
    private Integer availableCount;

    private Boolean active;
}
