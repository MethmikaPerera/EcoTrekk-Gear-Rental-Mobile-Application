package com.akigen.ecotrekk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", nullable = false, length = 128, unique = true)
    private String firebaseUid;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "email", nullable = false, length = 160, unique = true)
    private String email;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

}
