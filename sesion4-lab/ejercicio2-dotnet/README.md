# Ejercicio 2 · .NET — Perfiles de usuario (mal vs corrección)

`ProfileController` con **Dapper** y SQLite. Identifica las vulnerabilidades en la versión MAL.

| Versión | GET perfil | PUT bio |
|---------|------------|---------|
| **MAL** | `GET /api/profile/vulnerable/{userId}` | `PUT /api/profile/vulnerable/{userId}/bio` |
| **BIEN** | `GET /api/profile/seguro/{userId}` | `PUT /api/profile/seguro/{userId}/bio` |

Puerto: **8194**

### Usuarios de prueba

| Usuario | UserId | Cabecera demo |
|---------|--------|---------------|
| Ana | `11111111-1111-1111-1111-111111111111` | `X-User-Id: 11111111-1111-1111-1111-111111111111` |
| Luis | `22222222-2222-2222-2222-222222222222` | `X-User-Id: 22222222-2222-2222-2222-222222222222` |
| Admin | `33333333-3333-3333-3333-333333333333` | `X-User-Id: 33333333-...` + `X-User-Role: Admin` |

---

## Vulnerabilidades (MAL)

### 1. SQL Injection

```csharp
var query = $"SELECT * FROM UserProfiles WHERE UserId = '{userId}'";
```

```bash
# Devuelve el primer perfil (inyeccion en userId)
curl -s "http://localhost:8194/api/profile/vulnerable/'%20OR%20'1'='1"
```

### 2. BOLA / IDOR (Broken Object Level Authorization)

Sin comprobar que el usuario autenticado puede ver o editar ese `userId`. Cualquiera lee la `NotaPrivada` de otro usuario:

```bash
curl -s "http://localhost:8194/api/profile/vulnerable/22222222-2222-2222-2222-222222222222"
```

---

## Corrección (BIEN)

```csharp
[HttpGet("seguro/{userId:guid}")]
public async Task<IActionResult> GetProfileSeguro(Guid userId)
{
    var currentUserId = Guid.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
    if (currentUserId != userId && !User.IsInRole("Admin"))
        return Forbid();

    var profile = await _db.QueryFirstOrDefaultAsync<UserProfile>(
        "SELECT * FROM UserProfiles WHERE UserId = @UserId",
        new { UserId = userId });
    ...
}
```

En la demo, `X-User-Id` / `X-User-Role` simulan el JWT.

```bash
# Ana ve su perfil → OK
curl -s -H "X-User-Id: 11111111-1111-1111-1111-111111111111" \
  "http://localhost:8194/api/profile/seguro/11111111-1111-1111-1111-111111111111"

# Ana intenta ver a Luis → 403
curl -s -H "X-User-Id: 11111111-1111-1111-1111-111111111111" \
  "http://localhost:8194/api/profile/seguro/22222222-2222-2222-2222-222222222222"

# Admin ve cualquier perfil → OK
curl -s -H "X-User-Id: 33333333-3333-3333-3333-333333333333" \
     -H "X-User-Role: Admin" \
  "http://localhost:8194/api/profile/seguro/22222222-2222-2222-2222-222222222222"
```

```bash
# Actualizar bio (solo el propio usuario)
curl -s -X PUT -H "Content-Type: application/json" \
  -H "X-User-Id: 11111111-1111-1111-1111-111111111111" \
  -d '{"bio":"Nueva bio de Ana"}' \
  "http://localhost:8194/api/profile/seguro/11111111-1111-1111-1111-111111111111/bio"
```

---

## Cómo levantarlo

```bash
docker compose up --build
```
