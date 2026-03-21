package com.akigen.ecotrekk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminStatusUpdateRequestDTO {
    
    @NotBlank(message = "Status cannot be blank")
    private String status;
}
