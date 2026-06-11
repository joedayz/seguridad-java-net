package com.example.logging.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * BIEN — manejo centralizado (diapositiva «Patrones inseguros · Error handling»).
 * Solo aplica a endpoints que dejan propagar la excepcion (p. ej. {@code /api/orders/seguro/**}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {

        String errorId = UUID.randomUUID().toString().substring(0, 8);

        log.error(
                "Unhandled exception {} {} errorId={}",
                request.getMethod(),
                request.getRequestURI(),
                errorId,
                ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("modo", "SEGURO — @ControllerAdvice (sin stack trace ni mensaje interno)");
        body.put("message", "Ha ocurrido un error inesperado.");
        body.put("code", "ERR_INTERNAL_SERVER_ERROR");
        body.put("errorId", errorId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
