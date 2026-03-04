package com.akigen.ecotrekk_backend.entity;

import com.akigen.ecotrekk_backend.enums.AdminRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUsers extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 60, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "email", nullable = false, length = 160, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private AdminRole role;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

}
