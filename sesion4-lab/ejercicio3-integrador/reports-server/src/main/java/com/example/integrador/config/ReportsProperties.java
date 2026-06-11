package com.example.integrador.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reports")
public record ReportsProperties(
        String apiKey,
        RateLimit rateLimit) {

    public record RateLimit(int capacity, int windowSeconds) {
    }
}
