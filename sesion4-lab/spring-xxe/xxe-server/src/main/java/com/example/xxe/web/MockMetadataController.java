package com.example.xxe.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simula el endpoint de metadatos de instancia AWS (169.254.169.254) para demostrar
 * la variante SSRF de XXE sin depender de una nube real.
 */
@RestController
public class MockMetadataController {

    @GetMapping("/internal/mock-metadata/iam/security-credentials/demo-role")
    public Map<String, String> mockAwsCredentials() {
        return Map.of(
                "AccessKeyId", "AKIA_DEMO_XXE_LEAK",
                "SecretAccessKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
                "Token", "xxe-demo-session-token",
                "Expiration", "2026-12-31T23:59:59Z");
    }
}
