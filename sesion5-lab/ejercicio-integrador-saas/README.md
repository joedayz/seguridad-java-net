# Laboratorio integrador SaaS B2B — Sesión 5 · Bloque 3

Arquitectura backend típica con **hallazgos reales** del material: Actuator expuesto, IDOR,
secretos en configuración, CORS permisivo y matriz de riesgos priorizada.

Puerto: **8208**

| Hallazgo | Cómo reproducirlo en la demo |
|----------|------------------------------|
| Actuator `/env` con secretos | `GET /actuator/env` |
| IDOR en Orders | `GET /api/v1/orders/vulnerable/{id}` |
| Corrección IDOR | `GET /api/v1/orders/seguro/{id}` + `X-User-Id` |
| Secretos en repo | `application.yml` + script Gitleaks |
| CORS `*` | Cabecera `Origin` en peticiones autenticadas |
| Matriz CVSS | `GET /api/audit/matriz-riesgos` |

Usuarios de prueba:

| Usuario | X-User-Id | Pedido propio |
|---------|-----------|---------------|
| Ana | `usr_ana` | 1001 |
| Luis | `usr_luis` | 1002 |

---

## Metodología de revisión (5 capas)

1. **Perímetro** — TLS, headers, rate limit, CORS
2. **Infraestructura** — contenedores, K8s, escaneo Trivy
3. **Servicios internos** — mTLS, OAuth2 client credentials
4. **Aplicación** — SAST, DAST, configuración Actuator
5. **Datos** — cifrado, clasificación PII

Este laboratorio concentra hallazgos de las capas **4 (aplicación)** y **5 (datos)**.

---

## Cómo levantarlo

```bash
cd ejercicio-integrador-saas
docker compose up --build
```

---

## Pruebas guiadas

### 1. Actuator expuesto (CVSS 8.6)

```bash
curl -s http://localhost:8208/actuator/env | jq '.propertySources[0].properties | keys[:10]'
```

Busca `jwt.secret`, `stripe.api-key`, `aws.access-key` en la respuesta.

**Remediación (referencia):**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    env:
      show-values: never
server:
  port: 8080
# management.server.port: 8081  # solo red interna
```

### 2. IDOR — acceder al pedido de otro usuario (CVSS 7.5)

Ana lee el pedido de Luis **sin autorización**:

```bash
curl -s http://localhost:8208/api/v1/orders/vulnerable/1002 | jq .
```

→ Devuelve datos de negocio de Luis (`customerName`, `total`).

**Corrección:**

```bash
# Ana solo ve su pedido
curl -s http://localhost:8208/api/v1/orders/seguro/1001 \
  -H "X-User-Id: usr_ana" | jq .

# Ana NO puede ver pedido 1002
curl -s http://localhost:8208/api/v1/orders/seguro/1002 \
  -H "X-User-Id: usr_ana" | jq .
```

→ **404** (no revela si el recurso existe).

### 3. Secretos en Git (CVSS 8.2)

```bash
cd ..
./scripts/scan-secrets.sh ejercicio-integrador-saas
```

Requiere [Gitleaks](https://github.com/gitleaks/gitleaks) instalado (opcional).

### 4. CORS permisivo

```bash
curl -s -I http://localhost:8208/api/v1/orders/vulnerable/1001 \
  -H "Origin: https://evil.example" | grep -i access-control
```

### 5. Matriz de riesgos del curso

```bash
curl -s http://localhost:8208/api/audit/matriz-riesgos | jq .
```

---

## Simulación con OWASP ZAP

Con la API levantada:

```bash
cd ..
./scripts/zap-baseline.sh http://localhost:8208
```

Requiere Docker (imagen oficial `ghcr.io/zaproxy/zaproxy:stable`).

---

## Hoja de ruta priorizada (del material)

| Horizonte | Acciones |
|-----------|----------|
| **0–72 h** | Rotar credenciales, fijar JWT, restringir Actuator, parche IDOR |
| **1–4 sem** | Vault, Network Policies, SAST/SCA en CI, pre-commit anti-secretos |
| **1–3 meses** | Service mesh mTLS, SIEM, pentest trimestral |

---

## Windows — cmd

```cmd
docker compose up --build
curl.exe -s http://localhost:8208/actuator/env
curl.exe -s http://localhost:8208/api/v1/orders/vulnerable/1002
```
