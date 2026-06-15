package com.example.jwt.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class SecureJwtService {

    private final SecretKey key;
    private final String issuer;
    private final String audience;

    public SecureJwtService(
            @Value("${jwt.secure.secret}") String secret,
            @Value("${jwt.secure.issuer}") String issuer,
            @Value("${jwt.secure.audience}") String audience) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
    }

    public String issueToken(String subject, String role) {
        return Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Map<String, Object> validate(String token) {
        Claims claims = Jwts.parser()
                .requireIssuer(issuer)
                .requireAudience(audience)
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", true);
        result.put("algorithm", "HS256");
        result.put("subject", claims.getSubject());
        result.put("role", claims.get("role"));
        result.put("issuer", claims.getIssuer());
        result.put("audience", claims.getAudience());
        return result;
    }
}
