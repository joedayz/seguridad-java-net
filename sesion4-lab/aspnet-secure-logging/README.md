# Demo Logging seguro — ASP.NET Core (antes / después)

Dos anti-patrones del material de la sesión:

1. **Loguear PII** (tarjeta, CVV, token) con `ILogger`
2. **Exposición de excepciones** al cliente (stack trace / detalles internos)

| Tema | ANTES — vulnerable | DESPUÉS — seguro |
|------|-------------------|------------------|
| Logging | `POST /api/checkout/vulnerable` | `POST /api/checkout/seguro` |
| Errores HTTP | `GET /api/orders/vulnerable/{id}` | `GET /api/orders/seguro/{id}` |

| Servicio | URL |
|----------|-----|
| API | http://localhost:8196 |

Tarjetas que terminan en `0000` simulan rechazo del banco (para probar el `catch`).

---

## Código vulnerable (MAL)

```csharp
catch (Exception ex)
{
    return Content(
        $"Error: {ex}\n" +
        $"Card={request.CardNumber}\n" +
        $"Cvv={request.Cvv}\n" +
        $"Token={request.CustomerToken}");
}
```

## Código seguro (BIEN)

Checkout — `catch` local sin filtrar PII al cliente:

```csharp
catch (Exception ex)
{
    _logger.LogError(ex, "Checkout failed");
    return StatusCode(500, new { error = "No se pudo procesar el pago." });
}
```

Errores HTTP — `IExceptionHandler` centralizado (`GlobalExceptionHandler.cs`):

```csharp
builder.Services.AddExceptionHandler<GlobalExceptionHandler>();
app.UseExceptionHandler();
```

---

## Cómo levantarlo

Requisitos: Docker Desktop o Podman con `compose`.

```bash
cd aspnet-secure-logging
docker compose up --build
```

Alternativa: `./compose.sh up --build` · Parar: `docker compose down`

---

## Cómo probar

**Pago rechazado — versión vulnerable** (expone tarjeta, CVV y token en texto plano):

```bash
curl -s -X POST http://localhost:8196/api/checkout/vulnerable \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111110000",
    "cvv": "123",
    "customerToken": "tok_live_secret_abc123"
  }'
```

**Mismo escenario — versión segura** (mensaje genérico + log sin PII):

```bash
curl -s -X POST http://localhost:8196/api/checkout/seguro \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111110000",
    "cvv": "123",
    "customerToken": "tok_live_secret_abc123"
  }' | jq .
```

### Errores HTTP (stack trace)

```bash
# Expone mensaje interno + stack trace completo
curl -s http://localhost:8196/api/orders/vulnerable/abc | jq .

# Respuesta generica + errorId (detalle solo en logs del servidor)
curl -s http://localhost:8196/api/orders/seguro/abc | jq .
```

**Pago exitoso** (tarjeta que no termina en 0000):

```bash
curl -s -X POST http://localhost:8196/api/checkout/seguro \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111111111",
    "cvv": "123",
    "customerToken": "tok_live_secret_abc123"
  }' | jq .
```

---

## Windows — cmd y curl.exe (sin PowerShell)

```cmd
docker compose up --build
curl.exe -s -X POST http://localhost:8196/api/checkout/vulnerable -H "Content-Type: application/json" -d "{\"cardNumber\":\"4111111111110000\",\"cvv\":\"123\",\"customerToken\":\"tok_secret\"}"
```
