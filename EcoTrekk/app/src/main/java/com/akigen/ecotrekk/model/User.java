package com.akigen.ecotrekk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String uid;
    private String uname;
    private String email;
    private String phone;
    private String address;
    private String profilePicture;
}
