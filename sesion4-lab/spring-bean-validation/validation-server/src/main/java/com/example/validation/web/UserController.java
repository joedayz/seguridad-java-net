package com.example.validation.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.validation.dto.CreateUserRequest;
import com.example.validation.service.UserService;

import jakarta.validation.Valid;

/**
 * Creacion de usuarios en dos variantes:
 *
 *  POST /api/users/vulnerable  -> sin @Valid (acepta datos invalidos)
 *  POST /api/users/seguro      -> con @Valid (Bean Validation, 400 si falla)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/vulnerable")
    public ResponseEntity<Map<String, Object>> createVulnerable(@RequestBody CreateUserRequest req) {
        // PELIGRO: sin @Valid la API acepta username vacio, email mal formado, age < 18...
        userService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "modo", "VULNERABLE (sin @Valid)",
                "mensaje", "Usuario aceptado sin validar entrada",
                "usuario", req));
    }

    @PostMapping("/seguro")
    public ResponseEntity<Map<String, Object>> createSeguro(@Valid @RequestBody CreateUserRequest req) {
        // Si la validacion falla, Spring lanza MethodArgumentNotValidException -> 400
        userService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "modo", "SEGURO (@Valid + Bean Validation)",
                "mensaje", "Usuario creado con datos validados",
                "usuario", req));
    }
}
