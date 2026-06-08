#!/usr/bin/env bash
# Carga secretos de demo en Azure Key Vault (paso 02 — Migrar).
# Requiere: az login, permisos Key Vault Secrets Officer en el vault.
set -euo pipefail
set +H   # evita "bash: !: event not found" en contraseñas

if [ $# -lt 1 ]; then
  echo "Uso: $0 <nombre-del-key-vault>" >&2
  echo "Ejemplo: $0 cas-training-kv" >&2
  exit 1
fi

VAULT_NAME="$1"

echo "Key Vault: $VAULT_NAME"

az keyvault secret set --vault-name "$VAULT_NAME" \
  --name "ConnectionStrings--Default" \
  --value 'Server=demo-db.empresa.com;Database=AppDB;User Id=demo;Password=DemoOnlyNotReal;'

az keyvault secret set --vault-name "$VAULT_NAME" \
  --name "ApiKeys--Stripe" \
  --value "sk_test_demo_stripe_key_1234"

az keyvault secret set --vault-name "$VAULT_NAME" \
  --name "ApiKeys--SendGrid" \
  --value "SG.demo_sendgrid_key_5678"

echo "Secretos creados. Anade a .env:"
echo "KEY_VAULT_URI=https://${VAULT_NAME}.vault.azure.net/"
