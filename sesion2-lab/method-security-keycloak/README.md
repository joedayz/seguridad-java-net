# Demo Keycloak + Spring Method Security

Ilustra **autorización a nivel de método** con `@PreAuthorize` y `@PostAuthorize` (como en la diapositiva de *Method Security*), sobre un resource server que valida JWT de Keycloak.

## Arquitectura

```
  cliente (curl)
        │  password grant
        ▼
  Keycloak :8090  ──JWT──▶  Spring Boot :8086
                            @PreAuthorize / @PostAuthorize en servicios
```

| Servicio | URL |
|----------|-----|
| Keycloak | http://localhost:8090 (admin / admin) |
| API | http://localhost:8086 |

### Usuarios (realm `demo`)

| Usuario | Password | Roles |
|---------|----------|-------|
| `alice` | `password` | ADMIN, USER |
| `bob` | `password` | USER |

### Reglas de autorización (capa servicio)

| Endpoint | Anotación | Comportamiento |
|----------|-----------|----------------|
| `GET /api/users/{userId}` | `@PreAuthorize` | ADMIN ve cualquier usuario; USER solo si `#userId == authentication.name` |
| `GET /api/documents/{id}` | `@PostAuthorize` | Tras cargar el documento, solo si `returnObject.ownerId == authentication.name` |

Documentos de prueba: id `1` → propietario `alice`; id `2` → propietario `bob`.

## Levantar

```bash
chmod +x compose.sh get-token.sh
./compose.sh up --build
```

## Probar

```bash
# Público
curl http://localhost:8086/api/public/hello

ALICE=$(./get-token.sh alice)
BOB=$(./get-token.sh bob)

# alice (ADMIN) puede leer bob
curl -H "Authorization: Bearer $ALICE" http://localhost:8086/api/users/bob

# bob (USER) NO puede leer alice → 403
curl -s -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $BOB" http://localhost:8086/api/users/alice

# bob puede leer su propio documento (id=2)
curl -H "Authorization: Bearer $BOB" http://localhost:8086/api/documents/2

# bob NO puede leer documento de alice (id=1) → 403
curl -s -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $BOB" http://localhost:8086/api/documents/1
```

## Código destacado

- `MethodSecurityConfig` — `@EnableMethodSecurity`
- `UserService.getUser` — `@PreAuthorize("hasRole('ADMIN') or ...")`
- `DocumentService.getDocument` — `@PostAuthorize("returnObject.ownerId == authentication.name")`

## Comparación con `spring-security`

| Demo | Puerto API | Autorización |
|------|------------|--------------|
| `spring-security` | 8081 | HTTP (`authorizeHttpRequests`) |
| `method-security-keycloak` | 8086 | Método (`@PreAuthorize` / `@PostAuthorize`) |
