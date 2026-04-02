package com.akigen.ecotrekk.dto;

public class CustomerProfileUpdateRequest {
    private final String idToken;
    private final String fullName;
    private final String email;
    private final String phone;

    public CustomerProfileUpdateRequest(String idToken, String fullName, String email, String phone) {
        this.idToken = idToken;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    public String getIdToken() {
        return idToken;
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
}
