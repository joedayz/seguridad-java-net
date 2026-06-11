# Demo Logging seguro — Spring Boot (antes / después)

**No registrar datos sensibles** en logs: contraseñas, tokens JWT, números de tarjeta completos.

| Variante | Endpoint |
|----------|----------|
| **ANTES — vulnerable** | `POST /api/auth/vulnerable/login` |
| **DESPUÉS — seguro** | `POST /api/auth/seguro/login` |

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

```bash
docker compose up --build
```

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

---

## Windows — cmd y curl.exe (sin PowerShell)

```cmd
docker compose up --build
curl.exe -s -X POST http://localhost:8195/api/auth/vulnerable/login -H "Content-Type: application/json" -d "{\"username\":\"ana\",\"password\":\"Secr3t!\",\"jwtToken\":\"eyJ...\",\"creditCardNumber\":\"4111111111111111\"}"
```
