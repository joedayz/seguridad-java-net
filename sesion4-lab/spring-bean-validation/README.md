# Demo Bean Validation — Spring Boot (antes / después)

Validación de entrada con **Jakarta Validation API** (`spring-boot-starter-validation`).

| Variante | Endpoint | Comportamiento |
|----------|----------|----------------|
| **ANTES — vulnerable** | `POST /api/users/vulnerable` | Sin `@Valid` — acepta datos inválidos |
| **DESPUÉS — seguro** | `POST /api/users/seguro` | Con `@Valid` — **400** si falla la validación |

| Servicio | URL |
|----------|-----|
| API | http://localhost:8189 |

---

## DTO — `CreateUserRequest`

```java
public record CreateUserRequest(
    @NotBlank @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Solo caracteres alfanumericos")
    String username,

    @NotBlank @Email
    String email,

    @NotNull @Min(18) @Max(120)
    Integer age
) {}
```

## Controlador seguro

```java
@PostMapping("/seguro")
public ResponseEntity<?> createSeguro(@Valid @RequestBody CreateUserRequest req) {
    userService.create(req);
    return ResponseEntity.status(201).build();
}
```

Si la validación falla, Spring lanza `MethodArgumentNotValidException` y devuelve **400**
con el detalle de errores por campo.

---

## Cómo levantarlo

```bash
docker compose up --build
```

---

## Cómo probar

**Usuario válido (ambos endpoints → 201):**

```bash
curl -s -X POST http://localhost:8189/api/users/seguro \
  -H "Content-Type: application/json" \
  -d '{"username":"ana_garcia","email":"ana@acme.com","age":30}'
```

**Datos inválidos** (`username` con espacios, email malo, `age` menor de 18):

```bash
curl -s -X POST http://localhost:8189/api/users/vulnerable \
  -H "Content-Type: application/json" \
  -d '{"username":"a","email":"no-es-email","age":10}'
```

→ **Vulnerable** acepta y devuelve `201`.

```bash
curl -s -X POST http://localhost:8189/api/users/seguro \
  -H "Content-Type: application/json" \
  -d '{"username":"a","email":"no-es-email","age":10}'
```

→ **Seguro** devuelve `400` con `errores` por campo.

---

## Regla de oro

Valida **siempre** la entrada en el servidor con `@Valid` / `@Validated`. La validación
solo en el cliente no es suficiente.
