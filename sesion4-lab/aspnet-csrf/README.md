# Demo CSRF — ASP.NET Core + Razor Pages (antes / después)

Demo de **Cross-Site Request Forgery (CSRF)** y su mitigación en .NET con Anti-Forgery:

| Variante | URL | Protección |
|----------|-----|------------|
| **ANTES — vulnerable** | http://localhost:8186/vulnerable | `[IgnoreAntiforgeryToken]` |
| **DESPUÉS — seguro** | http://localhost:8186/secure | Anti-Forgery (por defecto en Razor Pages) |
| **Sitio malicioso** | http://localhost:8186/attacker | Simula `evil.com` |

Mismo escenario que la demo Java: banco con **1000 EUR** en sesión y transferencia forzada
desde `/attacker`.

## Mitigación (diapositiva)

### `Program.cs` — AntiForgery

```csharp
builder.Services.AddAntiforgery(options =>
{
    options.HeaderName = "X-CSRF-TOKEN"; // Para peticiones AJAX
    options.Cookie.SameSite = SameSiteMode.Strict;
    options.Cookie.SecurePolicy = CookieSecurePolicy.Always; // produccion HTTPS
});
```

En esta demo usamos `SameAsRequest` para que funcione en `http://localhost` sin HTTPS.

### Razor Pages (seguro por defecto)

Los formularios con tag helpers incluyen el token automáticamente:

```html
<form method="post" asp-page-handler="Transfer">
```

Equivalente en MVC:

```csharp
[HttpPost]
[ValidateAntiForgeryToken]
public IActionResult Transfer(TransferViewModel model) { ... }
```

### Versión vulnerable

```csharp
[IgnoreAntiforgeryToken]
public class TransferModel : PageModel { ... }
```

### APIs REST con JWT

Para Minimal APIs / JWT, desactivar AntiForgery es seguro si **todas** las peticiones de
mutación requieren cabecera `Authorization`: el atacante no puede hacer que el navegador la
incluya de forma involuntaria.

---

## Cómo levantarlo

```bash
docker compose up --build
# o: ./compose.sh up --build
```

| Servicio | URL |
|----------|-----|
| Inicio | http://localhost:8186/ |

---

## Cómo probar

1. Abre **Vulnerable** → http://localhost:8186/vulnerable
2. En otra pestaña, **Sitio malicioso** → *Ejecutar ataque CSRF (vulnerable)*
3. Vuelve a **Vulnerable** → saldo **500 EUR**
4. *Restablecer saldo*
5. Abre **Seguro** → repite el ataque → **400 Bad Request** (token Anti-Forgery ausente)

---

## Ejecutar en local

```bash
cd csrf-demo
dotnet run --urls http://localhost:8186
```

---

## Regla de oro

- No uses `[IgnoreAntiforgeryToken]` salvo que sepas lo que haces.
- En Razor Pages y MVC con cookies de sesión, el Anti-Forgery es **obligatorio**.
- Configura `SameSite=Strict` (o `Lax`) en cookies de sesión y del token.
- En APIs puras con JWT en cabecera, CSRF **no aplica**.
