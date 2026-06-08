package com.example.demo.security;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Genera un nonce por peticion y lo expone para CSP con nonce (paso 03 del ejercicio).
 */
@Component
public class CspNonceFilter extends OncePerRequestFilter {

    public static final String NONCE_ATTRIBUTE = "cspNonce";

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String nonce = generateNonce();
        request.setAttribute(NONCE_ATTRIBUTE, nonce);

        response.setHeader("Content-Security-Policy",
                "default-src 'self'; script-src 'self' 'nonce-" + nonce
                        + "'; frame-ancestors 'none'; base-uri 'self'");

        filterChain.doFilter(request, response);
    }

    private String generateNonce() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
