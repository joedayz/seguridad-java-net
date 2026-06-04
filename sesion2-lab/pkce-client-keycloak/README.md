# Demo Angular + PKCE + Keycloak (Ejercicio 2)

Cliente **SPA Angular** que obtiene tokens con **Authorization Code + PKCE** (S256) contra Keycloak y llama al **Resource Server** Spring Boot con `Authorization: Bearer`.

Completa el **Ejercicio 2** de la presentación. Los ejercicios 1 y 3 están cubiertos por el resource server y los roles de realm (`ADMIN` / `USER`).

## Arquitectura

```
  Navegador (Angular :8093)
        │  1) redirect + PKCE (code_challenge S256)
        ▼
  Keycloak :8092  ──authorization code──▶  Angular
        │  2) canje code + code_verifier
        ▼
  access_token (JWT)
        │  3) Bearer en llamadas HTTP
        ▼
  Resource Server Spring Boot :8088
```

## URLs

| Servicio | URL | Notas |
|----------|-----|--------|
| **SPA Angular** | http://localhost:8093 | Abre esta URL para la demo |
| **Keycloak** | http://localhost:8092 | Realm `demo` |
| **Consola admin** | http://localhost:8092/admin | `admin` / `admin` |
| **Resource Server** | http://localhost:8088 | API REST |

### Endpoints del Resource Server

| Método | Ruta | Seguridad |
|--------|------|-----------|
| GET | `/api/public/hello` | Público |
| GET | `/api/me` | JWT válido |
| GET | `/api/admin/hello` | Rol `ADMIN` |

### Cliente OIDC en Keycloak

| Campo | Valor |
|-------|--------|
| Client ID | `angular-pkce-client` |
| Tipo | Público (SPA) |
| Flujo | Standard flow + **PKCE S256** |
| Redirect URI | `http://localhost:8093/*` |

### Usuarios de prueba

| Usuario | Password | Roles | `/api/admin/hello` |
|---------|----------|-------|---------------------|
| `alice` | `password` | ADMIN, USER | 200 |
| `bob` | `password` | USER | 403 |

---

## Cómo levantarlo

```bash
chmod +x compose.sh
./compose.sh up --build
```

Espera ~30 s a que Keycloak importe el realm. Abre **http://localhost:8093**.

**Windows (cmd):**

```cmd
cd sesion2-lab\pkce-client-keycloak
docker compose up --build
```

---

## Cómo probar (en el navegador)

1. Abre http://localhost:8093
2. Pulsa **Iniciar sesion en Keycloak** (redirige a Keycloak con PKCE).
3. Inicia sesion como `alice` / `password`.
4. Vuelves a la SPA con el token guardado.
5. Prueba los botones:
   - **GET /api/public/hello** — funciona sin login.
   - **GET /api/me** — muestra usuario y authorities.
   - **GET /api/admin/hello** — OK con `alice`, 403 con `bob`.
6. Cierra sesion y repite con `bob` para ver el 403 en admin.

---

## Desarrollo local (sin Docker en Angular)

Con Keycloak y el API ya levantados por compose:

```bash
cd angular-client
npm install
npm start
```

La SPA queda en http://localhost:8093 (misma config en `auth.config.ts`).

---

## Código destacado

**PKCE + OIDC** (`angular-oauth2-oidc`):

```typescript
// auth.config.ts
responseType: 'code',  // Authorization Code
// PKCE S256 lo aplica la libreria por defecto en clientes publicos
```

**Bearer en llamadas al API** (`auth.interceptor.ts`):

```typescript
setHeaders: { Authorization: `Bearer ${token}` }
```

---

## Comparación con otras demos

| Demo | Cliente | Flujo token |
|------|---------|-------------|
| `spring-security` | `curl` / script | Password grant (ROPC) |
| `entra-aspnet` | script | Device code |
| **`pkce-client-keycloak`** | **Angular SPA** | **Authorization Code + PKCE** |

## Puertos (no chocan con otras demos)

| Demo | Keycloak | API | Cliente |
|------|----------|-----|---------|
| spring-security | 8080 | 8081 | — |
| method-security-keycloak | 8090 | 8086 | — |
| aspnet-policies-keycloak | 8091 | 8087 | — |
| **pkce-client-keycloak** | **8092** | **8088** | **8093** |
