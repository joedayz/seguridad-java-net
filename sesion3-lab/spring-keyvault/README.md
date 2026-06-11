# Ejercicio 3 — Secretos dinámicos con HashiCorp Vault (Spring Boot)

Demo del **Ejercicio 3**: Spring Boot obtiene credenciales de **PostgreSQL dinámicas**
desde **HashiCorp Vault** en un entorno **Docker Compose** local. Cada arranque de la
aplicación recibe un **usuario único** con **TTL de 1 hora**, en lugar de una contraseña
estática en `application.yml`.

## Antes vs después

| Antes (inseguro) | Después (esta demo) |
|------------------|---------------------|
| Usuario/clave fijos de PostgreSQL en `application.yml` | Solo la **URL** de la BD es estática |
| El mismo secreto para siempre, compartido | Vault emite un usuario **efímero por instancia**, con TTL y revocación automática |
| Rotar = editar config + redeploy | Rotación transparente vía **lease renewal** de Spring Cloud Vault |

## Arquitectura

```
  Spring Boot (:8086)
        │  1) login AppRole (role-id + secret-id)
        ▼
  HashiCorp Vault (:8200) ── motor "database" ──▶ PostgreSQL (:5432)
        │  2) read database/creds/app-role            ▲
        │  3) Vault CREA un usuario temporal ─────────┘  (CREATE ROLE ... VALID UNTIL)
        ▼
  Spring inyecta ese usuario/clave en spring.datasource.* (TTL 1h)
```

| Servicio | URL |
|----------|-----|
| API Spring | http://localhost:8086 |
| Vault UI | http://localhost:8200 (token `dev-token`) |
| PostgreSQL | localhost:5432 (admin `vault-admin` / `vault-admin-pass`) |

> **Vault corre en modo `dev`**: en memoria, auto-unseal y root token fijo. Solo para
> laboratorio, **nunca en producción**.

---

## Cómo levantarlo (un solo comando)

El propio contenedor de **Vault se autoconfigura al arrancar** (`vault/entrypoint.sh` →
`vault/setup.sh`): habilita el motor de BD, crea el rol dinámico y el AppRole con
`role-id`/`secret-id` fijos. No hay pasos manuales:

```bash
cd sesion3-lab/spring-keyvault
./compose.sh up --build
```

El `app` espera (vía healthcheck) a que `spring-vault` quede **healthy**, lo que solo ocurre
cuando la configuración terminó. Funciona igual con `docker compose` y con `podman-compose`.

### Windows — cmd (sin PowerShell)

```cmd
cd sesion3-lab\spring-keyvault
docker compose up --build
```

---

## Cómo probar (la demostración clave)

```bash
# Usuario REAL con el que la app habla con PostgreSQL: lo generó Vault
curl http://localhost:8086/api/db/whoami
```

Respuesta esperada — fíjate en `vaultGeneratedDbUser`, empieza por `v-approle-app-role-`:

```json
{
  "app": "spring-vault-demo",
  "vaultGeneratedDbUser": "v-approle-app-role-3Qk9mZ2x1a-1718000000",
  "explicacion": "El usuario empieza por 'v-approle-' => credencial dinamica emitida por Vault",
  "timestamp": "..."
}
```

```bash
# Datos leídos con la credencial dinámica (solo permiso SELECT)
curl http://localhost:8086/api/products

# Roles efímeros que Vault ha ido creando en PostgreSQL
curl http://localhost:8086/api/db/dynamic-roles
```

### Verlo desde dentro de Vault

```bash
# Pide una credencial nueva manualmente (verás user/pass distintos y lease_duration=1h)
docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN=dev-token \
  spring-vault vault read database/creds/app-role
```

### Verlo desde PostgreSQL

```bash
docker exec -it spring-vault-postgres \
  psql -U vault-admin -d appdb -c "SELECT rolname, rolvaliduntil FROM pg_roles WHERE rolname LIKE 'v-%';"
```

Cada usuario `v-approle-app-role-...` tiene un `rolvaliduntil` ~1 hora en el futuro.

---

## Cómo funciona (piezas)

| Pieza | Archivo | Rol |
|-------|---------|-----|
| Orquestación | `docker-compose.yml` | PostgreSQL + Vault (autoconfigurado) + app |
| Arranque de Vault | `vault/entrypoint.sh` | Lanza Vault dev y dispara la configuración en el mismo contenedor |
| Configuración de Vault | `vault/setup.sh` (y `vault/setup.ps1` manual) | Habilita motor `database`, crea rol `app-role` (TTL 1h) y AppRole `spring-app` |
| Política mínima | `vault/app-policy.hcl` | Solo permite leer `database/creds/app-role` |
| Datos demo | `postgres/init.sql` | Tabla `products` (los roles dinámicos solo tienen `SELECT`) |
| Arranque Spring | `app/src/main/resources/bootstrap.yml` | Login AppRole + motor `database` de Spring Cloud Vault |
| Demostración | `app/.../web/CredentialsController.java` | Expone el usuario dinámico y los datos |

El secreto del enfoque está en `bootstrap.yml`: Spring Cloud Vault se autentica con
**AppRole** y pide credenciales al **motor `database`** *antes* de crear el `DataSource`,
de modo que Hikari nunca ve una contraseña escrita en el repo.

```yaml
spring:
  cloud:
    vault:
      authentication: APPROLE
      app-role:
        role-id: ${VAULT_ROLE_ID}
        secret-id: ${VAULT_SECRET_ID}
      database:
        enabled: true
        backend: database
        role: app-role
      config:
        lifecycle:
          enabled: true   # renueva el lease y rota al expirar el TTL
```

---

## Rotación y caducidad del lease (WARN `Cannot renew lease`)

Si dejas la app corriendo verás en algún momento:

```
WARN ... LeaseEventPublisher$LoggingErrorListener : ... Cannot renew lease: Status 400 Bad Request lease expired
```

**Es esperado**, no un bug. La credencial dinámica tiene `default_ttl=1h` y `max_ttl=24h`:

- Spring Cloud Vault **renueva** el lease de forma periódica para mantener vivo el usuario
  (hasta el `max_ttl` de 24h).
- Cuando se agota el `max_ttl`, o cuando la máquina **se suspende/duerme** y el planificador
  de renovación no corre a tiempo, el lease caduca. Al despertar, Spring intenta renovar un
  lease **ya expirado** → `400 lease expired`. (Por eso aparece tras una noche, con salto de
  fecha en los logs.)

### Limitación importante (didáctica)

Las credenciales se inyectan en `spring.datasource.*` **una sola vez al arrancar**, y
**HikariCP** las cachea. Aunque Vault rote la credencial, el pool **no** adopta la nueva
automáticamente. Es decir: cuando el usuario dinámico se revoca, el pool deja de poder
conectar.

### Recuperación

Reinicia la app: al arrancar pide una credencial nueva.

```bash
podman restart spring-vault-app   # o: ./compose.sh restart app
```

### En producción

Para sobrevivir a la rotación **sin reiniciar** se usa un `DataSource` que refresca las
credenciales (p. ej. bean `@RefreshScope` + evento de rotación de Spring Cloud Vault, o un
pool que pida la credencial a Vault en cada nueva conexión). Queda fuera del alcance de esta
demo, cuyo objetivo es **mostrar** que la credencial es efímera y con TTL.

> Los servicios llevan `restart: unless-stopped`, así que el stack vuelve solo tras reiniciar
> la máquina (pero la app seguirá necesitando credencial nueva, que obtiene al rearrancar).

## De laboratorio a producción

| Aspecto | En la demo | En producción |
|---------|------------|---------------|
| Vault | modo `dev` en memoria | clúster con almacenamiento + auto-unseal (KMS) |
| `role-id`/`secret-id` | fijos en `.env` | `secret-id` de un solo uso/efímero, entregado por un *trusted orchestrator* (CI, K8s) |
| Auth | AppRole | AppRole, **Kubernetes** o **JWT/OIDC** |
| TTL | 1 h | ajustado a la app + renovación automática |

Para el flujo real (sin valores fijos):

```bash
# role-id (estable, no secreto)
vault read auth/approle/role/spring-app/role-id

# secret-id nuevo (secreto, de un solo uso)
vault write -f auth/approle/role/spring-app/secret-id
```

---

## Apagar y limpiar

```bash
./compose.sh down
```
