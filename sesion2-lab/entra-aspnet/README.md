# Demo Microsoft Entra ID + ASP.NET Core (JWT Bearer Resource Server)

API mínima que valida tokens JWT de **Microsoft Entra ID**, con los mismos endpoints que la demo
Spring (`../entra-spring-security`) y la demo Keycloak.

Usa la **misma configuración de Azure Portal** que la demo Java: dos app registrations (API + cliente)
y el mismo archivo `.env`.

## Arquitectura

```
  cliente (curl / Postman / cmd / Git Bash)
        │  device code flow
        ▼
  Microsoft Entra ID  ──JWT──▶  ASP.NET Core API :8084
```

### Compatibilidad

| Entorno | Levantar | Token | Probar API |
|---------|----------|-------|------------|
| macOS / Linux | `./compose.sh up --build` | `./get-token.sh` | `curl` |
| Windows sin PowerShell | `docker compose` en **cmd** | `curl.exe` device code (sección abajo) | `curl.exe` |

### Endpoints

| Método | Ruta | Seguridad |
|--------|------|-----------|
| GET | `/api/public/hello` | Público |
| GET | `/api/me` | Autenticado |
| GET | `/api/admin/hello` | Rol de app `ADMIN` |

---

## Configuración en Azure

Sigue los pasos del README de **`../entra-spring-security`** (registro API, app roles `ADMIN`/`USER`,
cliente con flujo de cliente público, permisos delegados y asignación de roles).

Copia `.env.example` a `.env` con los mismos valores (puedes compartir un solo `.env` entre ambas demos).

Variables que consume Docker / `dotnet run`:

| Variable `.env` | Config ASP.NET |
|-----------------|----------------|
| `AZURE_TENANT_ID` | `AzureAd__TenantId` |
| `AZURE_API_CLIENT_ID` | `AzureAd__ClientId` |
| `AZURE_API_AUDIENCE` | `AzureAd__Audience` |

---

## Cómo levantarlo

```bash
cp .env.example .env   # edita con tus GUIDs
chmod +x compose.sh get-token.sh
./compose.sh up --build
```

**Windows (cmd):**

```cmd
copy .env.example .env
docker compose up --build
```

**Git Bash:** `cp .env.example .env` y `./compose.sh up --build`

**PowerShell (opcional):** `.\compose.ps1 up --build`

Local sin contenedor:

```bash
export AzureAd__TenantId="$AZURE_TENANT_ID"
export AzureAd__ClientId="$AZURE_API_CLIENT_ID"
export AzureAd__Audience="$AZURE_API_AUDIENCE"
cd entra-demo
dotnet run
```

---

## Cómo probar

### macOS / Linux (bash)

```bash
curl http://localhost:8084/api/public/hello

TOKEN=$(./get-token.sh)
curl http://localhost:8084/api/me -H "Authorization: Bearer $TOKEN"
curl http://localhost:8084/api/admin/hello -H "Authorization: Bearer $TOKEN"
```

En **Windows** sin PowerShell: [Windows — cmd y curl.exe](#windows--cmd-y-curlexe-sin-powershell).

---

## Windows — cmd y curl.exe (sin PowerShell)

Misma configuración Azure y mismo `.env` que `../entra-spring-security`. Abrí **cmd** en `entra-aspnet`.

Placeholders: `TENANT_ID`, `CLIENT_ID`, `API_CLIENT_ID` (ver tabla en el README de entra-spring-security).

### Levantar y parar

```cmd
cd sesion2-lab\entra-aspnet
copy .env.example .env
docker compose up --build
```

```cmd
docker compose down
```

### 1. Endpoint público

```cmd
curl.exe http://localhost:8084/api/public/hello
```

### 2. Token Entra ID (device code)

**Paso A** — Código de dispositivo:

```cmd
curl.exe -s -X POST https://login.microsoftonline.com/TENANT_ID/oauth2/v2.0/devicecode -H "Content-Type: application/x-www-form-urlencoded" -d "client_id=CLIENT_ID" -d "scope=api://API_CLIENT_ID/access_as_user openid profile offline_access" -o device.json
notepad device.json
```

Abrid `https://microsoft.com/devicelogin`, introducid `user_code` e iniciad sesión.

**Paso B** — Access token (repetir si `authorization_pending`):

```cmd
curl.exe -s -X POST https://login.microsoftonline.com/TENANT_ID/oauth2/v2.0/token -H "Content-Type: application/x-www-form-urlencoded" -d "grant_type=urn:ietf:params:oauth:grant-type:device_code" -d "client_id=CLIENT_ID" -d "device_code=PEGAR_DEVICE_CODE" -o token.json
notepad token.json
```

```cmd
set TOKEN=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIs...
```

### 3. Endpoint autenticado

```cmd
curl.exe http://localhost:8084/api/me -H "Authorization: Bearer %TOKEN%"
```

### 4. Endpoint admin

```cmd
curl.exe http://localhost:8084/api/admin/hello -H "Authorization: Bearer %TOKEN%"
```

| Servicio | URL |
|----------|-----|
| API | http://localhost:8084 |

---

## Validación del token

- `Authority`: `https://login.microsoftonline.com/{tenant}/v2.0`
- `Audience`: Application ID URI de la API (`api://...`)
- `RoleClaimType`: `roles` (app roles de Entra)
- `[Authorize(Roles = "ADMIN")]` exige el app role **ADMIN**

## Comparación de demos en sesion2-lab

| Demo | Stack | IdP | Puerto |
|------|-------|-----|--------|
| `spring-security` | Spring | Keycloak (local) | 8081 |
| `aspnet-identity` | ASP.NET | Identity integrado | 8082 |
| `entra-spring-security` | Spring | Entra ID (tu tenant) | 8083 |
| `entra-aspnet` | ASP.NET | Entra ID (tu tenant) | 8084 |
