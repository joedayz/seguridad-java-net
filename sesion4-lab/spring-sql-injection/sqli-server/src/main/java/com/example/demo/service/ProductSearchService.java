package com.example.demo.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.example.demo.model.Product;
import com.example.demo.repo.ResultadoBusquedaProducto;

/**
 * Busqueda de productos por nombre (patron {@code LIKE}). Contiene DOS
 * implementaciones para comparar lado a lado:
 *
 *  - {@link #searchProductsVulnerable(String)}: ANTES. Concatena la entrada
 *    del usuario en la clausula {@code LIKE}. Es explotable.
 *  - {@link #searchProductsSeguro(String)}: DESPUES. Usa {@code PreparedStatement}
 *    con un parametro vinculado. La base de datos trata la entrada como dato,
 *    no como parte del comando SQL.
 */
@Service
public class ProductSearchService {

    private final DataSource dataSource;

    public ProductSearchService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ==========================================================================
    // ANTES — VULNERABLE
    // ==========================================================================

    /**
     * PELIGRO: concatena la entrada del usuario directamente en la consulta SQL.
     * Un atacante puede alterar la logica de la query (p. ej.
     * {@code ' OR '1'='1' --}) y devolver todos los productos.
     */
    public ResultadoBusquedaProducto searchProductsVulnerable(String searchTerm) {
        // El termino de busqueda entra "tal cual" dentro de las comillas del LIKE.
        String sql = "SELECT id, name, price FROM products WHERE name LIKE '%" + searchTerm + "%'";

        List<Product> productos = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando la consulta vulnerable", e);
        }
        return new ResultadoBusquedaProducto(sql, productos);
    }

    // ==========================================================================
    // DESPUES — SEGURO
    // ==========================================================================

    /**
     * SEGURO: usa una consulta parametrizada. El {@code ?} es un marcador de
     * posicion; el valor se vincula con {@code setString} y la base de datos lo
     * trata como dato, nunca como parte del comando SQL.
     */
    public ResultadoBusquedaProducto searchProductsSeguro(String searchTerm) {
        String sql = "SELECT id, name, price FROM products WHERE name LIKE ?";

        List<Product> productos = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Bind user input safely.
            statement.setString(1, "%" + searchTerm + "%");
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando la consulta segura", e);
        }
        return new ResultadoBusquedaProducto(
                sql + "   [parametro vinculado: %" + searchTerm + "%]",
                productos);
    }

    private Product mapear(ResultSet rs) throws SQLException {
        return new Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("price"));
    }
}
