# Crea Key Vault + IAM + secretos de demo (Ejercicio 2).
# Ejecutar en Azure Cloud Shell (PowerShell) o localmente tras: az login
#
# Uso:
#   ./scripts/create-keyvault.ps1
#   ./scripts/create-keyvault.ps1 -ResourceGroup cas-training-rg -Location westeurope -VaultName mi-vault-unico

[CmdletBinding()]
param(
    [string]$ResourceGroup = $(if ($env:RG) { $env:RG } else { "cas-training-rg" }),
    [string]$Location      = $(if ($env:LOCATION) { $env:LOCATION } else { "westeurope" }),
    [string]$VaultName     = $(if ($env:VAULT_NAME) {
            $env:VAULT_NAME
        } else {
            $ts = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()
            "caskv" + $ts.Substring($ts.Length - 6)
        })
)

$ErrorActionPreference = "Stop"

Write-Host "==> Resource group: $ResourceGroup ($Location)"
az group create --name $ResourceGroup --location $Location -o none

Write-Host "==> Key Vault: $VaultName (RBAC)"
az keyvault create `
    --name $VaultName `
    --resource-group $ResourceGroup `
    --location $Location `
    --enable-rbac-authorization true `
    -o none

$KvId   = az keyvault show --name $VaultName --resource-group $ResourceGroup --query id -o tsv
$UserId = az ad signed-in-user show --query id -o tsv

Write-Host "==> Permisos IAM para el usuario actual"
# El usuario ya podria tener el rol: ignorar el error si la asignacion existe.
az role assignment create `
    --role "Key Vault Secrets Officer" `
    --assignee-object-id $UserId `
    --assignee-principal-type User `
    --scope $KvId -o none 2>$null

az role assignment create `
    --role "Key Vault Secrets User" `
    --assignee-object-id $UserId `
    --assignee-principal-type User `
    --scope $KvId -o none 2>$null

Write-Host "==> Esperando propagacion de roles (15s)..."
Start-Sleep -Seconds 15

Write-Host "==> Secretos de demo"
az keyvault secret set --vault-name $VaultName `
    --name "ConnectionStrings--Default" `
    --value 'Server=demo-db.empresa.com;Database=AppDB;User Id=demo;Password=DemoOnlyNotReal;' -o none

az keyvault secret set --vault-name $VaultName `
    --name "ApiKeys--Stripe" `
    --value 'sk_test_demo_stripe_key_1234' -o none

az keyvault secret set --vault-name $VaultName `
    --name "ApiKeys--SendGrid" `
    --value 'SG.demo_sendgrid_key_5678' -o none

$TenantId = az account show --query tenantId -o tsv

Write-Host ""
Write-Host "=== Listo. Copia en sesion3-lab/aspnet-keyvault/.env ==="
Write-Host "AZURE_TENANT_ID=$TenantId"
Write-Host "KEY_VAULT_URI=https://$VaultName.vault.azure.net/"
Write-Host ""
Write-Host "=== Probar en local ==="
Write-Host "az login --tenant $TenantId"
Write-Host "cd keyvault-demo"
Write-Host "`$env:KeyVault__Uri = 'https://$VaultName.vault.azure.net/'"
Write-Host "dotnet run"
Write-Host "curl.exe http://localhost:8085/api/config/status"
