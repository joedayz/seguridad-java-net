#!/usr/bin/env sh
# Arranca Vault en modo dev y lo autoconfigura en el MISMO contenedor.
# Asi funciona igual con `docker compose` y con `podman-compose`, sin necesitar
# un contenedor de init aparte ni `depends_on: service_completed_successfully`
# (que podman-compose no soporta bien).
set -eu

export VAULT_ADDR="http://127.0.0.1:8200"
export VAULT_TOKEN="${VAULT_TOKEN:-dev-token}"

# 1) Servidor Vault dev en segundo plano (en memoria, auto-unseal, solo laboratorio).
vault server -dev \
  -dev-root-token-id="$VAULT_TOKEN" \
  -dev-listen-address="0.0.0.0:8200" &
VAULT_PID=$!

# 2) Configuracion idempotente (motor BD + rol dinamico TTL 1h + AppRole).
sh /vault-init/setup.sh

# 3) Mantener el contenedor vivo con el servidor Vault en primer plano.
wait "$VAULT_PID"
