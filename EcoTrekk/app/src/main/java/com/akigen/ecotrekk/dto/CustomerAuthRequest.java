package com.akigen.ecotrekk.dto;

public class CustomerAuthRequest {
    private final String idToken;

    public CustomerAuthRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }
}
