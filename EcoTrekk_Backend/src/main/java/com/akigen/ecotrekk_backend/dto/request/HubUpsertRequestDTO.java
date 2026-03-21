package com.akigen.ecotrekk_backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubUpsertRequestDTO {

    @NotBlank(message = "Hub name is required")
    @Size(max = 120, message = "Hub name cannot exceed 120 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 160, message = "Address cannot exceed 160 characters")
    private String address;

    @NotBlank(message = "Phone is required")
    @Size(max = 40, message = "Phone cannot exceed 40 characters")
    private String phone;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
    @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
    @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
    private BigDecimal longitude;

    @NotBlank(message = "Map URL is required")
    @Size(max = 500, message = "Map URL cannot exceed 500 characters")
    private String mapLocationUrl;

    @NotBlank(message = "Hub cover image URL is required")
    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;

    private Boolean active;
}
