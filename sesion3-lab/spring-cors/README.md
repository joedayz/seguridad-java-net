# Demo CORS — Spring Security

Demo que aplica **CORS seguro** en una API de Spring Boot con **Spring Security**. Solo los
orígenes explícitamente permitidos pueden consumir la API con credenciales.

> **Nunca en producción:** `allowedOrigins("*")` + `allowCredentials(true)`. Esa combinación es
> inválida y peligrosa: expone tokens de sesión a sitios maliciosos.

## Arquitectura

```
  cliente (origen permitido o bloqueado)
        │  GET/POST /api/datos  (+ cabecera Origin)
        ▼
  ┌──────────────────────────────────────────┐
  │  Spring Boot + Security :8183             │
  │  CorsConfig (orígenes explícitos)         │
  │     origen permitido? ── sí ──▶ 200 + ACAO │
  │            │                               │
  │            └── no ──▶ sin Access-Control-*  │
  └──────────────────────────────────────────┘
```

- **API** en `http://localhost:8183`.
- **Clientes** (nginx + HTML estático): `http://localhost:8193` (app), `http://localhost:8195`
  (admin), `http://localhost:8196` (origen **no** permitido, para comparar en el navegador).
- Orígenes permitidos por defecto: `http://localhost:8193`, `http://localhost:8195`
  (en la diapositiva: `https://app.midominio.com`, `https://admin.midominio.com`).
- Métodos: `GET`, `POST`. Cabeceras: `Authorization`, `Content-Type`. `maxAge`: 3600 s.

### Endpoints

| Método | Ruta | CORS |
|--------|------|------|
| GET | `/api/datos` | Sí |
| POST | `/api/datos` | Sí |

### Cómo funciona

- `CorsConfig` define un `CorsConfigurationSource` con orígenes explícitos, métodos limitados,
  `allowCredentials(true)` y `maxAge`.
- `SecurityConfig` activa CORS con `.cors(Customizer.withDefaults())`.

### Compatibilidad

| Entorno | Levantar servicios | Probar |
|---------|-------------------|--------|
| macOS / Linux + **Podman** | `podman compose up --build` | `curl` (bash) |
| macOS / Linux + **Docker** | `docker compose up --build` | `curl` (bash) |
| Windows + **Docker Desktop** | `docker compose up --build` | `curl.exe` (cmd) |
| Cualquiera (auto-detecta) | `./compose.sh` / `.\compose.ps1` | según SO |

---

## Cómo levantarlo

```bash
podman compose up --build
# o
docker compose up --build
```

---

## Cómo probar

### En el navegador (recomendado en clase)

Con `podman compose up --build` levantados API y clientes:

1. Abrí **http://localhost:8193** → pulsa **GET /api/datos** → debe mostrar JSON (CORS OK).
2. Abrí **http://localhost:8196** → mismo botón → el navegador **bloquea** la petición
   (error CORS en consola / mensaje en pantalla).
3. Opcional: **http://localhost:8195** también está permitido (segundo origen de la diapositiva).

> `curl` no aplica CORS; sirve para ver cabeceras, pero la demo visual es en el navegador.

### 1. Preflight (OPTIONS) — origen permitido → cabeceras CORS

```bash
curl -i -X OPTIONS http://localhost:8183/api/datos \
  -H "Origin: http://localhost:8193" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Content-Type"
```

Debes ver `Access-Control-Allow-Origin: http://localhost:8193` y
`Access-Control-Allow-Credentials: true`.

### 2. GET con origen permitido → 200

```bash
curl -i http://localhost:8183/api/datos \
  -H "Origin: http://localhost:8193"
```

### 3. Origen no permitido → sin cabeceras CORS

```bash
curl -i -X OPTIONS http://localhost:8183/api/datos \
  -H "Origin: http://evil.com" \
  -H "Access-Control-Request-Method: GET"
```

No debe aparecer `Access-Control-Allow-Origin: http://evil.com`.

### 4. Método no permitido (PUT) → rechazado en preflight

```bash
curl -i -X OPTIONS http://localhost:8183/api/datos \
  -H "Origin: http://localhost:8193" \
  -H "Access-Control-Request-Method: PUT"
```

---

## Windows — cmd y curl.exe (sin PowerShell)

```cmd
cd sesion3-lab\spring-cors
docker compose up --build
```

```cmd
curl.exe -i -X OPTIONS http://localhost:8183/api/datos -H "Origin: http://localhost:8193" -H "Access-Control-Request-Method: GET"
```

| Servicio | URL |
|----------|-----|
| API (Spring Boot) | http://localhost:8183 |
| Cliente app (permitido) | http://localhost:8193 |
| Cliente admin (permitido) | http://localhost:8195 |
| Cliente bloqueado | http://localhost:8196 |

---

## Ejecutar en local (sin contenedor)

```bash
cd cors-server
mvn spring-boot:run
```
