package com.akigen.ecotrekk_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisterResponseDTO {
    /** Firebase custom token — use signInWithCustomToken() on the mobile client. */
    private String customToken;
    private Long customerId;
    private String firebaseUid;
    private String fullName;
    private String email;
    private String phone;
    private String profilePictureUrl;
}
