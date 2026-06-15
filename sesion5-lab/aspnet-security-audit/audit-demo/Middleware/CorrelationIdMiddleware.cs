namespace AuditDemo.Middleware;

public class CorrelationIdMiddleware
{
    private const string Header = "X-Correlation-ID";
    private readonly RequestDelegate _next;

    public CorrelationIdMiddleware(RequestDelegate next) => _next = next;

    public async Task InvokeAsync(HttpContext context)
    {
        var correlationId = context.Request.Headers[Header].FirstOrDefault();
        if (string.IsNullOrWhiteSpace(correlationId))
        {
            correlationId = $"req-{Guid.NewGuid():N}"[..12];
        }

        context.Items["CorrelationId"] = correlationId;
        context.Response.Headers[Header] = correlationId;
        await _next(context);
    }
}
