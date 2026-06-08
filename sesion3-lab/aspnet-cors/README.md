# Demo CORS — ASP.NET Core (políticas por entorno)

Demo que aplica **CORS** en ASP.NET Core con **políticas con nombre** y selección por entorno,
tal y como se ve en la diapositiva. En lugar de una política global permisiva, se definen
`PoliticaProduccion` y `PoliticaDesarrollo`.

## Arquitectura

```
  cliente (origen permitido o bloqueado)
        │  GET/POST /api/datos  (+ cabecera Origin)
        ▼
  ┌────────────────────────────────────────────┐
  │  ASP.NET Core :8184                         │
  │  UseCors(PoliticaDesarrollo | Produccion)   │
  │     origen permitido? ── sí ──▶ 200 + ACAO   │
  │            │                                 │
  │            └── no ──▶ sin Access-Control-*   │
  └────────────────────────────────────────────┘
```

- **API** en `http://localhost:8184`.
- **Clientes**: `http://localhost:8194` (desarrollo, permitido), `http://localhost:8197`
  (origen no permitido, para comparar en el navegador).
- **Development** (por defecto en la demo): `PoliticaDesarrollo` → origen `http://localhost:8194`.
- **Production**: `PoliticaProduccion` → `https://app.midominio.com`, métodos `GET`/`POST`/`PUT`,
  cabeceras `Authorization`/`Content-Type`, credenciales, preflight 3600 s.

### Endpoints

| Método | Ruta | CORS |
|--------|------|------|
| GET | `/api/datos` | Sí (política activa) |
| POST | `/api/datos` | Sí (política activa) |

### Cómo funciona

- `builder.Services.AddCors(...)` registra las dos políticas.
- `app.UseCors(env.IsProduction() ? "PoliticaProduccion" : "PoliticaDesarrollo")` aplica la
  política según el entorno.

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

El contenedor arranca con `ASPNETCORE_ENVIRONMENT=Development` (política de desarrollo).

---

## Cómo probar

### En el navegador (recomendado en clase)

1. Abrí **http://localhost:8194** → **GET /api/datos** → JSON (CORS OK con `PoliticaDesarrollo`).
2. Abrí **http://localhost:8197** → mismo botón → bloqueo CORS en el navegador.

### 1. Preflight — origen de desarrollo permitido

```bash
curl -i -X OPTIONS http://localhost:8184/api/datos \
  -H "Origin: http://localhost:8194" \
  -H "Access-Control-Request-Method: GET"
```

Debes ver `Access-Control-Allow-Origin: http://localhost:8194`.

### 2. GET con origen permitido → 200

```bash
curl -i http://localhost:8184/api/datos \
  -H "Origin: http://localhost:8194"
```

### 3. Origen no permitido

```bash
curl -i -X OPTIONS http://localhost:8184/api/datos \
  -H "Origin: http://evil.com" \
  -H "Access-Control-Request-Method: GET"
```

### 4. Probar política de producción (opcional)

```bash
ASPNETCORE_ENVIRONMENT=Production docker compose up --build
```

```bash
curl -i -X OPTIONS http://localhost:8184/api/datos \
  -H "Origin: https://app.midominio.com" \
  -H "Access-Control-Request-Method: PUT" \
  -H "Access-Control-Request-Headers: Authorization"
```

---

## Windows — cmd y curl.exe (sin PowerShell)

```cmd
cd sesion3-lab\aspnet-cors
docker compose up --build
```

```cmd
curl.exe -i -X OPTIONS http://localhost:8184/api/datos -H "Origin: http://localhost:8194" -H "Access-Control-Request-Method: GET"
```

| Servicio | URL |
|----------|-----|
| API (ASP.NET Core) | http://localhost:8184 |
| Cliente desarrollo (permitido) | http://localhost:8194 |
| Cliente bloqueado | http://localhost:8197 |

---

## Ejecutar en local (sin contenedor)

```bash
cd cors-demo
dotnet run
```
