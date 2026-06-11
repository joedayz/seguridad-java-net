# Demo Security Headers — ASP.NET Core

Middleware de seguridad HTTP: **sin headers** vs **configuración endurecida** (diapositiva).

| Variante | URL |
|----------|-----|
| **ANTES — sin headers** | `GET /api/insecure/check` |
| **DESPUÉS — seguro** | `GET /api/secure/check` |
| Inicio | http://localhost:8192/ |

Puerto: **8192**

---

## Configuración segura (`Program.cs`)

```csharp
app.UseHsts();
// app.UseHttpsRedirection();  // en produccion con HTTPS

app.Use(async (context, next) =>
{
    context.Response.Headers.XContentTypeOptions = "nosniff";
    context.Response.Headers.XFrameOptions = "DENY";
    context.Response.Headers["X-XSS-Protection"] = "1; mode=block";
    context.Response.Headers["Referrer-Policy"] = "strict-origin-when-cross-origin";
    context.Response.Headers.ContentSecurityPolicy =
        "default-src 'self'; script-src 'self'; object-src 'none'";
    await next();
});

app.UseCors(policy => policy
    .WithOrigins("https://myapp.com", "https://admin.myapp.com")
    .WithMethods("GET", "POST", "PUT", "DELETE")
    .AllowCredentials());
```

En la demo el middleware vive en `SecurityHeadersMiddleware.cs` (`UseSecurityHeaders()`).

---

## Cómo probar

```bash
docker compose up --build

curl -i http://localhost:8192/api/insecure/check
curl -i http://localhost:8192/api/secure/check
```

La respuesta segura incluye `X-Frame-Options`, `Content-Security-Policy`, `Referrer-Policy`, etc.

> **HSTS** (`UseHsts`) solo se envia en HTTPS. En `http://localhost` no aparecera.
