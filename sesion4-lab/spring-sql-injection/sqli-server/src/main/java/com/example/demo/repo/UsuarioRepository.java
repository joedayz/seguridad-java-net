package com.example.demo.repo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.example.demo.model.Usuario;

/**
 * Acceso a datos de usuarios. Contiene DOS implementaciones de la misma
 * busqueda para comparar lado a lado:
 *
 *  - {@link #buscarVulnerable(String)}: ANTES. Concatena la entrada del usuario
 *    directamente en la SQL (el patron de la diapositiva). Es explotable.
 *  - {@link #buscarSeguro(String)}: DESPUES. Usa PreparedStatement con un
 *    parametro vinculado. La base de datos trata la entrada como dato, no como
 *    codigo SQL.
 */
@Repository
public class UsuarioRepository {

    private final DataSource dataSource;

    public UsuarioRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ==========================================================================
    // ANTES — VULNERABLE
    // ==========================================================================

    /**
     * PELIGRO: concatena la entrada del usuario directamente en la consulta SQL.
     * Un atacante puede alterar la logica de la query (p. ej. {@code ' OR '1'='1})
     * y acceder a datos no autorizados.
     */
    public ResultadoBusqueda buscarVulnerable(String email) {
        // El email entra "tal cual" dentro de las comillas: aqui esta el fallo.
        String sql = "SELECT id, email, nombre, rol, nota_secreta FROM users WHERE email = '" + email + "'";

        List<Usuario> usuarios = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                usuarios.add(mapear(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando la consulta vulnerable", e);
        }
        return new ResultadoBusqueda(sql, usuarios);
    }

    // ==========================================================================
    // DESPUES — SEGURO
    // ==========================================================================

    /**
     * SEGURO: usa una consulta parametrizada. El {@code ?} es un marcador de
     * posicion; el valor se vincula con {@code setString} y la base de datos lo
     * trata como dato, nunca como parte del comando SQL. La inyeccion deja de
     * funcionar: {@code ' OR '1'='1} se busca literalmente como un email.
     */
    public ResultadoBusqueda buscarSeguro(String email) {
        String sql = "SELECT id, email, nombre, rol, nota_secreta FROM users WHERE email = ?";

        List<Usuario> usuarios = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    usuarios.add(mapear(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando la consulta segura", e);
        }
        return new ResultadoBusqueda(sql + "   [parametro vinculado: " + email + "]", usuarios);
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("nombre"),
                rs.getString("rol"),
                rs.getString("nota_secreta"));
    }
}
