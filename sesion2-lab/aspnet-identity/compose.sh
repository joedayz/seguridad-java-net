#!/usr/bin/env bash
# Wrapper para levantar/parar la demo con Podman o Docker (macOS / Linux).
# En Windows usa compose.ps1
set -euo pipefail

if command -v podman >/dev/null 2>&1; then
  if podman compose version >/dev/null 2>&1; then
    exec podman compose "$@"
  fi
  if command -v podman-compose >/dev/null 2>&1; then
    exec podman-compose "$@"
  fi
fi

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  exec docker compose "$@"
fi

echo "No se encontro podman compose, podman-compose ni docker compose." >&2
exit 1
