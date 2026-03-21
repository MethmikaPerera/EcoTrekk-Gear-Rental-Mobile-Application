package com.akigen.ecotrekk_backend.dto.response;

import com.akigen.ecotrekk_backend.enums.AdminRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRegisterResponseDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private AdminRole role;
    private boolean active;
    private java.time.Instant createdAt;
    private Long hubId;
}
