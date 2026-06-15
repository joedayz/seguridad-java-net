# Demo JWT — ASP.NET Core

Equivalente .NET de [spring-jwt-attacks](../spring-jwt-attacks): algoritmo `none`, secreto débil y claims sin validar.

Puerto: **8202**

| Variante | Emitir | Verificar |
|----------|--------|-----------|
| MAL | `POST /api/jwt/vulnerable/issue` | `GET /api/jwt/vulnerable/verify` |
| BIEN | `POST /api/jwt/seguro/issue` | `GET /api/jwt/seguro/verify` |

---

## Cómo levantarlo

```bash
cd aspnet-jwt-attacks
docker compose up --build
```

---

## Ataque alg=none

```bash
curl -s http://localhost:8202/api/jwt/vulnerable/verify \
  -H "Authorization: Bearer eyJhbGciOiJubmUifQ.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9." | jq .

curl -s http://localhost:8202/api/jwt/seguro/verify \
  -H "Authorization: Bearer eyJhbGciOiJubmUifQ.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9." | jq .
```

---

## Windows — cmd

```cmd
curl.exe -s http://localhost:8202/api/jwt/vulnerable/verify -H "Authorization: Bearer eyJhbGciOiJubmUifQ.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9."
```
