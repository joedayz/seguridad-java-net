package com.example.demo.repo;

import java.util.List;

import com.example.demo.model.Usuario;

/**
 * Resultado de una busqueda que ademas expone la consulta SQL realmente
 * ejecutada. Mostrar el SQL en la respuesta es solo para fines didacticos:
 * permite ver como la inyeccion reescribe la consulta.
 */
public record ResultadoBusqueda(String sqlEjecutado, List<Usuario> usuarios) {

    public int total() {
        return usuarios.size();
    }
}
