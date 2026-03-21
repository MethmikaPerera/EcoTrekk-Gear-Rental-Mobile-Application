package com.akigen.ecotrekk_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginRequestDTO {

    @NotBlank
    @Email
    @Size(max = 160)
    private String email;

    @NotBlank
    private String password;

}
