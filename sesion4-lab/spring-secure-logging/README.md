# Demo Logging seguro — Spring Boot (antes / después)

Dos anti-patrones del material de la sesión:

1. **Logging de datos sensibles** (contraseñas, JWT, tarjeta)
2. **Exposición de excepciones** al cliente (stack trace / detalles internos)

| Tema | ANTES — vulnerable | DESPUÉS — seguro |
|------|-------------------|------------------|
| Logging | `POST /api/auth/vulnerable/login` | `POST /api/auth/seguro/login` |
| Errores HTTP | `GET /api/orders/vulnerable/{id}` | `GET /api/orders/seguro/{id}` |

| Servicio | URL |
|----------|-----|
| API | http://localhost:8195 |

La respuesta incluye `lineasLog` para ver qué se escribió sin mirar los logs del contenedor.

---

## Código vulnerable (MAL)

```java
log.info(
    "Login attempt username={}, password={}, token={}, card={}",
    username, password, jwtToken, creditCardNumber);
```

## Código seguro (BIEN)

```java
log.info("Login attempt username={}, cardLast4={}", username, maskCard(creditCardNumber));
log.info("Login outcome username={}, success={}", username, ok);
// password y jwtToken NUNCA se registran
```

---

## Cómo levantarlo

Requisitos: Docker Desktop o Podman con `compose`.

```bash
cd spring-secure-logging
docker compose up --build
```

Alternativa: `./compose.sh up --build` · Parar: `docker compose down`

---

## Cómo probar

Payload de ejemplo (datos ficticios):

```bash
curl -s -X POST http://localhost:8195/api/auth/vulnerable/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ana",
    "password": "Secr3t!",
    "jwtToken": "eyJhbGciOiJIUzI1NiJ9.payload.firma",
    "creditCardNumber": "4111111111111111"
  }' | jq .
```

→ `lineasLog` muestra **password**, **token** y **tarjeta completa**.

```bash
curl -s -X POST http://localhost:8195/api/auth/seguro/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ana",
    "password": "Secr3t!",
    "jwtToken": "eyJhbGciOiJIUzI1NiJ9.payload.firma",
    "creditCardNumber": "4111111111111111"
  }' | jq .
```

→ Solo `username`, `cardLast4=****1111` y resultado; sin secretos.

### Errores HTTP (stack trace)

```bash
# Expone mensaje interno + stack trace completo
curl -s http://localhost:8195/api/orders/vulnerable/abc | jq .

# Respuesta generica + errorId (detalle solo en logs del servidor)
curl -s http://localhost:8195/api/orders/seguro/abc | jq .
```

---

## Windows — cmd y curl.exe (sin PowerShell)

```cmd
docker compose up --build
curl.exe -s -X POST http://localhost:8195/api/auth/vulnerable/login -H "Content-Type: application/json" -d "{\"username\":\"ana\",\"password\":\"Secr3t!\",\"jwtToken\":\"eyJ...\",\"creditCardNumber\":\"4111111111111111\"}"
```
