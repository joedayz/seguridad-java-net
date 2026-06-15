#!/usr/bin/env bash
set -euo pipefail

TARGET="${1:-.}"

if command -v gitleaks >/dev/null 2>&1; then
  echo "==> Gitleaks sobre: $TARGET"
  gitleaks detect --source "$TARGET" --no-git --verbose
  exit 0
fi

if command -v docker >/dev/null 2>&1; then
  echo "==> Gitleaks (Docker) sobre: $TARGET"
  docker run --rm -v "$(cd "$TARGET" && pwd):/repo" ghcr.io/gitleaks/gitleaks:latest \
    detect --source /repo --no-git --verbose
  exit 0
fi

echo "Gitleaks no encontrado." >&2
echo "Instalacion: brew install gitleaks  |  https://github.com/gitleaks/gitleaks" >&2
exit 1
