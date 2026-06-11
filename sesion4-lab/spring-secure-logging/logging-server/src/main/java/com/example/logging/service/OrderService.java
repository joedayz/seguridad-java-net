package com.example.logging.service;

import org.springframework.stereotype.Service;

import com.example.logging.dto.OrderDto;

@Service
public class OrderService {

    public OrderDto findById(String id) {
        if (id == null || !id.matches("\\d+")) {
            throw new IllegalArgumentException(
                    "ID de pedido invalido; tabla interna orders_legacy rechazo formato: " + id);
        }
        throw new java.util.NoSuchElementException(
                "Pedido no encontrado en shard orders_legacy para id=" + id);
    }
}
