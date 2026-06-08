package com.example.demo.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/datos")
    public Map<String, Object> obtenerDatos() {
        return Map.of(
                "message", "Datos servidos correctamente (origen CORS permitido).",
                "timestamp", Instant.now().toString());
    }

    @PostMapping("/datos")
    public Map<String, Object> crearDatos(@RequestBody(required = false) Map<String, Object> body) {
        return Map.of(
                "message", "POST aceptado (origen CORS permitido).",
                "received", body == null ? Map.of() : body,
                "timestamp", Instant.now().toString());
    }
}
