package com.akigen.ecotrekk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFirebaseLoginRequestDTO {

    @NotBlank
    private String idToken;

}
