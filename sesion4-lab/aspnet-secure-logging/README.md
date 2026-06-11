# Demo Logging seguro — ASP.NET Core (antes / después)

Dos anti-patrones del checkout:

1. **Loguear PII** (tarjeta, CVV, token) con `ILogger`
2. **Devolver datos sensibles** en la respuesta de error al cliente

| Variante | Endpoint |
|----------|----------|
| **ANTES — vulnerable** | `POST /api/checkout/vulnerable` |
| **DESPUÉS — seguro** | `POST /api/checkout/seguro` |

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

```csharp
catch (Exception ex)
{
    _logger.LogError(ex, "Checkout failed");
    return StatusCode(500, new { error = "No se pudo procesar el pago." });
}
```

---

## Cómo levantarlo

```bash
docker compose up --build
```

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
