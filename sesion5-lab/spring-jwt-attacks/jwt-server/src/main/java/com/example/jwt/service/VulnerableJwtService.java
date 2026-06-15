package com.example.jwt.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class VulnerableJwtService {

    private final String secret;
    private final ObjectMapper mapper = new ObjectMapper();

    public VulnerableJwtService(@Value("${jwt.vulnerable.secret}") String secret) {
        this.secret = secret;
    }

    public String issueToken(String subject, String role) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key)
                .compact();
    }

    /**
     * VULN: acepta alg=none (sin firma) y no valida iss/aud.
     */
    public Map<String, Object> validate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Token malformado");
        }

        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        Map<?, ?> header = readJson(headerJson);
        String alg = String.valueOf(header.get("alg"));

        Claims claims;
        if ("none".equalsIgnoreCase(alg)) {
            // VULN critica — acepta tokens sin firma
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            claims = Jwts.claims(readJson(payloadJson));
        } else {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", true);
        result.put("algorithm", alg);
        result.put("subject", claims.getSubject());
        result.put("role", claims.get("role"));
        result.put("secretLength", secret.length());
        result.put("issues", new String[] {
                "Acepta algoritmo none",
                "Secreto HMAC debil (" + secret.length() + " chars)",
                "No valida iss ni aud"
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJson(String json) {
        try {
            return mapper.readValue(json, Map.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("JSON invalido en JWT", ex);
        }
    }
}
