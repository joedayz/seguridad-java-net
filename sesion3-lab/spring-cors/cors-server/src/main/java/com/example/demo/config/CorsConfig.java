package com.example.demo.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuracion CORS segura (ver diapositiva).
 *
 * NUNCA en produccion:
 *   cors.allowedOrigins("*");
 *   cors.allowCredentials(true);
 * Esa combinacion es invalida y peligrosa: expone tokens de sesion a sitios maliciosos.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${cors.allowed-origins}") String allowedOrigins,
            @Value("${cors.allowed-methods}") String allowedMethods,
            @Value("${cors.max-age-seconds:3600}") long maxAgeSeconds) {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(splitCsv(allowedOrigins));
        config.setAllowedMethods(splitCsv(allowedMethods));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(Duration.ofSeconds(maxAgeSeconds));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private static List<String> splitCsv(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
