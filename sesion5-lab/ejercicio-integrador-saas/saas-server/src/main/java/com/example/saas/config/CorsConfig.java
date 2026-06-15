package com.example.saas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${demo.cors.permissive:true}")
    private boolean permissive;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (permissive) {
            registry.addMapping("/api/**")
                    .allowedOrigins("*")
                    .allowedMethods("*");
        } else {
            registry.addMapping("/api/v1/orders/seguro/**")
                    .allowedOrigins("https://app.demo.local")
                    .allowedMethods("GET");
        }
    }
}
