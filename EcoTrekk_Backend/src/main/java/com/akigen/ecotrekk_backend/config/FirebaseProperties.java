package com.akigen.ecotrekk_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.firebase")
public class FirebaseProperties {
    private boolean enabled = true;
    private String serviceAccountPath;
}
