package com.example.demo.jpa;

import java.util.List;

/**
 * Resultado de una busqueda JPA que ademas expone la consulta HQL realmente
 * ejecutada (solo para fines didacticos).
 */
public record ResultadoBusquedaJpa(String consultaEjecutada, List<User> users) {

    public int total() {
        return users.size();
    }
}
