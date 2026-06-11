# Demo Validación — ASP.NET Core (antes / después)

Validación de entrada con **Data Annotations** y **FluentValidation**.

| Variante | Endpoint |
|----------|----------|
| **ANTES — vulnerable** | `POST /api/users/vulnerable` |
| **DESPUÉS — Data Annotations** | `POST /api/users/seguro-anotaciones` |
| **DESPUÉS — FluentValidation** | `POST /api/users/seguro-fluent` |

| Servicio | URL |
|----------|-----|
| API | http://localhost:8190 |

---

## Data Annotations en el modelo

```csharp
public class CreateUserRequestAnnotated
{
    [Required]
    [StringLength(50, MinimumLength = 3)]
    [RegularExpression(@"^[a-zA-Z0-9_-]+$")]
    public string Username { get; set; }

    [Required, EmailAddress]
    public string Email { get; set; }

    [Range(18, 120)]
    public int Age { get; set; }
}
```

Con `[ApiController]`, ASP.NET Core devuelve **400** automaticamente si falla.

## FluentValidation

```csharp
public class CreateUserValidator : AbstractValidator<CreateUserRequestFluent>
{
    public CreateUserValidator()
    {
        RuleFor(x => x.Username)
            .NotEmpty().Length(3, 50)
            .Matches(@"^[a-zA-Z0-9_-]+$");
        RuleFor(x => x.Email).NotEmpty().EmailAddress();
        RuleFor(x => x.Age).InclusiveBetween(18, 120);
    }
}
```

Registro en `Program.cs`:

```csharp
builder.Services.AddFluentValidationAutoValidation();
builder.Services.AddValidatorsFromAssemblyContaining<CreateUserValidator>();
```

> FluentValidation separa las reglas del modelo y permite reutilizarlas en tests
> unitarios sin el framework web.

---

## Cómo levantarlo

Requisitos: Docker Desktop o Podman con `compose`.

```bash
cd aspnet-bean-validation
docker compose up --build
```

Alternativa: `./compose.sh up --build` · Parar: `docker compose down`

---

## Cómo probar

```bash
# Valido → 201
curl -s -X POST http://localhost:8190/api/users/seguro-fluent \
  -H "Content-Type: application/json" \
  -d '{"username":"ana_garcia","email":"ana@acme.com","age":30}'

# Invalido
curl -s -X POST http://localhost:8190/api/users/vulnerable \
  -H "Content-Type: application/json" \
  -d '{"username":"a","email":"no-es-email","age":10}'

curl -s -X POST http://localhost:8190/api/users/seguro-anotaciones \
  -H "Content-Type: application/json" \
  -d '{"username":"a","email":"no-es-email","age":10}'
```

Vulnerable → **201**. Anotaciones / Fluent → **400** con detalle de errores.

---

## Windows — cmd y curl.exe (sin PowerShell)

```cmd
cd aspnet-bean-validation
docker compose up --build
curl.exe -s -X POST http://localhost:8190/api/users/seguro-fluent -H "Content-Type: application/json" -d "{\"username\":\"ana_garcia\",\"email\":\"ana@acme.com\",\"age\":30}"
```
