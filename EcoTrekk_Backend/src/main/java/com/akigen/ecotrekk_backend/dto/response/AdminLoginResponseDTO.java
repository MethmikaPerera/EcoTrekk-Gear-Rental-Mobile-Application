package com.akigen.ecotrekk_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponseDTO {
    private String tokenType;  //Bearer

    @JsonProperty("token")
    private String accessToken;

    private long expiresInSeconds;

    private AdminUserSummaryDTO adminUser;
}
