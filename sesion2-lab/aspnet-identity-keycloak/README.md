# Demo ASP.NET Core + Keycloak (OAuth2 Resource Server)

Variante de la demo `../aspnet-identity` en la que **la autenticacion ya no la gestiona ASP.NET
Core Identity**, sino un **Keycloak** externo. La app .NET se convierte en un **Resource Server**:
no almacena usuarios ni emite tokens, solo **valida** los JWT que emite Keycloak y autoriza por
roles. Es el equivalente en .NET de la demo `../spring-security`.

## Diferencia con `aspnet-identity`

| Aspecto | `aspnet-identity` | `aspnet-identity-keycloak` (esta demo) |
|---------|-------------------|----------------------------------------|
| IdP / auth | ASP.NET Identity integrado en la app | Keycloak externo |
| Almacen de usuarios | PostgreSQL (tablas de Identity) | Keycloak (realm `demo`) |
| Quien emite el token | La propia API (`JwtTokenService`, HMAC) | Keycloak (RSA, JWKS) |
| Login / registro / confirmacion | Endpoints `/api/auth/*` en la API | Los gestiona Keycloak |
| Validacion del token | Clave simetrica compartida | Firma RSA verificada via JWKS de Keycloak |
| Roles | `Admin`, `User` (claims propios) | `ADMIN`, `USER` (claim `realm_access.roles`) |
| Puerto API | 8082 | 8085 |

## Arquitectura

```
  cliente (curl / Postman / cmd / Git Bash)
        │  1) pide token (usuario+password)
        ▼
  ┌─────────────┐        2) JWT          ┌──────────────────────┐
  │  Keycloak   │ ───────────────────▶   │  ASP.NET Resource Srv │
  │  :8080      │   3) valida firma      │  .NET :8085           │
  └─────────────┘   (JWKS) y roles       └──────────────────────┘
```

- **Keycloak** en `http://localhost:8080` (consola admin: `admin` / `admin`).
- **API .NET** en `http://localhost:8085`.

### Realm pre-configurado (`demo`)

| Elemento | Valor |
|----------|-------|
| Realm | `demo` |
| Client | `demo-client` (secret: `demo-secret`, *Direct Access Grants* activado) |
| Roles | `ADMIN`, `USER` |
| Usuario `alice` | password `password` — roles `ADMIN`, `USER` |
| Usuario `bob` | password `password` — rol `USER` |

### Endpoints de la API

| Metodo | Ruta | Seguridad |
|--------|------|-----------|
| GET | `/api/public/hello` | Publico (sin token) |
| GET | `/api/me` | Autenticado (cualquier token valido) |
| GET | `/api/admin/hello` | Solo rol `ADMIN` |

### Compatibilidad

| Entorno | Levantar servicios | Obtener token | Probar endpoints |
|---------|-------------------|---------------|------------------|
| macOS / Linux + **Podman** | `podman compose up --build` | `./get-token.sh` | `curl` (bash) |
| macOS / Linux + **Docker** | `docker compose up --build` | `./get-token.sh` | `curl` (bash) |
| Windows + **Docker Desktop** | `docker compose up --build` | `.\get-token.ps1` o **curl** (cmd) | `curl` o Postman |
| Windows + **Podman Desktop** | `podman compose up --build` | igual que arriba | `curl` o Postman |
| Windows **sin PowerShell** | `docker compose` en **cmd** | `curl.exe` (ver seccion abajo) | `curl.exe` |
| Cualquiera (auto-detecta) | `./compose.sh` / `.\compose.ps1` | scripts anteriores | segun SO |

---

## Como levantarlo

La primera vez tarda un poco porque compila la app .NET. La API descarga las claves de Keycloak
de forma perezosa en la primera peticion autenticada, asi que no pasa nada si arranca unos
segundos antes de que Keycloak termine de importar el realm.

### Podman (macOS / Linux / Windows)

En macOS o Windows con Podman Desktop, asegurate de que la maquina esta en marcha:

```bash
podman machine start   # solo la primera vez o si esta parada
podman compose up --build
# alternativa si usas podman-compose:
# podman-compose up --build
```

Parar y limpiar:

```bash
podman compose down
podman compose down --rmi local   # tambien elimina imagenes locales
```

### Docker Desktop (macOS / Linux / Windows)

```bash
docker compose up --build
```

Parar y limpiar:

```bash
docker compose down
docker compose down --rmi local
```

En **Windows (cmd)** — sin PowerShell:

```cmd
docker compose up --build
docker compose down
```

### Script de ayuda

**macOS / Linux (bash):**

```bash
chmod +x compose.sh get-token.sh
./compose.sh up --build
./compose.sh down
```

**Windows con PowerShell** (opcional): `.\compose.ps1 up --build`

> El script de bash prioriza Podman; el de PowerShell prioriza Docker Desktop (habitual en Windows).

---

## Como probar

### macOS / Linux (bash)

**1. Endpoint publico (sin token)**

```bash
curl http://localhost:8085/api/public/hello
```

**2. Conseguir un token de Keycloak**

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
curl http://localhost:8085/api/me -H "Authorization: Bearer $TOKEN"
```

**4. Endpoint de admin**

Con `alice` (ADMIN) → **200 OK**:

```bash
curl http://localhost:8085/api/admin/hello -H "Authorization: Bearer $TOKEN"
```

Con `bob` (solo USER) → **403 Forbidden**:

```bash
TOKEN_BOB=$(./get-token.sh bob password)
curl -i http://localhost:8085/api/admin/hello -H "Authorization: Bearer $TOKEN_BOB"
```

En **Windows** sin PowerShell, seguid la seccion [Windows — cmd y curl.exe](#windows--cmd-y-curlexe-sin-powershell).

---

## Windows — cmd y curl.exe (sin PowerShell)

Abri **cmd** en la carpeta `aspnet-identity-keycloak`. Usad `curl.exe`; no hace falta PowerShell.

### Levantar y parar servicios

```cmd
cd sesion2-lab\aspnet-identity-keycloak
docker compose up --build
```

```cmd
docker compose down
```

### 1. Endpoint publico

```cmd
curl.exe http://localhost:8085/api/public/hello
```

### 2. Obtener token (Keycloak)

Guardad la respuesta en un fichero y copiad el valor de `access_token` (sin comillas):

```cmd
curl.exe -s -X POST http://localhost:8080/realms/demo/protocol/openid-connect/token -H "Content-Type: application/x-www-form-urlencoded" -d "grant_type=password" -d "client_id=demo-client" -d "client_secret=demo-secret" -d "username=alice" -d "password=password" -o token.json
notepad token.json
```

Definid la variable con el token copiado (una sola linea, sin espacios):

```cmd
set TOKEN=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

Usuario **bob** (solo rol USER): cambiad `username=bob` en el paso 2.

### 3. Endpoint autenticado

```cmd
curl.exe http://localhost:8085/api/me -H "Authorization: Bearer %TOKEN%"
```

### 4. Endpoint admin

Con token de **alice** (ADMIN) → **200 OK**:

```cmd
curl.exe http://localhost:8085/api/admin/hello -H "Authorization: Bearer %TOKEN%"
```

Con **bob**, repetid el paso 2 con `username=bob`, asignad `set TOKEN=...` y volved a llamar a
`/api/admin/hello` → debe devolver **403**.

### Resumen de puertos

| Servicio | URL |
|----------|-----|
| Keycloak | http://localhost:8080 |
| API .NET | http://localhost:8085 |

---

## Como funciona la validacion

- La API descarga la metadata OpenID Connect y las claves publicas (JWKS) de Keycloak por la red
  interna del compose (`http://keycloak:8080`, variable `Keycloak__MetadataAddress`), pero valida
  que el `iss` del token sea `http://localhost:8080/realms/demo` (variable `Keycloak__Issuer`), que
  es la URL por la que los clientes piden el token. Esto evita el clasico problema de
  *issuer mismatch* entre `localhost` y el nombre del servicio.
- Los **roles de realm** de Keycloak viajan en el claim `realm_access.roles`. En `Program.cs`, el
  evento `OnTokenValidated` los convierte en claims de rol (`ClaimTypes.Role`) para que funcione
  `[Authorize(Roles = "ADMIN")]`.
- Se usa `MapInboundClaims = false` para conservar los nombres de claim originales de Keycloak
  (`preferred_username`, `email`, `realm_access`) en lugar de remapearlos a las URIs largas de .NET.

## Ejecutar la API en local (sin contenedor de la app)

Levanta solo Keycloak y ejecuta la app con `dotnet run`:

**Podman:**

```bash
podman compose up keycloak
cd identity-demo
dotnet run
```

**Docker Desktop:**

```bash
docker compose up keycloak
cd identity-demo
dotnet run
```

**Windows (cmd o PowerShell):**

```cmd
docker compose up keycloak
cd identity-demo
dotnet run
```

Los valores por defecto de `appsettings.json` ya apuntan a `http://localhost:8080`.

## Comparacion con las otras demos de la sesion

| Aspecto | `spring-security` | `aspnet-identity` | `aspnet-identity-keycloak` |
|---------|-------------------|-------------------|----------------------------|
| Framework | Spring Boot | ASP.NET Core | ASP.NET Core |
| IdP / auth | Keycloak | Identity integrado | Keycloak |
| Token | JWT de Keycloak | JWT propio (HMAC) | JWT de Keycloak |
| Puerto API | 8081 | 8082 | 8085 |
| Roles | `ADMIN`, `USER` | `Admin`, `User` | `ADMIN`, `USER` |
| Password demo | `password` | `Password123!` | `password` |
