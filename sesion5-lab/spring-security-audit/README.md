# Demo Auditoría de seguridad — Spring Boot

Logging de acceso y eventos de seguridad según la **Sesión 5 · Bloque 1**: texto libre vs JSON estructurado,
`SecurityAuditListener`, logger dedicado `SECURITY_AUDIT` y **Correlation ID**.

| Tema | ANTES — vulnerable | DESPUÉS — seguro |
|------|-------------------|------------------|
| Login | `POST /api/auth/vulnerable/login` | `POST /api/auth/seguro/login` |
| Acceso HTTP | `GET /api/orders/vulnerable/{id}` | `GET /api/orders/seguro/{id}` |

| Servicio | URL |
|----------|-----|
| API | http://localhost:8199 |

La respuesta incluye `lineasLog` y `correlationId` (solo en rutas seguras).

Usuario de prueba: `ana` / `Secr3t!`

---

## Qué demuestra

**MAL:** mensajes de texto libre, password en el log, sin `correlation_id`, sin campos para SIEM.

**BIEN:** eventos JSON con `event_type`, `correlation_id`, `client_ip`, `http_method`, `status_code`, `duration_ms`.
Logger separado `SECURITY_AUDIT` (destino distinto en producción: Elasticsearch, Splunk…).

---

## Cómo levantarlo

```bash
cd spring-security-audit
docker compose up --build
```

Alternativa: `./compose.sh up --build`

---

## Cómo probar

**Login vulnerable — password en log**

```bash
curl -s -X POST http://localhost:8199/api/auth/vulnerable/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ana","password":"Secr3t!"}' | jq .
```

**Login seguro — JSON estructurado + Correlation ID**

```bash
curl -s -X POST http://localhost:8199/api/auth/seguro/login \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: req-demo-001" \
  -d '{"username":"ana","password":"Secr3t!"}' | jq .
```

**Intento fallido (WARN / AUTH_FAILURE)**

```bash
curl -s -X POST http://localhost:8199/api/auth/seguro/login \
  -H "Content-Type: application/json" \
  -d '{"username":"luis","password":"bad"}' | jq .
```

**Acceso a recurso — comparar contexto**

```bash
curl -s http://localhost:8199/api/orders/vulnerable/1001 | jq .

curl -s http://localhost:8199/api/orders/seguro/1001 \
  -H "X-Correlation-ID: req-demo-002" \
  -H "X-User-Id: usr_0042" | jq .
```

---

## Herramientas adicionales (opcional en clase)

Para el bloque de monitorización centralizada no hace falta instalar nada para la demo base.
Opcionalmente puedes instalar:

| Herramienta | Uso en la sesión |
|-------------|------------------|
| [Semgrep](https://semgrep.dev/docs/getting-started/) | SAST — ver `../scripts/semgrep-scan.sh` |
| [OWASP ZAP](https://www.zaproxy.org/download/) o Docker | DAST — ver `../scripts/zap-baseline.sh` |
| [Gitleaks](https://github.com/gitleaks/gitleaks) | Escaneo de secretos en Git |

---

## Windows — cmd y curl.exe (sin PowerShell)

```cmd
docker compose up --build
curl.exe -s -X POST http://localhost:8199/api/auth/seguro/login -H "Content-Type: application/json" -H "X-Correlation-ID: req-demo-001" -d "{\"username\":\"ana\",\"password\":\"Secr3t!\"}"
```
