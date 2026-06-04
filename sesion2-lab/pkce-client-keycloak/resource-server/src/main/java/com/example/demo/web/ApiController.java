package com.example.demo.web;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/public/hello")
    public Map<String, Object> publicHello() {
        return Map.of(
            "message", "Hola! Endpoint publico del Resource Server.",
            "scope", "public",
            "idp", "Keycloak (realm demo)");
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        return Map.of(
            "message", "Estas autenticado con un JWT obtenido via PKCE.",
            "user", jwt.getClaimAsString("preferred_username"),
            "email", String.valueOf(jwt.getClaimAsString("email")),
            "authorities", authorities);
    }

    @GetMapping("/admin/hello")
    public Map<String, Object> adminHello(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
            "message", "Bienvenido al area de administracion.",
            "user", jwt.getClaimAsString("preferred_username"),
            "scope", "admin");
    }
}
