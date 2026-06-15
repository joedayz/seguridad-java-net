#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TARGET="${1:-$ROOT}"

if ! command -v semgrep >/dev/null 2>&1; then
  echo "Semgrep no esta instalado." >&2
  echo "Instalacion: brew install semgrep  |  pip install semgrep" >&2
  echo "Docs: https://semgrep.dev/docs/getting-started/" >&2
  exit 1
fi

echo "==> Semgrep SAST sobre: $TARGET"
semgrep scan \
  --config "$ROOT/scripts/semgrep/rules/session5.yml" \
  --config p/java \
  --config p/csharp \
  "$TARGET"
