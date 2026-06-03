# Demo Keycloak + Spring Security (OAuth2 Resource Server)

Demo sencilla que levanta **Keycloak** y un **Resource Server de Spring Boot** con Compose
(Podman o Docker Desktop). El resource server valida tokens JWT emitidos por Keycloak y autoriza
por roles, tal y como se ve en la configuración de `SecurityConfig`.

## Arquitectura

```
  cliente (curl / Postman / cmd / Git Bash)
        │  1) pide token (usuario+password)
        ▼
  ┌─────────────┐        2) JWT          ┌──────────────────┐
  │  Keycloak   │ ───────────────────▶   │  Resource Server  │
  │  :8080      │   3) valida firma      │  Spring Boot :8081│
  └─────────────┘   (JWKS) y roles       └──────────────────┘
```

- **Keycloak** en `http://localhost:8080` (consola admin: `admin` / `admin`).
- **Resource Server** en `http://localhost:8081`.

### Realm pre-configurado (`demo`)

| Elemento | Valor |
|----------|-------|
| Realm | `demo` |
| Client | `demo-client` (secret: `demo-secret`, *Direct Access Grants* activado) |
| Roles | `ADMIN`, `USER` |
| Usuario `alice` | password `password` — roles `ADMIN`, `USER` |
| Usuario `bob` | password `password` — rol `USER` |

### Endpoints del Resource Server

| Método | Ruta | Seguridad |
|--------|------|-----------|
| GET | `/api/public/hello` | Público (sin token) |
| GET | `/api/me` | Autenticado (cualquier token válido) |
| GET | `/api/admin/hello` | Solo rol `ADMIN` |

### Compatibilidad

| Entorno | Levantar servicios | Obtener token | Probar endpoints |
|---------|-------------------|---------------|------------------|
| macOS / Linux + **Podman** | `podman compose up --build` | `./get-token.sh` | `curl` (bash) |
| macOS / Linux + **Docker** | `docker compose up --build` | `./get-token.sh` | `curl` (bash) |
| Windows + **Docker Desktop** | `docker compose up --build` | `.\get-token.ps1` o **curl** (cmd) | `curl` o Postman |
| Windows + **Podman Desktop** | `podman compose up --build` | igual que arriba | `curl` o Postman |
| Windows **sin PowerShell** | `docker compose` en **cmd** | `curl.exe` (ver sección abajo) | `curl.exe` |
| Cualquiera (auto-detecta) | `./compose.sh` / `.\compose.ps1` | scripts anteriores | según SO |

---

## Cómo levantarlo

La primera vez tarda un poco porque compila la app de Spring Boot. Cuando veas el resource
server arrancado, ya puedes probar. (El resource server descarga las claves de Keycloak de
forma perezosa en la primera petición autenticada, así que no pasa nada si arranca unos
segundos antes de que Keycloak termine de importar el realm.)

### Podman (macOS / Linux / Windows)

En macOS o Windows con Podman Desktop, asegúrate de que la máquina está en marcha:

```bash
podman machine start   # solo la primera vez o si está parada
```

Luego levanta los servicios:

```bash
podman compose up --build
# alternativa si usas podman-compose:
# podman-compose up --build
```

Parar y limpiar:

```bash
podman compose down
podman compose down --rmi local   # también elimina imágenes locales
```

### Docker Desktop (macOS / Linux / Windows)

Asegúrate de que **Docker Desktop** está en ejecución y luego:

```bash
docker compose up --build
```

Parar y limpiar:

```bash
docker compose down
docker compose down --rmi local   # también elimina imágenes locales
```

En **Windows**, en **Símbolo del sistema (cmd)** — no hace falta PowerShell:

```cmd
docker compose up --build
docker compose down
```

(O con PowerShell, si lo tenéis permitido: `docker compose up --build`.)

### Script de ayuda (detecta Podman o Docker)

**macOS / Linux (bash):**

```bash
chmod +x compose.sh get-token.sh
./compose.sh up --build
./compose.sh down
```

**Windows con PowerShell** (opcional):

```powershell
.\compose.ps1 up --build
.\compose.ps1 down
```

> En Windows sin PowerShell, usad la sección [Windows — cmd y curl.exe](#windows--cmd-y-curlexe-sin-powershell).

> El script de bash prioriza Podman; el de PowerShell prioriza Docker Desktop (habitual en Windows).

---

## Cómo probar

### macOS / Linux (bash)

**1. Endpoint público (sin token)**

```bash
curl http://localhost:8081/api/public/hello
```

**2. Conseguir un token**

```bash
TOKEN=$(./get-token.sh alice password)   # alice = ADMIN
echo "$TOKEN"
```

O directamente con curl:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/realms/demo/protocol/openid-connect/token \
  -d grant_type=password \
  -d client_id=demo-client \
  -d client_secret=demo-secret \
  -d username=alice \
  -d password=password | jq -r .access_token)
```

**3. Endpoint autenticado**

```bash
curl http://localhost:8081/api/me -H "Authorization: Bearer $TOKEN"
```

**4. Endpoint de admin**

Con `alice` (ADMIN) → **200 OK**:

```bash
curl http://localhost:8081/api/admin/hello -H "Authorization: Bearer $TOKEN"
```

Con `bob` (solo USER) → **403 Forbidden**:

```bash
TOKEN_BOB=$(./get-token.sh bob password)
curl -i http://localhost:8081/api/admin/hello -H "Authorization: Bearer $TOKEN_BOB"
```

En **Windows** sin PowerShell, seguid la sección [Windows — cmd y curl.exe](#windows--cmd-y-curlexe-sin-powershell).

---

## Windows — cmd y curl.exe (sin PowerShell)

Abrí **Símbolo del sistema (cmd)** en la carpeta `spring-security`. Usad `curl.exe` (incluido en Windows 10/11).
No hace falta PowerShell ni los scripts `.ps1`.

### Levantar y parar servicios

```cmd
cd sesion2-lab\spring-security
docker compose up --build
```

Otra ventana cmd para parar (Ctrl+C en la primera, o):

```cmd
docker compose down
```

### 1. Endpoint público

```cmd
curl.exe http://localhost:8081/api/public/hello
```

### 2. Obtener token (Keycloak)

Guardad la respuesta en un fichero y copiad el valor de `access_token` (sin comillas):

```cmd
curl.exe -s -X POST http://localhost:8080/realms/demo/protocol/openid-connect/token -H "Content-Type: application/x-www-form-urlencoded" -d "grant_type=password" -d "client_id=demo-client" -d "client_secret=demo-secret" -d "username=alice" -d "password=password" -o token.json
notepad token.json
```

Definid la variable con el token copiado (una sola línea, sin espacios):

```cmd
set TOKEN=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

Usuario **bob** (solo rol USER): cambiad `username=bob` en el paso 2.

### 3. Endpoint autenticado

```cmd
curl.exe http://localhost:8081/api/me -H "Authorization: Bearer %TOKEN%"
```

### 4. Endpoint admin

Con token de **alice** (ADMIN) → **200 OK**:

```cmd
curl.exe http://localhost:8081/api/admin/hello -H "Authorization: Bearer %TOKEN%"
```

Con **bob**, repetid el paso 2 con `username=bob`, asignad `set TOKEN=...` y volved a llamar a `/api/admin/hello` → debe devolver **403**.

### Resumen de puertos

| Servicio | URL |
|----------|-----|
| Keycloak | http://localhost:8080 |
| Resource Server | http://localhost:8081 |

---

## Cómo funciona la validación

- El resource server descarga las claves públicas (JWKS) de Keycloak por la red interna del
  compose (`http://keycloak:8080`), pero valida que el `iss` del token sea
  `http://localhost:8080/realms/demo`, que es la URL por la que los clientes piden el token.
  Esto evita el clásico problema de *issuer mismatch* entre `localhost` y el nombre del servicio.
- Los **roles de realm** de Keycloak viajan en el claim `realm_access.roles`. La clase
  `SecurityConfig` los convierte a authorities `ROLE_*` para que funcione `hasRole("ADMIN")`.

## Ejecutar el Resource Server en local (sin contenedor)

Levanta solo Keycloak y ejecuta la app con Maven:

**Podman:**

```bash
podman compose up keycloak
cd resource-server
mvn spring-boot:run
```

**Docker Desktop:**

```bash
docker compose up keycloak
cd resource-server
mvn spring-boot:run
```

**Windows (cmd o PowerShell):**

```cmd
docker compose up keycloak
cd resource-server
mvn spring-boot:run
```

Los valores por defecto de `application.yml` ya apuntan a `http://localhost:8080`.
