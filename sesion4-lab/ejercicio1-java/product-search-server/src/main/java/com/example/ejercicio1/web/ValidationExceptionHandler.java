package com.example.ejercicio1.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<Map<String, Object>> handleValidation(Exception ex) {
        Map<String, String> errores = new LinkedHashMap<>();

        if (ex instanceof ConstraintViolationException cve) {
            cve.getConstraintViolations().forEach(v ->
                    errores.put(v.getPropertyPath().toString(), v.getMessage()));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("modo", "SEGURO — validacion rechazada");
        body.put("status", 400);
        body.put("errores", errores.isEmpty() ? ex.getMessage() : errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
