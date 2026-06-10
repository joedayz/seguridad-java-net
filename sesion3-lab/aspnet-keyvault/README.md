# Ejercicio 2 — Externalización segura de credenciales (ASP.NET + Azure Key Vault)

Demo del **Ejercicio 2** del material: migrar una app .NET que tenía credenciales en
`appsettings.json` hacia **Azure Key Vault** con **`DefaultAzureCredential`**
(Entra ID / Managed Identity).

Reutiliza el **mismo tenant de Azure** que las demos Entra de `sesion2-lab`.

## Antes vs después

| Antes (inseguro) | Después (esta demo) |
|------------------|---------------------|
| Secretos en `appsettings.json` en el repo | Solo `KeyVault:Uri` en configuración |
| Ver `keyvault-demo/appsettings.insecure.json.example` | `Program.cs` → `AddAzureKeyVault` + `DefaultAzureCredential` |

## Arquitectura

```
  dotnet run / Docker
        │  DefaultAzureCredential (az login o Managed Identity)
        ▼
  Azure Key Vault  ──secretos──▶  ASP.NET Core :8085
        ▲
  Microsoft Entra ID (mismo tenant que entra-aspnet)
```

| Servicio | URL |
|----------|-----|
| API | http://localhost:8085 |

---

## Fases del ejercicio

### 01 — Identificar

Busca secretos en el repo (no deben existir en código commiteado):

```bash
# Ejemplos del material
gitleaks detect -v
# trufflehog filesystem .
```
NOTA: https://github.com/gitleaks/gitleaks

Referencia del **antes**: `keyvault-demo/appsettings.insecure.json.example`

### 02 — Migrar (Azure CLI — recomendado)

En **Azure Cloud Shell** (o local con `az login`), desde la carpeta de la demo:

```bash
chmod +x scripts/create-keyvault.sh
./scripts/create-keyvault.sh
```

**PowerShell** (Windows / Azure Cloud Shell):

```powershell
./scripts/create-keyvault.ps1
```

El script crea resource group, Key Vault (RBAC), asigna permisos IAM y carga los secretos.
Al final imprime las líneas para tu `.env`.

Variables opcionales:

```bash
RG=mi-rg LOCATION=westeurope VAULT_NAME=mi-vault-unico ./scripts/create-keyvault.sh
```

```powershell
./scripts/create-keyvault.ps1 -ResourceGroup mi-rg -Location westeurope -VaultName mi-vault-unico
```

#### Solo secretos (si el vault ya existe)

```bash
chmod +x scripts/setup-keyvault-secrets.sh
./scripts/setup-keyvault-secrets.sh cas-training-kv-demo
```

**PowerShell**:

```powershell
./scripts/setup-keyvault-secrets.ps1 cas-training-kv-demo
```

| Nombre en Key Vault | Se mapea a configuración |
|---------------------|--------------------------|
| `ConnectionStrings--Default` | `ConnectionStrings:Default` |
| `ApiKeys--Stripe` | `ApiKeys:Stripe` |
| `ApiKeys--SendGrid` | `ApiKeys:SendGrid` |

#### Alternativa: Azure Portal

1. **Key vaults** → **Create** → permission model **RBAC**
2. Subir los secretos de la tabla anterior
3. IAM → tu usuario → **Key Vault Secrets User**

#### Permisos IAM (Entra)

**Desarrollo local** (`dotnet run` + `az login`):

1. Key Vault → **Access control (IAM)** → **Add role assignment**
2. Rol: **Key Vault Secrets User**
3. Miembro: **tu usuario** de Entra (el mismo con el que haces device code en las demos Entra)

**Producción (opcional)**: asigna el mismo rol a la **Managed Identity** de la app cuando la despliegues en Azure App Service.

> No reutilices el client secret de `entra-demo-client` en el código: para Docker/CI crea una app
> `keyvault-demo-sp` dedicada con rol en el vault (ver `.env.example`).

### 03 — Integrar

El código ya está en `keyvault-demo/Program.cs`:

```csharp
builder.Configuration.AddAzureKeyVault(
    new Uri(builder.Configuration["KeyVault:Uri"]),
    new DefaultAzureCredential());
```

---

## Configuración local

```bash
cp .env.example .env
# Edita KEY_VAULT_URI y AZURE_TENANT_ID (mismo tenant que entra-spring-security)
```

Ejemplo `.env`:

```env
AZURE_TENANT_ID=aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee
KEY_VAULT_URI=https://cas-training-kv-demo.vault.azure.net/
```

Inicia sesión en Azure CLI (mismo tenant):

```bash
az login --tenant "$AZURE_TENANT_ID"
az account show
```

---

## Cómo levantarlo

### Recomendado: local con `dotnet run`

```bash
cd keyvault-demo
export $(grep -v '^#' ../.env | xargs)
export KeyVault__Uri="$KEY_VAULT_URI"
dotnet run
```

### Docker / Podman (requiere service principal en `.env`)

Dentro del contenedor **no hay `az login` ni Managed Identity**, así que
`DefaultAzureCredential` no tiene de dónde sacar un token. El `create-keyvault.sh` **no
genera** estas credenciales (solo da permiso a *tu* usuario), por eso hay que crear un
**service principal** dedicado y añadir `AZURE_CLIENT_ID` + `AZURE_CLIENT_SECRET` al `.env`.

#### Paso 1 — Crear el service principal con rol sobre el vault

Con `az login` hecho (mismo tenant), ejecuta:

```bash
# Resource group donde está el vault (si no lo recuerdas)
az keyvault show --name caskv099134 --query resourceGroup -o tsv

SUB=$(az account show --query id -o tsv)
RG=cas-training-rg            # ajusta si usaste otro
VAULT=caskv099134            # tu Key Vault

az ad sp create-for-rbac \
  --name "keyvault-demo-sp" \
  --role "Key Vault Secrets User" \
  --scopes "/subscriptions/$SUB/resourceGroups/$RG/providers/Microsoft.KeyVault/vaults/$VAULT"
```

**PowerShell**:

```powershell
$Sub   = az account show --query id -o tsv
$Rg    = "cas-training-rg"
$Vault = "caskv099134"

az ad sp create-for-rbac `
  --name "keyvault-demo-sp" `
  --role "Key Vault Secrets User" `
  --scopes "/subscriptions/$Sub/resourceGroups/$Rg/providers/Microsoft.KeyVault/vaults/$Vault"
```

#### Paso 2 — Mapear la salida al `.env`

El comando imprime un JSON. **El `password` solo se muestra una vez**:

```json
{
  "appId":       "11111111-2222-3333-4444-555555555555",  ← AZURE_CLIENT_ID
  "displayName": "keyvault-demo-sp",
  "password":    "abc~secret~generado~por~azure",          ← AZURE_CLIENT_SECRET
  "tenant":      "59c70fe1-...-7901c9969f4c"               ← AZURE_TENANT_ID
}
```

Tu `.env` queda así:

```env
AZURE_TENANT_ID=59c70fe1-a889-4ac0-8e4c-7901c9969f4c
AZURE_CLIENT_ID=11111111-2222-3333-4444-555555555555
AZURE_CLIENT_SECRET=abc~secret~generado~por~azure
KEY_VAULT_URI=https://caskv099134.vault.azure.net/
```

> Con estas 3 variables, `DefaultAzureCredential` usa **EnvironmentCredential** dentro del
> contenedor. El `.env` está en `.gitignore`: **nunca** lo subas al repo.

#### Paso 3 — Levantar

```bash
./compose.sh up --build
```

#### Si pierdes el secret o caduca

No se puede recuperar; genera uno nuevo para la misma app:

```bash
APP_ID=$(az ad sp list --display-name keyvault-demo-sp --query "[0].appId" -o tsv)
az ad app credential reset --id "$APP_ID" --query "{AZURE_CLIENT_ID:appId, AZURE_CLIENT_SECRET:password, AZURE_TENANT_ID:tenant}"
```

---

## Cómo probar

```bash
curl http://localhost:8085/api/public/hello

curl http://localhost:8085/api/config/status
```

Respuesta esperada (`allSecretsPresent: true` y valores enmascarados):

```json
{
  "keyVault": { "configured": true, "uri": "https://....vault.azure.net/" },
  "secretsLoaded": {
    "connectionString": "****e!;",
    "stripe": "****1234",
    "sendGrid": "****5678"
  },
  "allSecretsPresent": true
}
```

Si `allSecretsPresent` es `false`, revisa permisos IAM y que los nombres de secretos coincidan.

---

## Windows — cmd y curl.exe

```cmd
cd sesion3-lab\aspnet-keyvault
copy .env.example .env
az login --tenant TU_TENANT_ID
cd keyvault-demo
set KeyVault__Uri=https://tu-vault.vault.azure.net/
dotnet run
```

```cmd
curl.exe http://localhost:8085/api/config/status
```

---

## Relación con otras demos

| Demo | Puerto | Uso de Entra |
|------|--------|--------------|
| `entra-spring-security` | 8083 | JWT resource server |
| `entra-aspnet` | 8084 | JWT resource server |
| **aspnet-keyvault** | **8085** | `DefaultAzureCredential` → Key Vault |

Mismo **tenant**; esta demo añade **Key Vault** sin sustituir las demos OAuth.

---

## Producción: Managed Identity

En Azure App Service:

1. Activar **System assigned managed identity**
2. IAM en Key Vault → **Key Vault Secrets User** para esa identity
3. `DefaultAzureCredential` la detecta automáticamente (sin client secret en código)
