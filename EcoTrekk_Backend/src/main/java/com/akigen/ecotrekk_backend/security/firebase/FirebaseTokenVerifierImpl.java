package com.akigen.ecotrekk_backend.security.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseTokenVerifierImpl implements FirebaseTokenVerifier {
    @Override
    public FirebaseUserPayload verifyIdToken(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();
            String picture = decodedToken.getPicture();
            String phone = FirebaseAuth.getInstance().getUser(uid).getPhoneNumber();

            return FirebaseUserPayload.builder()
                    .uid(uid)
                    .email(email)
                    .fullName(name)
                    .phone(phone)
                    .profilePictureUrl(picture)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Invalid firebase idToken", e);
        }
    }
}
