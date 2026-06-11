package com.example.logging.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.logging.dto.LoginRequest;
import com.example.logging.service.AuthServiceSecure;
import com.example.logging.service.AuthServiceVulnerable;
import com.example.logging.support.InMemoryLogAppender;

/**
 *  POST /api/auth/vulnerable/login — loguea password, JWT y tarjeta
 *  POST /api/auth/seguro/login       — solo username y ultimos 4 digitos
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceVulnerable authVulnerable;
    private final AuthServiceSecure authSecure;

    public AuthController(AuthServiceVulnerable authVulnerable, AuthServiceSecure authSecure) {
        this.authVulnerable = authVulnerable;
        this.authSecure = authSecure;
    }

    @PostMapping("/vulnerable/login")
    public ResponseEntity<Map<String, Object>> loginVulnerable(@RequestBody LoginRequest req) {
        InMemoryLogAppender.clear();
        var resultado = authVulnerable.login(
                req.username(),
                req.password(),
                req.jwtToken(),
                req.creditCardNumber());

        return ResponseEntity.ok(body("VULNERABLE (password, JWT y tarjeta en logs)", resultado));
    }

    @PostMapping("/seguro/login")
    public ResponseEntity<Map<String, Object>> loginSeguro(@RequestBody LoginRequest req) {
        InMemoryLogAppender.clear();
        var resultado = authSecure.login(
                req.username(),
                req.password(),
                req.jwtToken(),
                req.creditCardNumber());

        return ResponseEntity.ok(body("SEGURO (sin secretos; tarjeta enmascarada)", resultado));
    }

    private static Map<String, Object> body(String modo, Map<String, Object> resultado) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("modo", modo);
        response.put("resultado", resultado);
        response.put("lineasLog", InMemoryLogAppender.snapshot());
        response.put("advertencia", "En produccion estos datos NO deben aparecer en logs ni en respuestas HTTP");
        return response;
    }
}
