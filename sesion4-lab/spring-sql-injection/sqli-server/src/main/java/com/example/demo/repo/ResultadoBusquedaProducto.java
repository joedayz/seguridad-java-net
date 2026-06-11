package com.example.demo.repo;

import java.util.List;

import com.example.demo.model.Product;

/**
 * Resultado de una busqueda de productos que ademas expone la consulta SQL
 * realmente ejecutada (solo para fines didacticos).
 */
public record ResultadoBusquedaProducto(String sqlEjecutado, List<Product> productos) {

    public int total() {
        return productos.size();
    }
}
