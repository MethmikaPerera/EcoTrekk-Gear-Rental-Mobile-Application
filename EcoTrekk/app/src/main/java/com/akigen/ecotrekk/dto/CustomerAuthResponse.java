package com.akigen.ecotrekk.dto;

public class CustomerAuthResponse {
    private Long customerId;
    private String firebaseUid;
    private String fullName;
    private String email;
    private String phone;
    private String profilePictureUrl;
    private boolean active;

    public CustomerAuthResponse() {
    }

    public CustomerAuthResponse(Long customerId, String firebaseUid, String fullName,
            String email, String phone, String profilePictureUrl) {
        this.customerId = customerId;
        this.firebaseUid = firebaseUid;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.profilePictureUrl = profilePictureUrl;
        this.active = true;
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

    public boolean isActive() {
        return active;
    }
}
