package com.akigen.ecotrekk_backend.bootstrap;

import com.akigen.ecotrekk_backend.entity.AdminUser;
import com.akigen.ecotrekk_backend.enums.AdminRole;
import com.akigen.ecotrekk_backend.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserBootstrapper implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap.email:}")
    private String bootstrapEmail;

    @Value("${app.admin.bootstrap.full-name:}")
    private String bootstrapFullName;

    @Value("${app.admin.bootstrap.password:}")
    private String bootstrapPassword;

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(bootstrapEmail) || !StringUtils.hasText(bootstrapPassword)) {
            log.warn("Skipping admin bootstrap: missing email or password configuration");
            return;
        }

        adminUserRepository.findByEmailIgnoreCase(bootstrapEmail)
                .ifPresentOrElse(
                        admin -> log.info("Admin bootstrap skipped; user with email {} already exists", bootstrapEmail),
                        this::createDefaultAdmin
                );
    }

    private void createDefaultAdmin() {
        AdminUser adminUser = AdminUser.builder()
                .email(bootstrapEmail.trim().toLowerCase())
                .username(bootstrapEmail.trim().toLowerCase())
                .fullName(StringUtils.hasText(bootstrapFullName) ? bootstrapFullName : "EcoTrekk Admin")
                .passwordHash(passwordEncoder.encode(bootstrapPassword))
                .role(AdminRole.SUPER_ADMIN)
                .active(true)
                .build();

        adminUserRepository.save(adminUser);
        log.info("Default admin user created with email {}", bootstrapEmail);
    }
}
