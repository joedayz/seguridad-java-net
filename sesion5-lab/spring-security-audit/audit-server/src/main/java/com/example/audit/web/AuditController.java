package com.example.audit.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.audit.dto.LoginRequest;
import com.example.audit.audit.SecurityAuditListener;
import com.example.audit.service.VulnerableAuditService;
import com.example.audit.support.InMemoryLogAppender;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class AuditController {

    private final VulnerableAuditService vulnerableAudit;
    private final AuthenticationManager authenticationManager;
    private final SecurityAuditListener securityAuditListener;

    public AuditController(
            VulnerableAuditService vulnerableAudit,
            AuthenticationManager authenticationManager,
            SecurityAuditListener securityAuditListener) {
        this.vulnerableAudit = vulnerableAudit;
        this.authenticationManager = authenticationManager;
        this.securityAuditListener = securityAuditListener;
    }

    @PostMapping("/auth/vulnerable/login")
    public ResponseEntity<Map<String, Object>> loginVulnerable(@RequestBody LoginRequest req) {
        InMemoryLogAppender.clear();
        boolean ok = vulnerableAudit.login(req.username(), req.password());
        return ResponseEntity.ok(body(
                "VULNERABLE (texto libre, password en log, sin correlation ID)",
                ok ? "LOGIN_OK" : "LOGIN_FAIL",
                null));
    }

    @PostMapping("/auth/seguro/login")
    public ResponseEntity<Map<String, Object>> loginSeguro(
            @RequestBody LoginRequest req,
            HttpServletRequest request) {

        InMemoryLogAppender.clear();
        long start = System.currentTimeMillis();
        String result;
        int status;

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
            result = "LOGIN_OK";
            status = 200;
        } catch (AuthenticationException ex) {
            result = "LOGIN_FAIL";
            status = 401;
        }

        securityAuditListener.logAccess(
                req.username(),
                "/api/auth/seguro/login",
                "POST",
                status,
                System.currentTimeMillis() - start,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return ResponseEntity.status(status).body(body(
                "SEGURO (JSON estructurado SECURITY_AUDIT + correlation ID)",
                result,
                request.getHeader("X-Correlation-ID")));
    }

    @GetMapping("/orders/vulnerable/{id}")
    public ResponseEntity<Map<String, Object>> orderVulnerable(@PathVariable String id) {
        InMemoryLogAppender.clear();
        vulnerableAudit.logAccess("anonymous", "/api/orders/vulnerable/" + id, 200);
        return ResponseEntity.ok(body(
                "VULNERABLE (log de acceso sin contexto)",
                Map.of("orderId", id, "total", 149.99),
                null));
    }

    @GetMapping("/orders/seguro/{id}")
    public ResponseEntity<Map<String, Object>> orderSeguro(
            @PathVariable String id,
            HttpServletRequest request) {

        InMemoryLogAppender.clear();
        long start = System.currentTimeMillis();
        securityAuditListener.logAccess(
                request.getHeader("X-User-Id") != null ? request.getHeader("X-User-Id") : "anonymous",
                "/api/orders/seguro/" + id,
                "GET",
                200,
                System.currentTimeMillis() - start,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return ResponseEntity.ok(body(
                "SEGURO (evento ACCESS con metodo, duracion, IP)",
                Map.of("orderId", id, "total", 149.99),
                request.getHeader("X-Correlation-ID")));
    }

    private static Map<String, Object> body(String modo, Object resultado, String correlationId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("modo", modo);
        response.put("resultado", resultado);
        response.put("correlationId", correlationId);
        response.put("lineasLog", InMemoryLogAppender.snapshot());
        return response;
    }
}
