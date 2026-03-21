package com.akigen.ecotrekk_backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAuthResponseDTO {
    private Long customerId;
    private String firebaseUid;
    private String fullName;
    private String email;
    private String phone;
    private String profilePictureUrl;
    private boolean active;
}
