package com.akigen.ecotrekk_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FirebaseAdminConfig {
    private final FirebaseProperties firebaseProperties;

    @PostConstruct
    public void initFirebase() {
        if (!firebaseProperties.isEnabled()) {
            log.info("Firebase Admin initialization skipped because app.firebase.enabled=false");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            return; // already initialized
        }

        String serviceAccountPath = firebaseProperties.getServiceAccountPath();
        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            throw new IllegalStateException("app.firebase.service-account-path must be set when Firebase is enabled");
        }

        Path path = Path.of(serviceAccountPath);
        if (!Files.exists(path)) {
            throw new IllegalStateException("Firebase service account file not found at " + path.toAbsolutePath());
        }

        try (InputStream in = Files.newInputStream(path)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(in);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin initialized successfully using {}", path.toAbsolutePath());
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to initialize Firebase Admin. Check service account path: " + path.toAbsolutePath(),
                    ex
            );
        }
    }
    
    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }
}
