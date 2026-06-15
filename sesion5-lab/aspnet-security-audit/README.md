# Demo Auditoría de seguridad — ASP.NET Core

Equivalente .NET de la demo Spring: `SecurityAuditMiddleware`, JSON estructurado y **Correlation ID**.

| Tema | ANTES — vulnerable | DESPUÉS — seguro |
|------|-------------------|------------------|
| Login | `POST /api/auth/vulnerable/login` | `POST /api/seguro/auth/login` |
| Acceso HTTP | `GET /api/orders/vulnerable/{id}` | `GET /api/seguro/orders/{id}` |

| Servicio | URL |
|----------|-----|
| API | http://localhost:8200 |

Usuario de prueba: `ana` / `Secr3t!`

---

## Cómo levantarlo

```bash
cd aspnet-security-audit
docker compose up --build
```

---

## Cómo probar

```bash
curl -s -X POST http://localhost:8200/api/auth/vulnerable/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ana","password":"Secr3t!"}' | jq .

curl -s -X POST http://localhost:8200/api/seguro/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: req-demo-001" \
  -d '{"username":"ana","password":"Secr3t!"}' | jq .

curl -s http://localhost:8200/api/seguro/orders/1001 \
  -H "X-Correlation-ID: req-demo-002" \
  -H "X-User-Id: usr_0042" | jq .
```

---

## Windows — cmd y curl.exe

```cmd
docker compose up --build
curl.exe -s -X POST http://localhost:8200/api/seguro/auth/login -H "Content-Type: application/json" -d "{\"username\":\"ana\",\"password\":\"Secr3t!\"}"
```
