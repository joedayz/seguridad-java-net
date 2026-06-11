# Configura Vault (modo dev) para emitir credenciales DINAMICAS de PostgreSQL.
# Equivalente PowerShell de setup.sh, pensado para Windows.
#
# El laboratorio YA hace esto solo: el contenedor de Vault se autoconfigura al arrancar
# (vault/entrypoint.sh). Usa este script solo si quieres ejecutar la configuracion a mano
# para entender los pasos.
#
# Requisitos:
#   - El stack levantado (./compose.ps1 up --build) -> puerto 8200 expuesto en localhost.
#   - El CLI de Vault instalado en el host:  winget install Hashicorp.Vault
#
# Uso:
#   ./vault/setup.ps1
#   ./vault/setup.ps1 -VaultAddr http://localhost:8200 -RoleId spring-app-role-id

[CmdletBinding()]
param(
    [string]$VaultAddr  = $(if ($env:VAULT_ADDR)      { $env:VAULT_ADDR }      else { "http://localhost:8200" }),
    [string]$VaultToken = $(if ($env:VAULT_TOKEN)     { $env:VAULT_TOKEN }     else { "dev-token" }),
    [string]$RoleId     = $(if ($env:VAULT_ROLE_ID)   { $env:VAULT_ROLE_ID }   else { "spring-app-role-id" }),
    [string]$SecretId   = $(if ($env:VAULT_SECRET_ID) { $env:VAULT_SECRET_ID } else { "spring-app-secret-id" })
)

$ErrorActionPreference = "Stop"
$env:VAULT_ADDR  = $VaultAddr
$env:VAULT_TOKEN = $VaultToken

$PolicyFile = Join-Path $PSScriptRoot "app-policy.hcl"

if (-not (Get-Command vault -ErrorAction SilentlyContinue)) {
    Write-Error "No se encontro el CLI de 'vault'. Instala con: winget install Hashicorp.Vault"
    exit 1
}

Write-Host "[vault-init] Esperando a que Vault responda en $VaultAddr ..."
do {
    vault status *> $null
    if ($LASTEXITCODE -ne 0) { Start-Sleep -Seconds 1 }
} until ($LASTEXITCODE -eq 0)
Write-Host "[vault-init] Vault disponible."

# 1) Motor de secretos de base de datos (idempotente).
vault secrets enable -path=database database 2>$null
if ($LASTEXITCODE -ne 0) { Write-Host "[vault-init] motor 'database' ya habilitado" }

# 2) Conexion al PostgreSQL usando el usuario admin (rota credenciales hijas).
#    Reintenta porque Postgres puede tardar en aceptar conexiones.
Write-Host "[vault-init] Configurando conexion a PostgreSQL (reintentando hasta que acepte) ..."
do {
    vault write database/config/app-postgres `
        plugin_name=postgresql-database-plugin `
        allowed_roles=app-role `
        "connection_url=postgresql://{{username}}:{{password}}@postgres:5432/appdb?sslmode=disable" `
        username=vault-admin `
        password=vault-admin-pass *> $null
    if ($LASTEXITCODE -ne 0) { Start-Sleep -Seconds 2 }
} until ($LASTEXITCODE -eq 0)
Write-Host "[vault-init] Conexion a PostgreSQL configurada."

# 3) Rol dinamico: cada lectura crea un usuario PostgreSQL nuevo con TTL de 1 hora.
$createStmt = 'CREATE ROLE "{{name}}" WITH LOGIN PASSWORD ''{{password}}'' VALID UNTIL ''{{expiration}}''; GRANT USAGE ON SCHEMA public TO "{{name}}"; GRANT SELECT ON ALL TABLES IN SCHEMA public TO "{{name}}";'
$revokeStmt = 'DROP ROLE IF EXISTS "{{name}}";'
vault write database/roles/app-role `
    db_name=app-postgres `
    default_ttl=1h `
    max_ttl=24h `
    "creation_statements=$createStmt" `
    "revocation_statements=$revokeStmt"
Write-Host "[vault-init] Rol dinamico 'app-role' creado (TTL=1h, max=24h)."

# 4) Autenticacion AppRole para la app (en vez del root token).
vault auth enable approle 2>$null
if ($LASTEXITCODE -ne 0) { Write-Host "[vault-init] auth 'approle' ya habilitado" }
vault policy write app-policy $PolicyFile

vault write auth/approle/role/spring-app `
    token_policies=app-policy `
    token_ttl=1h `
    token_max_ttl=4h `
    secret_id_num_uses=0 `
    secret_id_ttl=0

# 5) role-id y secret-id FIJOS (los mismos que el .env consume la app).
#    Idempotente y SIN ruido: borramos el secret-id previo (si existe) y lo recreamos,
#    asi una re-ejecucion no provoca el error 500 "SecretID is already registered".
vault write auth/approle/role/spring-app/role-id role_id=$RoleId
vault write auth/approle/role/spring-app/secret-id/destroy secret_id=$SecretId *> $null
vault write auth/approle/role/spring-app/custom-secret-id secret_id=$SecretId | Out-Null

Write-Host "[vault-init] AppRole 'spring-app' listo."
Write-Host "[vault-init]   VAULT_ROLE_ID=$RoleId"
Write-Host "[vault-init]   VAULT_SECRET_ID=$SecretId"
Write-Host "[vault-init] Configuracion completada con exito."
