# Sesión 4 — Laboratorios

Demos de **vulnerabilidades y desarrollo seguro** (OWASP Top 10) en Java (Spring Boot) y .NET (ASP.NET Core).

Cada carpeta tiene su propio **`README.md`** con endpoints, payloads y pruebas paso a paso.

---

## Índice completo de demos

| Puerto | Tema | Carpeta | Stack |
|--------|------|---------|-------|
| **8181** | SQL Injection | [spring-sql-injection](spring-sql-injection) | Spring Boot + H2 |
| **8182** | SQL Injection | [aspnet-sql-injection](aspnet-sql-injection) | ASP.NET Core + SQLite |
| **8183** | XSS | [spring-xss-thymeleaf](spring-xss-thymeleaf) | Spring Boot + Thymeleaf |
| **8184** | XSS | [aspnet-xss-razor](aspnet-xss-razor) | ASP.NET Core + Razor |
| **8185** | CSRF | [spring-csrf](spring-csrf) | Spring Boot + Spring Security |
| **8186** | CSRF | [aspnet-csrf](aspnet-csrf) | ASP.NET Core + Anti-Forgery |
| **8187** | XXE | [spring-xxe](spring-xxe) | Spring Boot · XML parser |
| **8188** | XXE | [aspnet-xxe](aspnet-xxe) | ASP.NET Core · `XmlReader` |
| **8189** | Validación de entrada | [spring-bean-validation](spring-bean-validation) | Jakarta `@Valid` |
| **8190** | Validación de entrada | [aspnet-bean-validation](aspnet-bean-validation) | Data Annotations + FluentValidation |
| **8191** | Security Headers | [spring-security-headers](spring-security-headers) | `SecurityFilterChain` |
| **8192** | Security Headers | [aspnet-security-headers](aspnet-security-headers) | Middleware HTTP |
| **8193** | Ejercicio 1 — SQLi + XSS | [ejercicio1-java](ejercicio1-java) | Búsqueda de productos |
| **8194** | Ejercicio 2 — SQLi + BOLA | [ejercicio2-dotnet](ejercicio2-dotnet) | Perfiles con Dapper |
| **8195** | Logging seguro + errores HTTP | [spring-secure-logging](spring-secure-logging) | SLF4J / `@ControllerAdvice` |
| **8196** | Logging seguro | [aspnet-secure-logging](aspnet-secure-logging) | `ILogger` |
| **8197** | Deserialización insegura | [spring-insecure-deserialization](spring-insecure-deserialization) | `ObjectInputStream` vs Jackson |
| **8198** | Ejercicio integrador | [ejercicio3-integrador](ejercicio3-integrador) | XXE + SQLi + API key + rate limit |

**Requisitos:** Docker Desktop o Podman con `compose`. Puertos **8181–8198** libres en `localhost`.

---

## Cómo ejecutar cualquier demo

```bash
cd <carpeta-de-la-demo>
docker compose up --build
```

Alternativa (el script detecta Podman o Docker):

```bash
chmod +x compose.sh   # solo la primera vez en macOS/Linux
./compose.sh up --build
```

Parar: `docker compose down` o `./compose.sh down`.

Luego abre el `README.md` de esa carpeta para los `curl` o URLs del navegador.

---

## SQL Injection

Consulta **vulnerable** (concatenación) vs **segura** (parametrizada). La respuesta incluye `sqlEjecutado`.

| Carpeta | Puerto |
|---------|--------|
| [spring-sql-injection](spring-sql-injection) | 8181 |
| [aspnet-sql-injection](aspnet-sql-injection) | 8182 |

| Caso | Spring (8181) | .NET (8182) |
|------|---------------|-------------|
| Usuarios (email) | `GET /api/usuarios/vulnerable?email=...` | `GET /api/usuarios/vulnerable?email=...` |
| Productos (`LIKE`) | `GET /api/productos/vulnerable?q=...` | — |
| Usuarios JPA (HQL) | `GET /api/usuarios-jpa/vulnerable?email=...` | — |
| Usuarios EF | — | `GET /api/usuarios-ef/vulnerable?username=...` |

Payload de prueba: `' OR '1'='1`

---

## Cross-Site Scripting (XSS)

Formulario de comentarios: salida sin escapar vs escapada.

| Carpeta | Puerto | Vulnerable | Seguro |
|---------|--------|------------|--------|
| [spring-xss-thymeleaf](spring-xss-thymeleaf) | 8183 | `/comments` | `/secure-comments` |
| [aspnet-xss-razor](aspnet-xss-razor) | 8184 | `/comments` | `/secure-comments` |

Payload en navegador: `<script>alert('XSS')</script>`

---

## Cross-Site Request Forgery (CSRF)

Transferencia bancaria simulada sin protección vs token CSRF / Anti-Forgery.

| Carpeta | Puerto | Vulnerable | Seguro | Atacante |
|---------|--------|------------|--------|----------|
| [spring-csrf](spring-csrf) | 8185 | `/vulnerable` | `/secure` | `/attacker` |
| [aspnet-csrf](aspnet-csrf) | 8186 | `/vulnerable` | `/secure` | `/attacker` |

---

## XML External Entity (XXE)

Parser XML vulnerable vs endurecido. Payloads en `payloads/` de cada demo.

| Carpeta | Puerto |
|---------|--------|
| [spring-xxe](spring-xxe) | 8187 |
| [aspnet-xxe](aspnet-xxe) | 8188 |

| Variante | Endpoint |
|----------|----------|
| ANTES | `POST /api/profile/vulnerable` |
| DESPUÉS | `POST /api/profile/seguro` · `POST /api/profile/seguro-reader` (.NET) |

---

## Validación de entrada

Datos de usuario sin validar vs `@Valid` / Data Annotations / FluentValidation.

| Carpeta | Puerto |
|---------|--------|
| [spring-bean-validation](spring-bean-validation) | 8189 |
| [aspnet-bean-validation](aspnet-bean-validation) | 8190 |

| Variante | Spring (8189) | .NET (8190) |
|----------|---------------|-------------|
| ANTES | `POST /api/users/vulnerable` | `POST /api/users/vulnerable` |
| DESPUÉS | `POST /api/users/seguro` | `POST /api/users/seguro-anotaciones` · `seguro-fluent` |

---

## Security Headers

Respuestas sin headers de defensa vs CSP, X-Frame-Options, HSTS, CORS, etc.

| Carpeta | Puerto |
|---------|--------|
| [spring-security-headers](spring-security-headers) | 8191 |
| [aspnet-security-headers](aspnet-security-headers) | 8192 |

```bash
curl -i http://localhost:8192/api/secure/check
```

---

## Ejercicios de clase

| Ejercicio | Carpeta | Puerto | Vulnerabilidades |
|-----------|---------|--------|------------------|
| **1 · Java** — Búsqueda productos | [ejercicio1-java](ejercicio1-java) | 8193 | SQLi + XSS |
| **2 · .NET** — Perfiles usuario | [ejercicio2-dotnet](ejercicio2-dotnet) | 8194 | SQLi + BOLA/IDOR + validación `bio` |
| **3 · Integrador** — Informes B2B | [ejercicio3-integrador](ejercicio3-integrador) | 8198 | XXE + SQLi + auth + rate limit + PDF |

---

## Deserialización insegura

`ObjectInputStream.readObject()` sin filtro vs JSON con Jackson y tipo conocido.

| Carpeta | Puerto |
|---------|--------|
| [spring-insecure-deserialization](spring-insecure-deserialization) | 8197 |

| Variante | Endpoint |
|----------|----------|
| ANTES | `POST /api/users/vulnerable/deserialize` |
| DESPUÉS | `POST /api/users/seguro/deserialize` |

---

## Logging seguro

No registrar contraseñas, JWT, tarjetas ni CVV en logs; no devolver stack traces al cliente.

| Carpeta | Puerto |
|---------|--------|
| [spring-secure-logging](spring-secure-logging) | 8195 |
| [aspnet-secure-logging](aspnet-secure-logging) | 8196 |

| Variante | Spring (8195) | .NET (8196) |
|----------|---------------|-------------|
| Logging ANTES | `POST /api/auth/vulnerable/login` | `POST /api/checkout/vulnerable` |
| Logging DESPUÉS | `POST /api/auth/seguro/login` | `POST /api/checkout/seguro` |
| Errores HTTP ANTES | `GET /api/orders/vulnerable/{id}` | — |
| Errores HTTP DESPUÉS | `GET /api/orders/seguro/{id}` | — |

---

## Windows sin PowerShell

Cada `README.md` de demo incluye la sección **「Windows — cmd y curl.exe (sin PowerShell)」**
con `docker compose` y, cuando aplica, `curl.exe`.

Los scripts `.ps1` son opcionales.

Los `.sh` del repo usan finales de línea **LF** (`.gitattributes`). Si ves
`env: bash\r: No such file or directory`, convertid con `sed -i '' $'s/\r$//' *.sh` en la
carpeta de la demo.
