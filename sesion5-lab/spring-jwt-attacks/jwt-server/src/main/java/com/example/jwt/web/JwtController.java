package com.example.jwt.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.jwt.service.SecureJwtService;
import com.example.jwt.service.VulnerableJwtService;

@RestController
@RequestMapping("/api/jwt")
public class JwtController {

    private final VulnerableJwtService vulnerable;
    private final SecureJwtService secure;

    public JwtController(VulnerableJwtService vulnerable, SecureJwtService secure) {
        this.vulnerable = vulnerable;
        this.secure = secure;
    }

    @PostMapping("/vulnerable/issue")
    public Map<String, Object> issueVulnerable(@RequestBody Map<String, String> body) {
        String subject = body.getOrDefault("subject", "user1");
        String role = body.getOrDefault("role", "USER");
        String token = vulnerable.issueToken(subject, role);
        return Map.of(
                "modo", "VULNERABLE (secreto debil en application.yml)",
                "token", token,
                "hint", "Prueba alg=none: eyJhbGciOiJubmUifQ.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9.");
    }

    @GetMapping("/vulnerable/verify")
    public ResponseEntity<Map<String, Object>> verifyVulnerable(
            @RequestHeader("Authorization") String authorization) {

        String token = authorization.replace("Bearer ", "");
        var claims = vulnerable.validate(token);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("modo", "VULNERABLE");
        body.put("claims", claims);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/seguro/issue")
    public Map<String, Object> issueSecure(@RequestBody Map<String, String> body) {
        String subject = body.getOrDefault("subject", "user1");
        String role = body.getOrDefault("role", "USER");
        return Map.of(
                "modo", "SEGURO (HS256, iss/aud validados)",
                "token", secure.issueToken(subject, role));
    }

    @GetMapping("/seguro/verify")
    public ResponseEntity<Map<String, Object>> verifySecure(
            @RequestHeader("Authorization") String authorization) {

        String token = authorization.replace("Bearer ", "");
        try {
            var claims = secure.validate(token);
            return ResponseEntity.ok(Map.of("modo", "SEGURO", "claims", claims));
        } catch (Exception ex) {
            return ResponseEntity.status(401).body(Map.of(
                    "modo", "SEGURO",
                    "valid", false,
                    "error", ex.getMessage()));
        }
    }
}
