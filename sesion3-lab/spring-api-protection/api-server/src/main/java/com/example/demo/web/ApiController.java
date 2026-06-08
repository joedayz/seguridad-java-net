package com.example.demo.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/datos")
    public Map<String, Object> datos(@AuthenticationPrincipal String apiKey) {
        return Map.of(
                "message", "Datos protegidos (usuario autenticado).",
                "user", apiKey,
                "timestamp", Instant.now().toString());
    }

    @PostMapping("/datos")
    public Map<String, Object> crear(@AuthenticationPrincipal String apiKey,
                                     @RequestBody(required = false) Map<String, Object> body) {
        return Map.of(
                "message", "POST aceptado.",
                "user", apiKey,
                "received", body == null ? Map.of() : body,
                "timestamp", Instant.now().toString());
    }
}
