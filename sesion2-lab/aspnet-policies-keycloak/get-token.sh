#!/usr/bin/env bash
# Token Keycloak (password grant). Uso: ./get-token.sh [usuario] [password]
set -euo pipefail

USER="${1:-carol}"
PASS="${2:-password}"
KC_PORT="${KC_PORT:-8091}"

curl -s -X POST "http://localhost:${KC_PORT}/realms/demo/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=demo-client" \
  -d "client_secret=demo-secret" \
  -d "username=${USER}" \
  -d "password=${PASS}" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
