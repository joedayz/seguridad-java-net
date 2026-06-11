# Demo Security Headers — Spring Security

Comparación de respuestas HTTP **sin headers de seguridad** vs **configuración endurecida**
(de la diapositiva «Configuración segura · Spring Security»).

| Variante | URL |
|----------|-----|
| **ANTES — sin headers** | `GET /api/insecure/check` |
| **DESPUÉS — seguro** | `GET /api/secure/check` |
| Inicio | http://localhost:8191/ |

Puerto: **8191**

---

## Configuración segura (`SecurityConfig.java`)

```java
http.headers(headers -> headers
    .contentTypeOptions(withDefaults())
    .frameOptions(frame -> frame.deny())
    .httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31_536_000))
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'; script-src 'self'; object-src 'none'")))
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```

| Header / opcion | Protege contra |
|-----------------|----------------|
| `X-Content-Type-Options: nosniff` | MIME sniffing |
| `X-Frame-Options: DENY` | Clickjacking |
| `Strict-Transport-Security` | Downgrade HTTP (solo HTTPS) |
| `Content-Security-Policy` | XSS, inyeccion de recursos |
| CSRF token en cookie | CSRF en apps con sesion/cookie |
| `STATELESS` | APIs REST con JWT (sin sesion servidor) |

---

## Cómo levantarlo

```bash
docker compose up --build
```

---

## Cómo probar

```bash
# Sin headers de seguridad
curl -i http://localhost:8191/api/insecure/check

# Con headers (fijate en X-Frame-Options, Content-Security-Policy, Set-Cookie XSRF-TOKEN...)
curl -i http://localhost:8191/api/secure/check
```

> **HSTS** solo se envia en conexiones HTTPS. En `http://localhost` no aparecera; el resto si.
