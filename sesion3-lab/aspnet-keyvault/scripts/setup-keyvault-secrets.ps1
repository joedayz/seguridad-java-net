# Carga secretos de demo en Azure Key Vault (paso 02 - Migrar).
# Requiere: az login, permisos Key Vault Secrets Officer en el vault.
#
# Uso:
#   ./scripts/setup-keyvault-secrets.ps1 -VaultName cas-training-kv
#   ./scripts/setup-keyvault-secrets.ps1 cas-training-kv

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$VaultName
)

$ErrorActionPreference = "Stop"

Write-Host "Key Vault: $VaultName"

az keyvault secret set --vault-name $VaultName `
    --name "ConnectionStrings--Default" `
    --value 'Server=demo-db.empresa.com;Database=AppDB;User Id=demo;Password=DemoOnlyNotReal;' -o none

az keyvault secret set --vault-name $VaultName `
    --name "ApiKeys--Stripe" `
    --value 'sk_test_demo_stripe_key_1234' -o none

az keyvault secret set --vault-name $VaultName `
    --name "ApiKeys--SendGrid" `
    --value 'SG.demo_sendgrid_key_5678' -o none

Write-Host "Secretos creados. Anade a .env:"
Write-Host "KEY_VAULT_URI=https://$VaultName.vault.azure.net/"
