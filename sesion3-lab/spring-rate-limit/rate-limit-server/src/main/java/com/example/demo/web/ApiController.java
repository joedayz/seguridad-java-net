package com.example.demo.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/datos-sensibles")
    public Map<String, Object> datosSensibles() {
        return Map.of(
                "message", "Datos servidos correctamente (dentro del limite de peticiones).",
                "timestamp", Instant.now().toString());
    }
}
