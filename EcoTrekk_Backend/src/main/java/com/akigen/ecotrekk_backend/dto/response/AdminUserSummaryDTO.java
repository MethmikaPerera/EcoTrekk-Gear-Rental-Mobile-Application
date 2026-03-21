package com.akigen.ecotrekk_backend.dto.response;

import com.akigen.ecotrekk_backend.enums.AdminRole;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSummaryDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private AdminRole role;
}
