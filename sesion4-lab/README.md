# Sesión 4 — Laboratorios

Demos de **vulnerabilidades y desarrollo seguro** (OWASP Top 10) en los dos stacks.

## Logging seguro (no registrar secretos)

Demuestra por qué **no** deben aparecer contraseñas, JWT, tarjetas o CVV en logs ni en respuestas de error.

| Carpeta | Stack | Puerto |
|---------|-------|--------|
| [spring-secure-logging](spring-secure-logging) | Spring Boot · SLF4J / Logback | 8195 |
| [aspnet-secure-logging](aspnet-secure-logging) | ASP.NET Core · `ILogger` | 8196 |

| Variante | Spring (8195) | .NET (8196) |
|----------|---------------|-------------|
| ANTES — vulnerable | `POST /api/auth/vulnerable/login` | `POST /api/checkout/vulnerable` |
| DESPUÉS — seguro | `POST /api/auth/seguro/login` | `POST /api/checkout/seguro` |

La respuesta JSON incluye `lineasLog` (Java siempre; .NET en la variante segura) para ver el contraste en clase.

---

## Ejercicio 2 · .NET — Perfiles de usuario

**SQL Injection** + **BOLA/IDOR** en versión MAL; Dapper parametrizado + autorización en la corrección.

| Carpeta | Puerto |
|---------|--------|
| [ejercicio2-dotnet](ejercicio2-dotnet) | 8194 |

| Versión | GET | PUT bio |
|---------|-----|---------|
| MAL | `/api/profile/vulnerable/{userId}` | `/api/profile/vulnerable/{userId}/bio` |
| BIEN | `/api/profile/seguro/{userId}` | `/api/profile/seguro/{userId}/bio` |

Auth simulada: cabeceras `X-User-Id` y `X-User-Role: Admin`.

---

## Ejercicio 1 · Java — Búsqueda de productos

Endpoint con **dos vulnerabilidades** en la versión MAL: **SQL Injection** + **XSS** (HTML sin escapar).

| Carpeta | Puerto |
|---------|--------|
| [ejercicio1-java](ejercicio1-java) | 8193 |

| Versión | Endpoint |
|---------|----------|
| MAL | `GET /api/products/vulnerable/search?keyword=...&category=...` |
| BIEN | `GET /api/products/seguro/search?keyword=...&category=...` |

---

## Security Headers

Headers HTTP de defensa en profundidad (CSP, X-Frame-Options, HSTS, CORS).

| Carpeta | Stack | Puerto |
|---------|-------|--------|
| [spring-security-headers](spring-security-headers) | Spring Boot · `SecurityFilterChain` | 8191 |
| [aspnet-security-headers](aspnet-security-headers) | ASP.NET Core · middleware | 8192 |

| Variante | Spring (8191) | .NET (8192) |
|----------|---------------|-------------|
| ANTES — sin headers | `GET /api/insecure/check` | `GET /api/insecure/check` |
| DESPUÉS — seguro | `GET /api/secure/check` | `GET /api/secure/check` |

Ver headers: `curl -i http://localhost:8192/api/secure/check`

---

## Validación de entrada

Validación de datos de usuario (username, email, age).

| Carpeta | Stack | Puerto |
|---------|-------|--------|
| [spring-bean-validation](spring-bean-validation) | Spring Boot · Jakarta `@Valid` | 8189 |
| [aspnet-bean-validation](aspnet-bean-validation) | ASP.NET Core · Data Annotations + FluentValidation | 8190 |

| Variante | Spring (8189) | .NET (8190) |
|----------|---------------|-------------|
| ANTES — sin validar | `POST /api/users/vulnerable` | `POST /api/users/vulnerable` |
| DESPUÉS — seguro | `POST /api/users/seguro` | `POST /api/users/seguro-anotaciones` · `POST /api/users/seguro-fluent` |

---

## Cross-Site Scripting (XSS)

Formulario de comentarios en dos variantes (antes / después) en Java y .NET.

| Carpeta | Stack | Puerto |
|---------|-------|--------|
| [spring-xss-thymeleaf](spring-xss-thymeleaf) | Spring Boot + Thymeleaf (`th:utext` vs `th:text`) | 8183 |
| [aspnet-xss-razor](aspnet-xss-razor) | ASP.NET Core + Razor (`Html.Raw` vs `@Model`) | 8184 |

| Variante | Spring (8183) | .NET (8184) |
|----------|---------------|-------------|
| ANTES — vulnerable | `/comments` | `/comments` |
| DESPUÉS — seguro | `/secure-comments` | `/secure-comments` |

Payload de prueba en el navegador: `<script>alert('XSS')</script>`

---

## XML External Entity (XXE)

Endpoint que parsea perfiles XML: parser **vulnerable** vs **seguro**.

| Carpeta | Stack | Puerto |
|---------|-------|--------|
| [spring-xxe](spring-xxe) | Spring Boot · `DocumentBuilderFactory` | 8187 |
| [aspnet-xxe](aspnet-xxe) | ASP.NET Core · `XmlReaderSettings` (`XxeMitigationExample`) | 8188 |

| Variante | Endpoint |
|----------|----------|
| ANTES — vulnerable | `POST /api/profile/vulnerable` |
| DESPUÉS — seguro (DOM / Reader) | `POST /api/profile/seguro` · `POST /api/profile/seguro-reader` |

Payloads de ejemplo en cada carpeta `payloads/` (lectura de archivo + SSRF a metadatos simulados).

---

## Cross-Site Request Forgery (CSRF)

Transferencia bancaria simulada: versión **sin protección** vs **token Anti-Forgery**.

| Carpeta | Stack | Puerto |
|---------|-------|--------|
| [spring-csrf](spring-csrf) | Spring Boot + Spring Security | 8185 |
| [aspnet-csrf](aspnet-csrf) | ASP.NET Core + Razor Pages (`AddAntiforgery`) | 8186 |

| Variante | Spring (8185) | .NET (8186) |
|----------|---------------|-------------|
| ANTES — vulnerable | `/vulnerable` | `/vulnerable` |
| DESPUÉS — seguro | `/secure` | `/secure` |
| Sitio malicioso | `/attacker` | `/attacker` |

---

## SQL Injection — el antes y el después

Cada caso expone el **mismo** endpoint en dos variantes: una **vulnerable** (concatenación
de la entrada en la SQL) y otra **segura** (consulta parametrizada). La respuesta incluye el
`sqlEjecutado` para ver cómo la inyección reescribe la query.

- **Usuarios** — búsqueda por email (`' OR '1'='1` vuelca toda la tabla, incluida una
  `API_KEY` simulada).
- **Productos** — búsqueda con `LIKE` (`' OR '1'='1' --` devuelve todos los productos).
- **Usuarios JPA** — HQL concatenado vs parámetro nombrado (mismo payload que JDBC).

En .NET además:

- **Usuarios EF** — `FromSqlRaw` con interpolación vs `FromSqlInterpolated` / `FromSqlRaw` con `{0}` / LINQ `Where`.

| Carpeta | Descripción | Puerto API |
|---------|-------------|------------|
| [spring-sql-injection](spring-sql-injection) | Spring Boot + H2 · JDBC, `LIKE` y Hibernate/JPA (vulnerable vs seguro) | 8181 |
| [aspnet-sql-injection](aspnet-sql-injection) | ASP.NET Core + SQLite · ADO.NET y Entity Framework (vulnerable vs seguro) | 8182 |

Endpoints Java (Spring Boot):

| Caso | ANTES — vulnerable | DESPUÉS — seguro |
|------|--------------------|------------------|
| Usuarios (email) | `GET /api/usuarios/vulnerable?email=...` | `GET /api/usuarios/seguro?email=...` |
| Productos (`LIKE`) | `GET /api/productos/vulnerable?q=...` | `GET /api/productos/seguro?q=...` |
| Usuarios JPA (HQL) | `GET /api/usuarios-jpa/vulnerable?email=...` | `GET /api/usuarios-jpa/seguro?email=...` |

Endpoints .NET (ASP.NET Core):

| Caso | ANTES — vulnerable | DESPUÉS — seguro |
|------|--------------------|------------------|
| Usuarios (ADO.NET) | `GET /api/usuarios/vulnerable?email=...` | `GET /api/usuarios/seguro?email=...` |
| Usuarios EF (`FromSqlRaw`) | `GET /api/usuarios-ef/vulnerable?username=...` | `GET /api/usuarios-ef/seguro-interpolado?username=...` |
| Usuarios EF (parámetros) | — | `GET /api/usuarios-ef/seguro-parametros?username=...` |
| Usuarios EF (LINQ) | — | `GET /api/usuarios-ef/seguro-linq?username=...` |

Prueba rápida (sustituye el puerto por 8181 Spring / 8182 .NET):

```bash
# Normal
curl -s -G "http://localhost:8181/api/usuarios/vulnerable" --data-urlencode "email=ana@acme.com"
# Inyección en la versión vulnerable → vuelca toda la tabla
curl -s -G "http://localhost:8181/api/usuarios/vulnerable" --data-urlencode "email=' OR '1'='1"
# Mismo payload en la versión segura → 0 filas
curl -s -G "http://localhost:8181/api/usuarios/seguro"     --data-urlencode "email=' OR '1'='1"
```

## Windows sin PowerShell

Cada demo incluye la sección **「Windows — cmd y curl.exe (sin PowerShell)」** en su
`README.md`, con `docker compose` y todos los pasos usando `curl.exe`.

Los scripts `.ps1` son opcionales.

Los `.sh` del repo usan finales de línea **LF** (`.gitattributes`). Si ves
`env: bash\r: No such file or directory`, convertid con `sed -i '' $'s/\r$//' *.sh` en la
carpeta de la demo.
