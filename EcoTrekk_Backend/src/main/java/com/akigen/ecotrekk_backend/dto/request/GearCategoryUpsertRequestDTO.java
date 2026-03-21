package com.akigen.ecotrekk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GearCategoryUpsertRequestDTO {

    @NotBlank(message = "Category name is required")
    @Size(max = 80, message = "Category name cannot exceed 80 characters")
    private String name;
}
