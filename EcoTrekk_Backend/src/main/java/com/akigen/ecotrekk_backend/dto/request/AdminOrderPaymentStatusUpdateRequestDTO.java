package com.akigen.ecotrekk_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderPaymentStatusUpdateRequestDTO {

    @NotNull(message = "paid is required")
    private Boolean paid;
}
