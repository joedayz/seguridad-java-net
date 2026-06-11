package com.example.logging.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.logging.dto.OrderDto;
import com.example.logging.service.OrderService;

/**
 *  GET /api/orders/vulnerable/{id} — devuelve mensaje y stack trace al cliente
 *  GET /api/orders/seguro/{id}       — {@link GlobalExceptionHandler} responde sin detalles internos
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/vulnerable/{id}")
    public ResponseEntity<Map<String, Object>> getVulnerable(@PathVariable String id) {
        try {
            OrderDto order = orderService.findById(id);
            return ResponseEntity.ok(Map.of("pedido", order));
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("modo", "VULNERABLE — excepcion y stack trace expuestos al cliente");
            body.put("error", ex.getMessage());
            body.put("stackTrace", sw.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    @GetMapping("/seguro/{id}")
    public OrderDto getSeguro(@PathVariable String id) {
        return orderService.findById(id);
    }
}
