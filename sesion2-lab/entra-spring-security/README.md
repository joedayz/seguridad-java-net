# Demo Microsoft Entra ID + Spring Security (OAuth2 Resource Server)

Demo que valida tokens JWT emitidos por **tu tenant de Microsoft Entra ID** (Azure Portal).
No incluye un IdP local en Docker: usas las aplicaciones que registres en Entra.

Misma idea que la demo Keycloak (`../spring-security`), pero el issuer es
`https://login.microsoftonline.com/{tenant-id}/v2.0` y los roles vienen del claim `roles`.

## Arquitectura

```
  cliente (curl / Postman / PowerShell)
        │  1) device code flow (navegador / Microsoft login)
        ▼
  ┌─────────────────────┐        2) JWT          ┌──────────────────┐
  │  Microsoft Entra ID │ ───────────────────▶   │  Resource Server  │
  │  (tu tenant Azure)  │   3) valida firma      │  Spring Boot :8083│
  └─────────────────────┘   (JWKS de Entra)      └──────────────────┘
```

- **Entra ID**: `https://login.microsoftonline.com/{tenant-id}/v2.0`
- **Resource Server**: `http://localhost:8083`

### Endpoints del Resource Server

| Método | Ruta | Seguridad |
|--------|------|-----------|
| GET | `/api/public/hello` | Público (sin token) |
| GET | `/api/me` | Autenticado (token válido para esta API) |
| GET | `/api/admin/hello` | Solo app role `ADMIN` |

### Compatibilidad

| Entorno | Levantar servicios | Obtener token | Probar endpoints |
|---------|-------------------|---------------|------------------|
| macOS / Linux | `./compose.sh up --build` | `./get-token.sh` | `curl` |
| Windows | `.\compose.ps1 up --build` | `.\get-token.ps1` | `Invoke-RestMethod` |

---

## Configuración en Azure Portal (una sola vez)

Necesitas **dos** registros de aplicación en **Microsoft Entra ID** > **App registrations**.

### 1. App API (`entra-demo-api`)

1. **New registration** → nombre `entra-demo-api` → Supported account types según tu tenant.
2. Anota el **Application (client) ID** → será `AZURE_API_CLIENT_ID`.
3. Anota el **Directory (tenant) ID** → será `AZURE_TENANT_ID`.
4. **Expose an API**:
   - **Set** Application ID URI → por ejemplo `api://<API_CLIENT_ID>` (recomendado).
   - **Add a scope**: `access_as_user`, usuarios pueden consentir.
5. **App roles** → **Create app role** (dos roles, tipo *Users/Groups*):

   | Display name | Value | Description |
   |--------------|-------|-------------|
   | Administrator | `ADMIN` | Acceso admin |
   | User | `USER` | Usuario estándar |

6. **Enterprise applications** → busca `entra-demo-api` → **Users and groups** → asigna usuarios:
   - Tu cuenta de prueba con rol **ADMIN** (para probar `/api/admin/hello`).
   - Otra cuenta (opcional) con rol **USER** (debería recibir **403** en admin).

### 2. App cliente (`entra-demo-client`)

1. **New registration** → nombre `entra-demo-client`.
2. Anota el **Application (client) ID** → será `AZURE_CLIENT_ID`.
3. **Authentication** → **Advanced settings** → activa **Allow public client flows** = **Yes**
   (necesario para el script *device code* del laboratorio).
4. **API permissions** → **Add a permission** → **My APIs** → `entra-demo-api` →
   delegada `access_as_user` → **Grant admin consent** (si tu tenant lo exige).

### 3. Archivo `.env`

```bash
cp .env.example .env
# Edita .env con tus GUIDs reales
```

Ejemplo:

```env
AZURE_TENANT_ID=aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee
AZURE_API_CLIENT_ID=11111111-2222-3333-4444-555555555555
AZURE_API_AUDIENCE=api://11111111-2222-3333-4444-555555555555
AZURE_CLIENT_ID=66666666-7777-8888-9999-000000000000
AZURE_SCOPE=api://11111111-2222-3333-4444-555555555555/access_as_user
```

> **Audience**: el claim `aud` del token debe coincidir con `AZURE_API_AUDIENCE`
> (normalmente el Application ID URI de la API).

---

## Cómo levantarlo

```bash
chmod +x compose.sh get-token.sh
./compose.sh up --build
```

Windows:

```powershell
.\compose.ps1 up --build
```

Solo el resource server (sin contenedor):

```bash
export $(grep -v '^#' .env | xargs)   # bash: carga variables
cd resource-server
mvn spring-boot:run
```

---

## Cómo probar

### 1. Endpoint público

```bash
curl http://localhost:8083/api/public/hello
```

### 2. Obtener token (device code)

El script abre el flujo en el navegador; inicia sesión con el usuario que tenga el rol deseado.

```bash
TOKEN=$(./get-token.sh)
echo "$TOKEN"
```

Windows:

```powershell
$TOKEN = .\get-token.ps1
```

### 3. Endpoint autenticado

```bash
curl http://localhost:8083/api/me -H "Authorization: Bearer $TOKEN"
```

Deberías ver el claim `roles` (p. ej. `["ADMIN"]`).

### 4. Endpoint admin

Usuario con app role **ADMIN** → **200 OK**:

```bash
curl http://localhost:8083/api/admin/hello -H "Authorization: Bearer $TOKEN"
```

Usuario solo con **USER** → **403 Forbidden** (vuelve a pedir token iniciando sesión con esa cuenta).

---

## Cómo funciona la validación

- `issuer-uri` apunta al endpoint v2 de tu tenant; Spring descarga JWKS desde el metadata de OpenID.
- `audiences` debe coincidir con el `aud` del access token (Application ID URI de la API).
- Los **app roles** de Entra viajan en `roles`; `SecurityConfig` los mapea a `ROLE_ADMIN`, `ROLE_USER`, etc.

## Comparación con Keycloak

| Aspecto | Keycloak (`../spring-security`) | Entra ID (esta demo) |
|---------|--------------------------------|----------------------|
| IdP | Keycloak local en Docker | Tu tenant en Azure |
| Puerto API | 8081 | 8083 |
| Roles en JWT | `realm_access.roles` | `roles` (app roles) |
| Obtener token | usuario + password (ROPC) | device code (navegador) |
| Config | realm export automático | App registrations en Portal |

## Notas

- Microsoft desaconseja el flujo *Resource Owner Password*; el laboratorio usa **device code**,
  alineado con cuentas reales de Entra.
- Si el token no trae `roles`, revisa la asignación en **Enterprise applications** y que el
  scope en `.env` apunte a tu API (`api://.../access_as_user`).
- Para depurar el JWT: [jwt.ms](https://jwt.ms).
