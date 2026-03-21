package com.akigen.ecotrekk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderStatusUpdateRequestDTO {

    @NotBlank(message = "status is required")
    private String status;

    private String note;
}
