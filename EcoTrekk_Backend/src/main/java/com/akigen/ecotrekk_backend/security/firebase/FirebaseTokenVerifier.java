package com.akigen.ecotrekk_backend.security.firebase;

public interface FirebaseTokenVerifier {
    FirebaseUserPayload verifyIdToken(String idToken);
}
