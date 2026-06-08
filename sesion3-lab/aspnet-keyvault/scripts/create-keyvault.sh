#!/usr/bin/env bash
# Crea Key Vault + IAM + secretos de demo (Ejercicio 2).
# Ejecutar en Azure Cloud Shell o localmente tras: az login
#
# Uso:
#   chmod +x scripts/create-keyvault.sh
#   ./scripts/create-keyvault.sh
#
# Opcional: RG=cas-training-rg LOCATION=westeurope ./scripts/create-keyvault.sh
set -euo pipefail
set +H   # evita "bash: !: event not found" en contraseñas

RG="${RG:-cas-training-rg}"
LOCATION="${LOCATION:-westeurope}"
VAULT_NAME="${VAULT_NAME:-caskv$(date +%s | tail -c 7)}"

echo "==> Resource group: $RG ($LOCATION)"
az group create --name "$RG" --location "$LOCATION" -o none

echo "==> Key Vault: $VAULT_NAME (RBAC)"
az keyvault create \
  --name "$VAULT_NAME" \
  --resource-group "$RG" \
  --location "$LOCATION" \
  --enable-rbac-authorization true \
  -o none

KV_ID=$(az keyvault show --name "$VAULT_NAME" --resource-group "$RG" --query id -o tsv)
USER_ID=$(az ad signed-in-user show --query id -o tsv)

echo "==> Permisos IAM para el usuario actual"
az role assignment create \
  --role "Key Vault Secrets Officer" \
  --assignee-object-id "$USER_ID" \
  --assignee-principal-type User \
  --scope "$KV_ID" -o none 2>/dev/null || true

az role assignment create \
  --role "Key Vault Secrets User" \
  --assignee-object-id "$USER_ID" \
  --assignee-principal-type User \
  --scope "$KV_ID" -o none 2>/dev/null || true

echo "==> Esperando propagacion de roles (15s)..."
sleep 15

echo "==> Secretos de demo"
az keyvault secret set --vault-name "$VAULT_NAME" \
  --name "ConnectionStrings--Default" \
  --value 'Server=demo-db.empresa.com;Database=AppDB;User Id=demo;Password=DemoOnlyNotReal;'

az keyvault secret set --vault-name "$VAULT_NAME" \
  --name "ApiKeys--Stripe" \
  --value 'sk_test_demo_stripe_key_1234'

az keyvault secret set --vault-name "$VAULT_NAME" \
  --name "ApiKeys--SendGrid" \
  --value 'SG.demo_sendgrid_key_5678'

TENANT_ID=$(az account show --query tenantId -o tsv)

echo ""
echo "=== Listo. Copia en sesion3-lab/aspnet-keyvault/.env ==="
echo "AZURE_TENANT_ID=$TENANT_ID"
echo "KEY_VAULT_URI=https://${VAULT_NAME}.vault.azure.net/"
echo ""
echo "=== Probar en local ==="
echo "az login --tenant $TENANT_ID"
echo "cd keyvault-demo"
echo "export KeyVault__Uri=https://${VAULT_NAME}.vault.azure.net/"
echo "dotnet run"
echo "curl http://localhost:8085/api/config/status"
