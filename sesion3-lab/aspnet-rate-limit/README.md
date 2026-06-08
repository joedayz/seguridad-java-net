# Demo Rate Limiting — ASP.NET Core 7+ (middleware nativo)

Demo sencilla que aplica **rate limiting** a una API de ASP.NET Core usando el **middleware
nativo** disponible desde **.NET 7** (`Microsoft.AspNetCore.RateLimiting`). **No requiere
paquetes NuGet externos**: forma parte del framework compartido. Cuando se supera el límite,
la API responde **`429 Too Many Requests`** con la cabecera **`Retry-After`**, tal y como se ve
en la diapositiva.

## Arquitectura

```
  cliente (curl / Postman / cmd)
        │  GET /api/datos-sensibles
        ▼
  ┌────────────────────────────────────────────┐
  │  ASP.NET Core :8182                          │
  │  app.UseRateLimiter()  ── política "api-policy"
  │     dentro de la ventana? ── sí ──▶ Controller (200)
  │            │                                 │
  │            └── no ──▶ 429 + Retry-After        │
  └────────────────────────────────────────────┘
```

- **API** en `http://localhost:8182`.
- Política `api-policy`: **FixedWindow** de **5 peticiones / 60 s** (la diapositiva usa
  `PermitLimit = 100`; aquí lo bajamos para que el `429` sea fácil de provocar). Configurable
  por variables de entorno.

### Endpoints

| Método | Ruta | Rate limit |
|--------|------|------------|
| GET | `/api/datos-sensibles` | Sí — `[EnableRateLimiting("api-policy")]` |
| GET | `/api/publico` | No (para comparar) |

### Cómo funciona

- En `Program.cs`, `builder.Services.AddRateLimiter(...)` registra un `AddFixedWindowLimiter`
  con `PermitLimit`, `Window`, `QueueProcessingOrder.OldestFirst` y `QueueLimit`.
- `options.RejectionStatusCode = 429` y un handler `OnRejected` añade la cabecera
  `Retry-After` (para **backoff exponencial** del cliente).
- `app.UseRateLimiter()` activa el middleware, y el controlador marca el endpoint con
  `[EnableRateLimiting("api-policy")]`.

> En la diapositiva `QueueLimit = 10` (encola hasta 10 peticiones antes de rechazar). Aquí
> usamos `QueueLimit = 0` para que el exceso se rechace de inmediato y el `429` se vea claro.

### Compatibilidad

| Entorno | Levantar servicios | Probar |
|---------|-------------------|--------|
| macOS / Linux + **Podman** | `podman compose up --build` | `curl` (bash) |
| macOS / Linux + **Docker** | `docker compose up --build` | `curl` (bash) |
| Windows + **Docker Desktop** | `docker compose up --build` | `curl.exe` (cmd) o Postman |
| Windows **sin PowerShell** | `docker compose` en **cmd** | `curl.exe` (ver sección abajo) |
| Cualquiera (auto-detecta) | `./compose.sh` / `.\compose.ps1` | según SO |

---

## Cómo levantarlo

### Podman (macOS / Linux / Windows)

```bash
podman machine start   # solo la primera vez o si está parada
podman compose up --build
```

Parar y limpiar:

```bash
podman compose down
podman compose down --rmi local
```

### Docker Desktop (macOS / Linux / Windows)

```bash
docker compose up --build
docker compose down
```

### Script de ayuda (detecta Podman o Docker)

**macOS / Linux (bash):**

```bash
chmod +x compose.sh
./compose.sh up --build
./compose.sh down
```

**Windows con PowerShell** (opcional):

```powershell
.\compose.ps1 up --build
.\compose.ps1 down
```

---

## Cómo probar

### macOS / Linux (bash)

**1. Una petición dentro del límite → `200 OK`**

```bash
curl -i http://localhost:8182/api/datos-sensibles
```

**2. Superar el límite → `429 Too Many Requests`**

```bash
for i in $(seq 1 10); do
  echo "--- petición $i ---"
  curl -s -o /dev/null -w "HTTP %{http_code}  Retry-After=%header{retry-after}\n" \
    http://localhost:8182/api/datos-sensibles
done
```

Las primeras 5 devuelven `200`; a partir de la 6, `429` con `Retry-After`.

**3. Endpoint sin límite (responde siempre)**

```bash
curl -i http://localhost:8182/api/publico
```

### Probar con un límite aún más bajo (opcional)

```bash
RateLimit__PermitLimit=2 RateLimit__WindowSeconds=20 docker compose up --build
```

---

## Windows — cmd y curl.exe (sin PowerShell)

Abrí **Símbolo del sistema (cmd)** en la carpeta `aspnet-rate-limit`. Usad `curl.exe`
(incluido en Windows 10/11). No hace falta PowerShell ni los scripts `.ps1`.

### Levantar y parar servicios

```cmd
cd sesion3-lab\aspnet-rate-limit
docker compose up --build
```

Otra ventana cmd para parar (Ctrl+C en la primera, o):

```cmd
docker compose down
```

### 1. Una petición

```cmd
curl.exe -i http://localhost:8182/api/datos-sensibles
```

### 2. Superar el límite (lanzar 10 peticiones)

```cmd
for /L %i in (1,1,10) do curl.exe -s -o NUL -w "HTTP %%{http_code}\n" http://localhost:8182/api/datos-sensibles
```

Las primeras 5 devuelven `200` y a partir de la 6 verás `429`.

### Resumen de puertos

| Servicio | URL |
|----------|-----|
| API (ASP.NET Core) | http://localhost:8182 |

---

## Ejecutar en local (sin contenedor)

Necesitas el **SDK de .NET 8**:

```bash
cd ratelimit-demo
dotnet run
```

La API arranca en `http://localhost:8182` (ver `Properties/launchSettings.json`).
