package com.example.saas.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.saas.model.OrderEntity;
import com.example.saas.repo.OrderRepository;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderRepository orders;

    public OrderController(OrderRepository orders) {
        this.orders = orders;
    }

    @GetMapping("/vulnerable/{orderId}")
    public ResponseEntity<Map<String, Object>> getVulnerable(@PathVariable Long orderId) {
        return orders.findById(orderId)
                .map(order -> ResponseEntity.ok(wrap("VULNERABLE (IDOR — sin validar propiedad)", order)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/seguro/{orderId}")
    public ResponseEntity<Map<String, Object>> getSeguro(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Cabecera X-User-Id requerida (simula JWT sub)"));
        }

        return orders.findByIdAndUserId(orderId, userId)
                .map(order -> ResponseEntity.ok(wrap("SEGURO (findByIdAndUserId)", order)))
                .orElse(ResponseEntity.status(404).build());
    }

    private static Map<String, Object> wrap(String modo, OrderEntity order) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("modo", modo);
        body.put("order", Map.of(
                "id", order.getId(),
                "userId", order.getUserId(),
                "customerName", order.getCustomerName(),
                "total", order.getTotal(),
                "status", order.getStatus()));
        return body;
    }
}

@RestController
@RequestMapping("/api/audit")
class AuditChecklistController {

    @GetMapping("/matriz-riesgos")
    public Map<String, Object> matriz() {
        return Map.of(
                "titulo", "Matriz de riesgos del laboratorio (Sesion 5)",
                "hallazgos", List.of(
                        Map.of("vulnerabilidad", "JWT algoritmo none", "cvss", 9.1, "severidad", "Critica"),
                        Map.of("vulnerabilidad", "Actuator /env expuesto", "cvss", 8.6, "severidad", "Alta"),
                        Map.of("vulnerabilidad", "Secretos en repositorio", "cvss", 8.2, "severidad", "Alta"),
                        Map.of("vulnerabilidad", "IDOR Orders Service", "cvss", 7.5, "severidad", "Alta"),
                        Map.of("vulnerabilidad", "CORS permisivo (*)", "cvss", 5.4, "severidad", "Media")));
    }
}
