#!/usr/bin/env bash
# Mismo script que entra-spring-security (device code contra Entra ID).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -f "${SCRIPT_DIR}/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "${SCRIPT_DIR}/.env"
  set +a
fi

TENANT_ID="${AZURE_TENANT_ID:?Define AZURE_TENANT_ID en .env}"
CLIENT_ID="${AZURE_CLIENT_ID:?Define AZURE_CLIENT_ID en .env}"
SCOPE="${AZURE_SCOPE:?Define AZURE_SCOPE en .env}"

json_get() {
  local key="$1"
  python3 -c "import json,sys; print(json.load(sys.stdin).get('${key}',''))" 2>/dev/null \
    || sed -n "s/.*\"${key}\":\"\([^\"]*\)\".*/\1/p"
}

RESP=$(curl -s -X POST "https://login.microsoftonline.com/${TENANT_ID}/oauth2/v2.0/devicecode" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=${CLIENT_ID}" \
  -d "scope=${SCOPE} openid profile offline_access")

USER_CODE=$(echo "$RESP" | json_get user_code)
DEVICE_CODE=$(echo "$RESP" | json_get device_code)
VERIFICATION_URI=$(echo "$RESP" | json_get verification_uri)
INTERVAL=$(echo "$RESP" | json_get interval)
INTERVAL="${INTERVAL:-5}"

if [[ -z "$DEVICE_CODE" ]]; then
  echo "Error al solicitar device code:" >&2
  echo "$RESP" >&2
  exit 1
fi

echo "Abre ${VERIFICATION_URI:-https://microsoft.com/devicelogin}" >&2
echo "Introduce el codigo: ${USER_CODE}" >&2
echo "Esperando autenticacion..." >&2

while true; do
  TOKEN_RESP=$(curl -s -X POST "https://login.microsoftonline.com/${TENANT_ID}/oauth2/v2.0/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=urn:ietf:params:oauth:grant-type:device_code" \
    -d "client_id=${CLIENT_ID}" \
    -d "device_code=${DEVICE_CODE}")

  ERROR=$(echo "$TOKEN_RESP" | json_get error)
  if [[ "$ERROR" == "authorization_pending" ]]; then
    sleep "$INTERVAL"
    continue
  fi
  if [[ -n "$ERROR" ]]; then
    echo "Error al obtener token: $TOKEN_RESP" >&2
    exit 1
  fi

  echo "$TOKEN_RESP" | json_get access_token
  exit 0
done
