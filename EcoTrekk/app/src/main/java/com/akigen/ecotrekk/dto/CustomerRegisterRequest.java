package com.akigen.ecotrekk.dto;

public class CustomerRegisterRequest {
    private final String fullName;
    private final String email;
    private final String password;
    private final String phone;

    public CustomerRegisterRequest(String fullName, String email, String password, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }
}
