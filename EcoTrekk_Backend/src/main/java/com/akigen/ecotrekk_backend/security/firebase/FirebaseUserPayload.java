package com.akigen.ecotrekk_backend.security.firebase;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseUserPayload {
    private String uid;
    private String email;
    private String fullName;
    private String phone;
    private String profilePictureUrl;
}
