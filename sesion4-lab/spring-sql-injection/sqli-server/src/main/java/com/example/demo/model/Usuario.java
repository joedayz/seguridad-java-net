package com.example.demo.model;

/**
 * Representa una fila de la tabla {@code users}.
 *
 * El campo {@code notaSecreta} simula informacion confidencial que un atacante
 * NO deberia poder leer salvo que conozca el email exacto. La inyeccion SQL
 * permite saltarse esa condicion y exfiltrar todas las filas.
 */
public record Usuario(long id, String email, String nombre, String rol, String notaSecreta) {
}
