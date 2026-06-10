#!/usr/bin/env sh
# Configura el Vault en modo dev para emitir credenciales DINAMICAS de PostgreSQL.
# Se ejecuta una sola vez (contenedor vault-init) y deja todo listo para la app.
set -eu

export VAULT_ADDR="${VAULT_ADDR:-http://vault:8200}"
export VAULT_TOKEN="${VAULT_TOKEN:-dev-token}"

# Valores fijos para que el laboratorio funcione sin copiar/pegar tokens.
ROLE_ID="${VAULT_ROLE_ID:-spring-app-role-id}"
SECRET_ID="${VAULT_SECRET_ID:-spring-app-secret-id}"

echo "[vault-init] Esperando a que Vault responda en $VAULT_ADDR ..."
until vault status >/dev/null 2>&1; do sleep 1; done
echo "[vault-init] Vault disponible."

# 1) Motor de secretos de base de datos.
vault secrets enable -path=database database 2>/dev/null || echo "[vault-init] motor 'database' ya habilitado"

# 2) Conexion al PostgreSQL usando el usuario admin (rota credenciales hijas).
#    Reintenta porque Postgres puede tardar en aceptar conexiones.
echo "[vault-init] Configurando conexion a PostgreSQL (reintentando hasta que acepte) ..."
until vault write database/config/app-postgres \
        plugin_name=postgresql-database-plugin \
        allowed_roles="app-role" \
        connection_url="postgresql://{{username}}:{{password}}@postgres:5432/appdb?sslmode=disable" \
        username="vault-admin" \
        password="vault-admin-pass" >/dev/null 2>&1; do
  sleep 2
done
echo "[vault-init] Conexion a PostgreSQL configurada."

# 3) Rol dinamico: cada lectura crea un usuario PostgreSQL nuevo con TTL de 1 hora.
vault write database/roles/app-role \
  db_name=app-postgres \
  default_ttl="1h" \
  max_ttl="24h" \
  creation_statements="CREATE ROLE \"{{name}}\" WITH LOGIN PASSWORD '{{password}}' VALID UNTIL '{{expiration}}'; GRANT USAGE ON SCHEMA public TO \"{{name}}\"; GRANT SELECT ON ALL TABLES IN SCHEMA public TO \"{{name}}\";" \
  revocation_statements="DROP ROLE IF EXISTS \"{{name}}\";"
echo "[vault-init] Rol dinamico 'app-role' creado (TTL=1h, max=24h)."

# 4) Autenticacion AppRole para la app (en vez del root token).
vault auth enable approle 2>/dev/null || echo "[vault-init] auth 'approle' ya habilitado"
vault policy write app-policy /vault-init/app-policy.hcl

vault write auth/approle/role/spring-app \
  token_policies="app-policy" \
  token_ttl=1h \
  token_max_ttl=4h \
  secret_id_num_uses=0 \
  secret_id_ttl=0

# 5) role-id y secret-id FIJOS (los mismos que el .env consume la app).
#    Idempotente y SIN ruido: borramos el secret-id previo (si existe) y lo recreamos,
#    asi una re-ejecucion no provoca el error 500 "SecretID is already registered".
vault write auth/approle/role/spring-app/role-id role_id="$ROLE_ID"
vault write auth/approle/role/spring-app/secret-id/destroy secret_id="$SECRET_ID" >/dev/null 2>&1 || true
vault write auth/approle/role/spring-app/custom-secret-id secret_id="$SECRET_ID" >/dev/null

echo "[vault-init] AppRole 'spring-app' listo."
echo "[vault-init]   VAULT_ROLE_ID=$ROLE_ID"
echo "[vault-init]   VAULT_SECRET_ID=$SECRET_ID"
echo "[vault-init] Configuracion completada con exito."
