package com.akigen.ecotrekk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCustomerSummaryDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String profilePictureUrl;
    private boolean active;
    private Instant createdAt;
}