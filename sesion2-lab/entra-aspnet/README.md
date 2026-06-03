# Demo Microsoft Entra ID + ASP.NET Core (JWT Bearer Resource Server)

API mínima que valida tokens JWT de **Microsoft Entra ID**, con los mismos endpoints que la demo
Spring (`../entra-spring-security`) y la demo Keycloak.

Usa la **misma configuración de Azure Portal** que la demo Java: dos app registrations (API + cliente)
y el mismo archivo `.env`.

## Arquitectura

```
  cliente
        │  device code flow
        ▼
  Microsoft Entra ID  ──JWT──▶  ASP.NET Core API :8084
```

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

Windows:

```powershell
copy .env.example .env
.\compose.ps1 up --build
```

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

```bash
curl http://localhost:8084/api/public/hello

TOKEN=$(./get-token.sh)
curl http://localhost:8084/api/me -H "Authorization: Bearer $TOKEN"
curl http://localhost:8084/api/admin/hello -H "Authorization: Bearer $TOKEN"
```

PowerShell:

```powershell
Invoke-RestMethod http://localhost:8084/api/public/hello
$TOKEN = .\get-token.ps1
Invoke-RestMethod http://localhost:8084/api/me -Headers @{ Authorization = "Bearer $TOKEN" }
Invoke-RestMethod http://localhost:8084/api/admin/hello -Headers @{ Authorization = "Bearer $TOKEN" }
```

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
