package com.example.demo.web;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/public/hello")
    public Map<String, Object> publicHello() {
        return Map.of(
            "message", "Hola! Este endpoint es publico, no requiere token.",
            "scope", "public",
            "idp", "Microsoft Entra ID");
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        String user = firstNonBlank(
            jwt.getClaimAsString("preferred_username"),
            jwt.getClaimAsString("upn"),
            jwt.getClaimAsString("name"),
            jwt.getSubject());
        return Map.of(
            "message", "Estas autenticado.",
            "user", user,
            "email", String.valueOf(jwt.getClaimAsString("email")),
            "authorities", authorities,
            "roles", jwt.getClaimAsStringList("roles") != null
                ? jwt.getClaimAsStringList("roles")
                : List.of());
    }

    @GetMapping("/admin/hello")
    public Map<String, Object> adminHello(@AuthenticationPrincipal Jwt jwt) {
        String user = firstNonBlank(
            jwt.getClaimAsString("preferred_username"),
            jwt.getClaimAsString("upn"),
            jwt.getClaimAsString("name"),
            jwt.getSubject());
        return Map.of(
            "message", "Bienvenido al area de administracion.",
            "user", user,
            "scope", "admin");
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
