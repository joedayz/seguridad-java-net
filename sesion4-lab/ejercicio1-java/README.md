# Ejercicio 1 · Java — Búsqueda de productos (mal vs corrección)

Analiza el endpoint de búsqueda: identifica **dos vulnerabilidades** en la versión MAL y compárala con la corrección.

| Versión | Endpoint | Respuesta |
|---------|----------|-----------|
| **MAL** | `GET /api/products/vulnerable/search` | HTML sin escapar |
| **BIEN** | `GET /api/products/seguro/search` | JSON (`ProductDto`) |

Puerto: **8193**

---

## Las dos vulnerabilidades (versión MAL)

### 1. SQL Injection

```java
String sql = "SELECT ... WHERE name LIKE '%" + keyword + "%' "
           + "AND category = '" + category + "'";
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(sql);
```

**Payload** (vuelca todos los productos):

```text
keyword=' OR '1'='1' --
category=electronics
```

```bash
curl -s -G "http://localhost:8193/api/products/vulnerable/search" \
  --data-urlencode "keyword=' OR '1'='1' --" \
  --data-urlencode "category=electronics"
```

Fíjate en la cabecera `X-Sql-Ejecutado`.

### 2. Cross-Site Scripting (XSS)

```java
html.append("<li>").append(rs.getString("name"))
    .append(": ").append(rs.getString("description"))
```

Devuelve **HTML** con `name` y `description` sin escapar. El monitor en BD tiene un `<script>` en `description` → al abrir la URL en el navegador se ejecuta el `alert`.

```bash
# Abre en el navegador (no curl):
http://localhost:8193/api/products/vulnerable/search?keyword=Monitor&category=electronics
```

---

## La corrección (versión BIEN)

```java
@GetMapping("/seguro/search")
public ResponseEntity<...> searchSeguro(
    @RequestParam @Size(max = 100) @Pattern(regexp = "^[\\w\\s-]+$") String keyword,
    @RequestParam @NotBlank String category) {

    String sql = "SELECT ... WHERE name LIKE :keyword AND category = :category";
    var params = new MapSqlParameterSource()
        .addValue("keyword", "%" + keyword + "%")
        .addValue("category", category);

    List<ProductDto> results = jdbcTemplate.query(sql, params, ...);
    return ResponseEntity.ok(results);  // JSON, no HTML
}
```

| Problema | Corrección |
|----------|------------|
| SQLi | `NamedParameterJdbcTemplate` + parámetros nombrados |
| XSS | Respuesta **JSON** (Jackson), no HTML concatenado |
| Entrada inválida | `@Size`, `@Pattern`, `@NotBlank` |

```bash
# Busqueda normal
curl -s -G "http://localhost:8193/api/products/seguro/search" \
  --data-urlencode "keyword=Laptop" \
  --data-urlencode "category=electronics"

# Mismo SQLi — 0 filas (parametro literal)
curl -s -G "http://localhost:8193/api/products/seguro/search" \
  --data-urlencode "keyword=' OR '1'='1' --" \
  --data-urlencode "category=electronics"
```

---

## Cómo levantarlo

Requisitos: Docker Desktop o Podman con `compose`.

```bash
cd ejercicio1-java
docker compose up --build
```

Alternativa:

```bash
./compose.sh up --build
```

Parar: `docker compose down` o `./compose.sh down`.

---

## Windows — cmd y curl.exe (sin PowerShell)

```cmd
cd ejercicio1-java
docker compose up --build
curl.exe -s -G "http://localhost:8193/api/products/seguro/search" --data-urlencode "keyword=Laptop" --data-urlencode "category=electronics"
```
