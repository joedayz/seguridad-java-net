using System.Diagnostics;
using System.Text.Json;
using AuditDemo.Support;

namespace AuditDemo.Middleware;

public class SecurityAuditMiddleware
{
    private readonly RequestDelegate _next;

    public SecurityAuditMiddleware(RequestDelegate next) => _next = next;

    public async Task InvokeAsync(HttpContext context, LogCapture logCapture)
    {
        if (!context.Request.Path.StartsWithSegments("/api/seguro"))
        {
            await _next(context);
            return;
        }

        var sw = Stopwatch.StartNew();
        await _next(context);
        sw.Stop();

        var correlationId = context.Items["CorrelationId"]?.ToString();
        var audit = new
        {
            timestamp = DateTime.UtcNow.ToString("o"),
            level = context.Response.StatusCode >= 400 ? "WARN" : "INFO",
            event_type = "ACCESS",
            correlation_id = correlationId,
            user_id = context.User?.Identity?.Name
                       ?? context.Request.Headers["X-User-Id"].FirstOrDefault()
                       ?? "anonymous",
            resource = context.Request.Path.Value,
            http_method = context.Request.Method,
            status_code = context.Response.StatusCode,
            duration_ms = sw.ElapsedMilliseconds,
            client_ip = context.Connection.RemoteIpAddress?.ToString()
        };

        var line = JsonSerializer.Serialize(audit);
        logCapture.Add(line);
    }
}
