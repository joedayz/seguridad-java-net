#!/usr/bin/env bash
# Genera keystore.p12 para HTTPS local (autofirmado). Para confiar en el navegador usa mkcert (ver README).
set -euo pipefail
DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${DIR}/api-server/src/main/resources/keystore.p12"
mkdir -p "$(dirname "$OUT")"
keytool -genkeypair -alias api -keyalg RSA -keysize 2048 -storetype PKCS12 \
  -keystore "$OUT" -validity 3650 -storepass changeit \
  -dname "CN=localhost, OU=Demo, O=Demo, L=Demo, ST=Demo, C=ES"
echo "Generado: $OUT"
