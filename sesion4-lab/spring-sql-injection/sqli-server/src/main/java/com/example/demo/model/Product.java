package com.example.demo.model;

/**
 * Representa una fila de la tabla {@code products}.
 */
public record Product(long id, String name, double price) {
}
