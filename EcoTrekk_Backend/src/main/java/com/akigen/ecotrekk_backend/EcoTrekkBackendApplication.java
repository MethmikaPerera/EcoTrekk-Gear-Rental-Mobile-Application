package com.akigen.ecotrekk_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EcoTrekkBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcoTrekkBackendApplication.class, args);
    }

}
