package com.akigen.ecotrekk_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.stripe")
public class StripeProperties {
    private String secretKey;
    private String publishableKey;
    private String currency = "lkr";
}
