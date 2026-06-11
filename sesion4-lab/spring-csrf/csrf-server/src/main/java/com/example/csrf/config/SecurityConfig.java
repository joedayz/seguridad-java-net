package com.example.csrf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Dos cadenas de seguridad para comparar lado a lado:
 *
 *  - {@code /vulnerable/**}: CSRF deshabilitado (como una app mal configurada).
 *  - {@code /secure/**} y el resto: CSRF habilitado con
 *    {@link CookieCsrfTokenRepository#withHttpOnlyFalse()} (patron de la diapositiva).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // ==========================================================================
  // ANTES — VULNERABLE (sin CSRF)
  // ==========================================================================

  @Bean
  @Order(1)
  SecurityFilterChain vulnerableChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/vulnerable/**")
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }

  // ==========================================================================
  // DESPUES — SEGURO (token CSRF + cookie legible por JS si hace falta)
  // ==========================================================================

  @Bean
  @Order(2)
  SecurityFilterChain secureChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf
            // Spring Security — CSRF habilitado por defecto en aplicaciones web.
            // CookieCsrfTokenRepository: el token va en cookie; withHttpOnlyFalse()
            // permite que SPA (Angular/React) lean la cookie y envien X-XSRF-TOKEN.
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

    // Para APIs REST stateless con JWT en Authorization, deshabilitar CSRF puede
    // ser aceptable: .csrf(AbstractHttpConfigurer::disable)

    return http.build();
  }
}
