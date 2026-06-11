package com.example.ejercicio1.web;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ejercicio1.dto.ProductDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Ejercicio 1 — busqueda de productos. Dos vulnerabilidades en la version MAL:
 * 1) SQL Injection (concatenacion en LIKE y category)
 * 2) XSS (devuelve HTML sin escapar name/description)
 */
@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProductController(DataSource dataSource, NamedParameterJdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==========================================================================
    // MAL — VULNERABLE (diapositiva «Ejercicio 1 · Analisis»)
    // ==========================================================================

    @GetMapping(value = "/vulnerable/search", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> searchVulnerable(
            @RequestParam String keyword,
            @RequestParam String category) throws Exception {

        // VULN 1 — SQLi: keyword y category concatenados en la query.
        String sql = "SELECT id, name, description, category FROM products "
                + "WHERE name LIKE '%" + keyword + "%' "
                + "AND category = '" + category + "'";

        StringBuilder html = new StringBuilder("<ul>");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // VULN 2 — XSS: name/description van a HTML sin escapar.
                html.append("<li>").append(rs.getString("name"))
                        .append(": ").append(rs.getString("description"))
                        .append(" [").append(rs.getString("category")).append("]")
                        .append("</li>");
            }
        }
        html.append("</ul>");

        return ResponseEntity.ok()
                .header("X-Sql-Ejecutado", sql)
                .body(html.toString());
    }

    // ==========================================================================
    // BIEN — CORREGIDO (diapositiva «Ejercicio 1 · Codigo corregido»)
    // ==========================================================================

    @GetMapping("/seguro/search")
    public ResponseEntity<Map<String, Object>> searchSeguro(
            @RequestParam @Size(max = 100) @Pattern(regexp = "^[\\w\\s-]+$") String keyword,
            @RequestParam @NotBlank String category) {

        String sql = """
                SELECT id, name, description, category FROM products
                WHERE name LIKE :keyword AND category = :category
                """;

        var params = new MapSqlParameterSource()
                .addValue("keyword", "%" + keyword + "%")
                .addValue("category", category);

        List<ProductDto> results = jdbcTemplate.query(sql, params, (rs, rowNum) -> new ProductDto(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("category")));

        // JSON serializado (Jackson), no HTML — sin riesgo de XSS reflejado.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("modo", "SEGURO (NamedParameterJdbcTemplate + validacion + JSON)");
        body.put("sqlPlantilla", sql.trim());
        body.put("parametros", Map.of("keyword", "%" + keyword + "%", "category", category));
        body.put("total", results.size());
        body.put("productos", results);

        return ResponseEntity.ok(body);
    }
}
