package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.example.demo.security.ApiKeyAuthFilter;
import com.example.demo.security.CspNonceFilter;
import com.example.demo.web.RateLimitFilter;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;

/**
 * Configuracion de seguridad integrada (Ejercicio 1 — codigo base de referencia).
 */
@Configuration
@EnableWebSecurity
public class ApiSecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final ApiKeyAuthFilter apiKeyAuthFilter;
    private final CspNonceFilter cspNonceFilter;

    public ApiSecurityConfig(CorsConfigurationSource corsConfigurationSource,
                             ApiKeyAuthFilter apiKeyAuthFilter,
                             CspNonceFilter cspNonceFilter) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.apiKeyAuthFilter = apiKeyAuthFilter;
        this.cspNonceFilter = cspNonceFilter;
    }

    @Bean
    public RateLimitFilter rateLimitFilter(ProxyManager<String> proxyManager,
                                           BucketConfiguration authenticatedBucketConfiguration,
                                           BucketConfiguration publicBucketConfiguration) {
        return new RateLimitFilter(proxyManager, authenticatedBucketConfiguration, publicBucketConfiguration);
    }

    @Bean
    public SecurityFilterChain chain(HttpSecurity http, RateLimitFilter rateLimitFilter) throws Exception {
        http
                .requiresChannel(c -> c.anyRequest().requiresSecure())
                .headers(h -> h
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31_536_000)
                                .preload(true))
                        // CSP base; CspNonceFilter anade script-src con nonce en respuestas HTML.
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; frame-ancestors 'none'; base-uri 'self'"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .referrerPolicy(rp -> rp.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/actuator/health").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(cspNonceFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, ApiKeyAuthFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
