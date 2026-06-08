# Demo Rate Limiting — Spring Boot + Bucket4j

Demo sencilla que aplica **rate limiting** (limitación de peticiones) a una API de Spring Boot
usando **Bucket4j** (algoritmo *token-bucket*). Cuando se supera el límite, la API responde
**`429 Too Many Requests`** con la cabecera **`Retry-After`**, tal y como se ve en la diapositiva.

## Arquitectura

```
  cliente (curl / Postman / cmd)
        │  GET /api/datos-sensibles
        ▼
  ┌──────────────────────────────────────────┐
  │  Spring Boot :8181                         │
  │  ┌──────────────┐   tryConsume(1)          │
  │  │ RateLimitFilter ├───────────────┐       │
  │  └──────────────┘                  ▼       │
  │      hay token? ── sí ──▶ Controller (200) │
  │           │                                │
  │           └── no ──▶ 429 + Retry-After      │
  └──────────────────────────────────────────┘
```

- **API** en `http://localhost:8181`.
- Límite por defecto: **5 peticiones por minuto** (la diapositiva usa 100/min; aquí lo bajamos
  para que el `429` sea fácil de provocar en una demo en vivo). Configurable por variables de entorno.

### Endpoints

| Método | Ruta | Rate limit |
|--------|------|------------|
| GET | `/api/datos-sensibles` | Sí (5 req/min por defecto) |

### Cómo funciona

- `RateLimitConfig` crea un `@Bean Bucket` con `Bandwidth.classic(5, Refill.greedy(5, 1 min))`.
- `RateLimitFilter` (un `OncePerRequestFilter` sobre `/api/`) hace `bucket.tryConsume(1)` por
  petición. Si no quedan tokens, devuelve `429` con `Retry-After` (segundos hasta que se
  recargue un token) para que el cliente implemente **backoff exponencial**.

> En esta demo el bucket es **global** (compartido por todos los clientes), igual que en la
> diapositiva. En producción es habitual un bucket **por IP, por usuario o por API key**
> (un `ConcurrentHashMap<String, Bucket>`), y respaldarlo en Redis para que sea distribuido.

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

La primera vez tarda un poco porque compila la app de Spring Boot.

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
curl -i http://localhost:8181/api/datos-sensibles
```

Fíjate en la cabecera `X-Rate-Limit-Remaining`, que va bajando con cada petición.

**2. Superar el límite → `429 Too Many Requests`**

Lanza 10 peticiones seguidas; las primeras 5 devuelven `200` y el resto `429`:

```bash
for i in $(seq 1 10); do
  echo "--- petición $i ---"
  curl -s -o /dev/null -w "HTTP %{http_code}  Retry-After=%header{retry-after}\n" \
    http://localhost:8181/api/datos-sensibles
done
```

Salida esperada (aproximada):

```
--- petición 1 ---
HTTP 200  Retry-After=
...
--- petición 6 ---
HTTP 429  Retry-After=60
```

**3. Esperar y reintentar**

Tras un minuto el bucket se recarga y vuelven a aceptarse peticiones.

### Probar con un límite aún más bajo (opcional)

Para verlo más rápido, baja el límite al levantar:

```bash
RATELIMIT_CAPACITY=2 RATELIMIT_REFILL_TOKENS=2 RATELIMIT_REFILL_PERIOD_SECONDS=20 \
  docker compose up --build
```

---

## Windows — cmd y curl.exe (sin PowerShell)

Abrí **Símbolo del sistema (cmd)** en la carpeta `spring-rate-limit`. Usad `curl.exe`
(incluido en Windows 10/11). No hace falta PowerShell ni los scripts `.ps1`.

### Levantar y parar servicios

```cmd
cd sesion3-lab\spring-rate-limit
docker compose up --build
```

Otra ventana cmd para parar (Ctrl+C en la primera, o):

```cmd
docker compose down
```

### 1. Una petición

```cmd
curl.exe -i http://localhost:8181/api/datos-sensibles
```

### 2. Superar el límite (lanzar 10 peticiones)

```cmd
for /L %i in (1,1,10) do curl.exe -s -o NUL -w "HTTP %%{http_code}\n" http://localhost:8181/api/datos-sensibles
```

Las primeras 5 devuelven `200` y a partir de la 6 verás `429`.

### Resumen de puertos

| Servicio | URL |
|----------|-----|
| API (Spring Boot) | http://localhost:8181 |

---

## Ejecutar en local (sin contenedor)

```bash
cd rate-limit-server
mvn spring-boot:run
```

Para ajustar el límite con Maven:

```bash
RATELIMIT_CAPACITY=3 mvn spring-boot:run
```
