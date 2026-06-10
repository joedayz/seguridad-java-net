package com.example.demo.web;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de demostracion del Ejercicio 3.
 *
 * El objetivo es PROBAR que la app NO usa una credencial estatica de PostgreSQL,
 * sino un usuario generado dinamicamente por HashiCorp Vault (con TTL de 1 hora).
 * Por eso {@code current_user} en la BD es un rol efimero tipo
 * {@code v-approle-app-role-XXXX...} y no el {@code vault-admin} de configuracion.
 */
@RestController
@RequestMapping("/api")
public class CredentialsController {

    private final JdbcTemplate jdbc;
    private final Environment env;

    @Value("${spring.application.name:spring-vault-demo}")
    private String appName;

    public CredentialsController(DataSource dataSource, Environment env) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.env = env;
    }

    /** Usuario real con el que la app esta conectada a PostgreSQL (lo emite Vault). */
    @GetMapping("/db/whoami")
    public Map<String, Object> whoami() {
        String dbUser = jdbc.queryForObject("SELECT current_user", String.class);
        String version = jdbc.queryForObject("SELECT version()", String.class);
        return Map.of(
                "app", appName,
                "vaultGeneratedDbUser", dbUser,
                "configuredUsername", env.getProperty("spring.datasource.username", "(inyectado por Vault)"),
                "postgresVersion", version,
                "explicacion", "El usuario empieza por 'v-approle-' => credencial dinamica emitida por Vault",
                "timestamp", Instant.now().toString());
    }

    /** Datos de negocio leidos con la credencial dinamica (permiso solo de SELECT). */
    @GetMapping("/products")
    public List<Map<String, Object>> products() {
        return jdbc.queryForList("SELECT id, name, price FROM products ORDER BY id");
    }

    /** Roles efimeros que Vault ha creado en PostgreSQL (visibles como roles 'v-approle-...'). */
    @GetMapping("/db/dynamic-roles")
    public List<Map<String, Object>> dynamicRoles() {
        return jdbc.queryForList(
                "SELECT rolname, rolvaliduntil FROM pg_roles "
                        + "WHERE rolname LIKE 'v-%' ORDER BY rolvaliduntil");
    }
}
