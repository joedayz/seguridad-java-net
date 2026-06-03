#!/usr/bin/env bash
# Obtiene un access token de Keycloak via Resource Owner Password Credentials.
# Uso:   ./get-token.sh [usuario] [password]
#        ./get-token.sh alice              # password por defecto (password)
# En Windows usa get-token.ps1
set -euo pipefail

USER="${1:-alice}"
PASS="${2:-password}"

curl -s -X POST "http://localhost:8080/realms/demo/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=demo-client" \
  -d "client_secret=demo-secret" \
  -d "username=${USER}" \
  -d "password=${PASS}" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
