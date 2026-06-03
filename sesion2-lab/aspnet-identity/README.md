# Demo ASP.NET Core Identity (configuracion avanzada)

Demo sencilla que levanta **ASP.NET Core Identity** con PostgreSQL usando Compose (Podman o
Docker Desktop). La configuracion replica la del slide de *Configuracion avanzada*: politica de
contrasenas estricta, bloqueo por intentos fallidos y confirmacion de email obligatoria.

## Arquitectura

```
  cliente (curl / Postman / cmd / Git Bash)
        │  1) POST /api/auth/login
        ▼
  ┌──────────────────┐     JWT Bearer     ┌─────────────────────┐
  │  Identity Demo   │ ◀───────────────   │  Endpoints protegidos│
  │  ASP.NET :8082   │                    │  /api/me, /api/admin │
  └────────┬─────────┘                    └─────────────────────┘
           │
           ▼
  ┌──────────────────┐
  │  PostgreSQL :5433│
  └──────────────────┘
```

- **API** en `http://localhost:8082`
- **PostgreSQL** en `localhost:5433` (usuario `demo`, password `demo`, BD `identitydemo`)

### Configuracion de Identity (Program.cs)

| Politica | Valor |
|----------|-------|
| Longitud minima de contrasena | 12 caracteres |
| Requiere mayuscula | Si |
| Requiere caracter no alfanumerico | Si |
| Intentos fallidos antes de bloqueo | 5 |
| Tiempo de bloqueo | 15 minutos |
| Email confirmado para login | Si (`RequireConfirmedEmail = true`) |

### Usuarios precargados

| Usuario | Password | Roles | Email confirmado |
|---------|----------|-------|------------------|
| `alice` | `Password123!` | Admin, User | Si |
| `bob` | `Password123!` | User | Si |

### Endpoints

| Metodo | Ruta | Seguridad |
|--------|------|-----------|
| POST | `/api/auth/register` | Publico |
| POST | `/api/auth/confirm-email` | Publico |
| POST | `/api/auth/login` | Publico |
| GET | `/api/public/hello` | Publico |
| GET | `/api/me` | Autenticado (JWT) |
| GET | `/api/admin/hello` | Rol `Admin` |

### Compatibilidad

| Entorno | Levantar servicios | Obtener token | Probar endpoints |
|---------|-------------------|---------------|------------------|
| macOS / Linux + **Podman** | `podman compose up --build` | `./get-token.sh` | `curl` (bash) |
| macOS / Linux + **Docker** | `docker compose up --build` | `./get-token.sh` | `curl` (bash) |
| Windows + **Docker Desktop** | `docker compose up --build` | `.\get-token.ps1` o **curl** (cmd) | `curl` o Postman |
| Windows + **Podman Desktop** | `podman compose up --build` | igual que arriba | `curl` o Postman |
| Windows **sin PowerShell** | `docker compose` en **cmd** | `curl.exe` (ver sección abajo) | `curl.exe` |
| Cualquiera (auto-detecta) | `./compose.sh` / `.\compose.ps1` | scripts anteriores | segun SO |

---

## Como levantarlo

La primera vez tarda un poco porque compila la app .NET y crea las tablas de Identity en PostgreSQL.

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
podman compose down --rmi local -v   # tambien elimina imagenes y volumen de BD
```

### Docker Desktop (macOS / Linux / Windows)

```bash
docker compose up --build
```

Parar y limpiar:

```bash
docker compose down
docker compose down --rmi local -v
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

> En Windows sin PowerShell: [Windows — cmd y curl.exe](#windows--cmd-y-curlexe-sin-powershell).

> El script de bash prioriza Podman; el de PowerShell prioriza Docker Desktop (habitual en Windows).

---

## Como probar

### macOS / Linux (bash)

**1. Endpoint publico**

```bash
curl http://localhost:8082/api/public/hello
```

**2. Login y token JWT**

```bash
TOKEN=$(./get-token.sh alice Password123!)
echo "$TOKEN"
```

**3. Endpoint autenticado**

```bash
curl http://localhost:8082/api/me -H "Authorization: Bearer $TOKEN"
```

**4. Endpoint admin**

Con `alice` (Admin) → **200 OK**:

```bash
curl http://localhost:8082/api/admin/hello -H "Authorization: Bearer $TOKEN"
```

Con `bob` (solo User) → **403 Forbidden**:

```bash
TOKEN_BOB=$(./get-token.sh bob Password123!)
curl -i http://localhost:8082/api/admin/hello -H "Authorization: Bearer $TOKEN_BOB"
```

**5. Probar politica de contrasenas (registro)**

```bash
curl -s -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"carol","email":"carol@example.com","password":"corta"}' | jq
```

**6. Probar confirmacion de email**

Tras registrar un usuario, en modo Development la respuesta incluye `confirmationToken`:

```bash
curl -s -X POST http://localhost:8082/api/auth/confirm-email \
  -H "Content-Type: application/json" \
  -d '{"email":"carol@example.com","token":"<confirmationToken>"}'
```

**7. Probar bloqueo por intentos fallidos**

Tras 5 logins fallidos con la misma cuenta, el siguiente intento devuelve **423 Locked** durante 15 minutos.

En **Windows** sin PowerShell: [Windows — cmd y curl.exe](#windows--cmd-y-curlexe-sin-powershell).

---

## Windows — cmd y curl.exe (sin PowerShell)

Abrí **cmd** en la carpeta `aspnet-identity`. Usad `curl.exe`; no hace falta PowerShell.

### Levantar y parar servicios

```cmd
cd sesion2-lab\aspnet-identity
docker compose up --build
```

```cmd
docker compose down
```

### 1. Endpoint publico

```cmd
curl.exe http://localhost:8082/api/public/hello
```

### 2. Login y token JWT

```cmd
curl.exe -s -X POST http://localhost:8082/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"Password123!\"}" -o token.json
notepad token.json
```

Copiad el valor de `accessToken` y definid:

```cmd
set TOKEN=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. Endpoint autenticado

```cmd
curl.exe http://localhost:8082/api/me -H "Authorization: Bearer %TOKEN%"
```

### 4. Endpoint admin

Con **alice** (Admin):

```cmd
curl.exe http://localhost:8082/api/admin/hello -H "Authorization: Bearer %TOKEN%"
```

Con **bob** (`Password123!` en el login del paso 2) → **403**:

```cmd
curl.exe -s -X POST http://localhost:8082/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"bob\",\"password\":\"Password123!\"}" -o token-bob.json
notepad token-bob.json
set TOKEN=...valor accessToken de bob...
curl.exe -i http://localhost:8082/api/admin/hello -H "Authorization: Bearer %TOKEN%"
```

### 5. Politica de contrasenas (registro)

```cmd
curl.exe -s -X POST http://localhost:8082/api/auth/register -H "Content-Type: application/json" -d "{\"username\":\"carol\",\"email\":\"carol@example.com\",\"password\":\"corta\"}"
```

### 6. Confirmacion de email

Sustituid el token que devolvio el registro en Development:

```cmd
curl.exe -s -X POST http://localhost:8082/api/auth/confirm-email -H "Content-Type: application/json" -d "{\"email\":\"carol@example.com\",\"token\":\"PEGAR_CONFIRMATION_TOKEN\"}"
```

### 7. Bloqueo por intentos fallidos

Repetid login incorrecto 5 veces; el sexto intento valido puede devolver **423 Locked**.

### Puerto API

| Servicio | URL |
|----------|-----|
| API | http://localhost:8082 |
| PostgreSQL | localhost:5433 |

---

## Ejecutar la API en local (sin contenedor de la app)

Levanta solo PostgreSQL y ejecuta la app con `dotnet run`:

**Podman:**

```bash
podman compose up postgres
cd identity-demo
dotnet run
```

**Docker Desktop:**

```bash
docker compose up postgres
cd identity-demo
dotnet run
```

**Windows (cmd o PowerShell):**

```cmd
docker compose up postgres
cd identity-demo
dotnet run
```

La connection string por defecto en `appsettings.json` apunta a `localhost:5433`.

## Comparacion con la demo Spring Security + Keycloak

| Aspecto | Spring Security (`../spring-security`) | ASP.NET Identity (esta demo) |
|---------|----------------------------------------|------------------------------|
| IdP / auth | Keycloak externo | Identity integrado en la app |
| Token | JWT de Keycloak | JWT generado por la API |
| Puerto API | 8081 | 8082 |
| Roles | `ADMIN`, `USER` | `Admin`, `User` |
| Password demo | `password` | `Password123!` (cumple politica) |

## Nota de produccion

> En entornos de produccion, revisa siempre `RequireConfirmedEmail` y `MaxFailedAccessAttempts`.
> Los valores por defecto de Identity son demasiado permisivos para aplicaciones con datos sensibles.
