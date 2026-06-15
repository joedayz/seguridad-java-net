using System.Text.Json;
using AuditDemo.Models;
using AuditDemo.Services;
using AuditDemo.Support;
using Microsoft.AspNetCore.Mvc;

namespace AuditDemo.Controllers;

[ApiController]
[Route("api")]
public class AuditController : ControllerBase
{
    private readonly VulnerableAuditService _vulnerable;
    private readonly LogCapture _logCapture;

    public AuditController(VulnerableAuditService vulnerable, LogCapture logCapture)
    {
        _vulnerable = vulnerable;
        _logCapture = logCapture;
    }

    [HttpPost("auth/vulnerable/login")]
    public IActionResult LoginVulnerable([FromBody] LoginRequest request)
    {
        _logCapture.Clear();
        var ok = _vulnerable.Login(request.Username, request.Password);
        return Ok(Body("VULNERABLE (texto libre, password en log)", ok ? "LOGIN_OK" : "LOGIN_FAIL", null));
    }

    [HttpPost("seguro/auth/login")]
    public IActionResult LoginSeguro([FromBody] LoginRequest request)
    {
        _logCapture.Clear();
        var ok = request.Username == "ana" && request.Password == "Secr3t!";
        var correlationId = HttpContext.Items["CorrelationId"]?.ToString();

        var audit = new
        {
            timestamp = DateTime.UtcNow.ToString("o"),
            level = ok ? "INFO" : "WARN",
            event_type = ok ? "AUTH_SUCCESS" : "AUTH_FAILURE",
            correlation_id = correlationId,
            user_id = request.Username,
            reason = ok ? "LOGIN_OK" : "INVALID_CREDENTIALS"
        };

        _logCapture.Add(JsonSerializer.Serialize(audit));

        return ok
            ? Ok(Body("SEGURO (JSON estructurado + correlation ID)", "LOGIN_OK", correlationId))
            : Unauthorized(Body("SEGURO (JSON estructurado + correlation ID)", "LOGIN_FAIL", correlationId));
    }

    [HttpGet("orders/vulnerable/{id}")]
    public IActionResult OrderVulnerable(string id)
    {
        _logCapture.Clear();
        _vulnerable.LogAccess("anonymous", $"/api/orders/vulnerable/{id}", 200);
        return Ok(Body("VULNERABLE", new { orderId = id, total = 149.99m }, null));
    }

    [HttpGet("seguro/orders/{id}")]
    public IActionResult OrderSeguro(string id)
    {
        _logCapture.Clear();
        return Ok(Body(
            "SEGURO (SecurityAuditMiddleware registra ACCESS)",
            new { orderId = id, total = 149.99m },
            HttpContext.Items["CorrelationId"]?.ToString()));
    }

    private object Body(string modo, object resultado, string? correlationId) => new
    {
        modo,
        resultado,
        correlationId,
        lineasLog = _logCapture.Snapshot()
    };
}
