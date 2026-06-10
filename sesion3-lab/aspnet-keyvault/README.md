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

### Docker (requiere service principal en `.env`)

```bash
./compose.sh up --build
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
