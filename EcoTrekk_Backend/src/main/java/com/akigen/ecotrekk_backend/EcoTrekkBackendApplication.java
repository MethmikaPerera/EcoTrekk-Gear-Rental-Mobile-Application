package com.akigen.ecotrekk_backend;

import com.akigen.ecotrekk_backend.config.CloudinaryProperties;
import com.akigen.ecotrekk_backend.config.FirebaseProperties;
import com.akigen.ecotrekk_backend.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, FirebaseProperties.class, CloudinaryProperties.class})
@EnableJpaAuditing
public class EcoTrekkBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcoTrekkBackendApplication.class, args);
    }

}
