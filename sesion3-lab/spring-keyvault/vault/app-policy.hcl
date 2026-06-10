# Permisos minimos para la app: solo leer credenciales dinamicas del rol "app-role".
path "database/creds/app-role" {
  capabilities = ["read"]
}
