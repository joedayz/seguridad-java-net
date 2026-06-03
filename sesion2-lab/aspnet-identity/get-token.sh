#!/usr/bin/env bash
# Obtiene un JWT de la API via login.
# Uso:   ./get-token.sh [usuario] [password]
#        ./get-token.sh alice              # password por defecto (Password123!)
# En zsh, si pasas password con !, usad comillas simples: ./get-token.sh alice 'Password123!'
# En Windows usa get-token.ps1
set -euo pipefail

USER="${1:-alice}"
PASS="${2:-Password123!}"

curl -s -X POST "http://localhost:8082/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${USER}\",\"password\":\"${PASS}\"}" \
  | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p'
