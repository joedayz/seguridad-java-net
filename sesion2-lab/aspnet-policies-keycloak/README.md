# Demo Keycloak + ASP.NET Authorization Policies

Ilustra **políticas de autorización** personalizadas (como en la diapositiva de *Authorization Policies*), combinando rol y claims del JWT emitido por Keycloak.

## Arquitectura

```
  cliente (curl)
        │  password grant
        ▼
  Keycloak :8091  ──JWT──▶  ASP.NET Core :8087
                            [Authorize(Policy = "SeniorDeveloper")]
```

| Servicio | URL |
|----------|-----|
| Keycloak | http://localhost:8091 (admin / admin) |
| API | http://localhost:8087 |

### Política `SeniorDeveloper`

Definida en `Program.cs`:

- Rol `Developer`
- Claim `seniority` = `senior`
- Claim `department` = `engineering`

### Usuarios (realm `demo`)

| Usuario | Password | Rol | seniority | department | `/api/internal/architecture` |
|---------|----------|-----|-----------|------------|------------------------------|
| `carol` | `password` | Developer | senior | engineering | **200** |
| `dave` | `password` | Developer | junior | engineering | **403** (falta seniority) |
| `alice` | `password` | ADMIN, USER | — | — | **403** (no es Developer) |

Los atributos `seniority` y `department` se mapean al JWT mediante protocol mappers en el client `demo-client`.

## Levantar

```bash
chmod +x compose.sh get-token.sh
./compose.sh up --build
```

## Probar

```bash
curl http://localhost:8087/api/public/hello

CAROL=$(./get-token.sh carol)
DAVE=$(./get-token.sh dave)

curl http://localhost:8087/api/me -H "Authorization: Bearer $CAROL"

# carol cumple la politica SeniorDeveloper
curl http://localhost:8087/api/internal/architecture -H "Authorization: Bearer $CAROL"

# dave es Developer pero junior → 403
curl -s -o /dev/null -w "%{http_code}\n" \
  -H "Authorization: Bearer $DAVE" \
  http://localhost:8087/api/internal/architecture
```

## Código destacado

```csharp
builder.Services.AddAuthorization(options => {
  options.AddPolicy("SeniorDeveloper", policy =>
    policy.RequireRole("Developer")
      .RequireClaim("seniority", "senior")
      .RequireClaim("department", "engineering"));
});

[Authorize(Policy = "SeniorDeveloper")]
[HttpGet("internal/architecture")]
public IActionResult GetArchitectureDocs() { ... }
```

## Comparación con `aspnet-identity-keycloak`

| Demo | Puerto API | Autorización |
|------|------------|--------------|
| `aspnet-identity-keycloak` | 8085 | `[Authorize(Roles = "ADMIN")]` |
| `aspnet-policies-keycloak` | 8087 | `[Authorize(Policy = "SeniorDeveloper")]` (rol + claims) |
