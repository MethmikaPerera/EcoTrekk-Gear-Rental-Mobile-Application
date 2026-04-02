package com.akigen.ecotrekk.dto;

public class CustomerRegisterResponse {
    private String customToken;
    private Long customerId;
    private String firebaseUid;
    private String fullName;
    private String email;
    private String phone;
    private String profilePictureUrl;

    public String getCustomToken() {
        return customToken;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
}
