# Demo CSRF — Spring Security (antes / después)

Demo de **Cross-Site Request Forgery (CSRF)** y su mitigación en Spring Boot:

| Variante | URL | Protección |
|----------|-----|------------|
| **ANTES — vulnerable** | http://localhost:8185/vulnerable | CSRF **deshabilitado** |
| **DESPUÉS — seguro** | http://localhost:8185/secure | Token CSRF + `SameSite=Lax` |
| **Sitio malicioso** | http://localhost:8185/attacker | Simula `evil.com` |

Escenario: banco simulado con **1000 EUR** en sesión. Un formulario de transferencia puede
ser forzado desde `/attacker` si no hay protección CSRF.

## Mitigaciones (diapositiva)

### Token sincronizador

El servidor genera un token aleatorio único por sesión. El cliente debe incluirlo en cada
petición de mutación (POST). El sitio malicioso **no puede leer** este token (política SOP).

Configuración en `SecurityConfig`:

```java
http.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
```

El formulario seguro incluye:

```html
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
```

### SameSite cookie

`SameSite=Lax` en la cookie de sesión (`application.yml`) impide que el navegador envíe la
cookie en peticiones cross-site, eliminando el vector de ataque a nivel de cookie.

```yaml
server:
  servlet:
    session:
      cookie:
        same-site: lax
```

> Para APIs REST stateless con JWT en `Authorization`, deshabilitar CSRF puede ser aceptable:
> `.csrf(AbstractHttpConfigurer::disable)`

---

## Cómo levantarlo

```bash
docker compose up --build
# o: ./compose.sh up --build
```

| Servicio | URL |
|----------|-----|
| Inicio | http://localhost:8185/ |

---

## Cómo probar (la demo del antes / después)

1. Abre **Vulnerable** → http://localhost:8185/vulnerable (saldo 1000 EUR).
2. En otra pestaña abre **Sitio malicioso** → pulsa *Ejecutar ataque CSRF (vulnerable)*.
3. Vuelve a **Vulnerable** → el saldo bajó a **500 EUR** sin que confirmaras nada.
4. Pulsa *Restablecer saldo*.
5. Abre **Seguro** → http://localhost:8185/secure.
6. Repite el ataque desde `/attacker` contra la versión segura → **403 Forbidden** (sin token CSRF).

---

## Ejecutar en local

```bash
cd csrf-server
mvn spring-boot:run
```

---

## Regla de oro

- En aplicaciones web con sesión/cookies: **deja CSRF habilitado** (es el default en Spring Security).
- Incluye el **token en formularios** o envía `X-XSRF-TOKEN` desde SPA.
- Configura **`SameSite=Lax`** (o `Strict`) en cookies de sesión.
- Solo desactiva CSRF en APIs **stateless** autenticadas por token (JWT), no por cookie.
