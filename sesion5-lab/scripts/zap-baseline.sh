#!/usr/bin/env bash
set -euo pipefail

TARGET_URL="${1:-http://host.docker.internal:8208}"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker requerido para ejecutar OWASP ZAP en contenedor." >&2
  exit 1
fi

echo "==> ZAP baseline scan sobre: $TARGET_URL"
docker run --rm -t ghcr.io/zaproxy/zaproxy:stable \
  zap-baseline.py -t "$TARGET_URL" -I

echo ""
echo "Para escaneo completo: zap-full-scan.py -t $TARGET_URL"
