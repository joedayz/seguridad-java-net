namespace HeadersDemo.Middleware;

/// <summary>
/// Middleware de seguridad en .NET (equivalente a headers en Spring Security).
/// X-Content-Type-Options, X-Frame-Options, CSP, Referrer-Policy, X-XSS-Protection.
/// </summary>
public class SecurityHeadersMiddleware
{
    private readonly RequestDelegate next;

    public SecurityHeadersMiddleware(RequestDelegate next)
    {
        this.next = next;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        var headers = context.Response.Headers;

        // NWebsec o middleware propio — nosniff
        headers.XContentTypeOptions = "nosniff";
        headers.XFrameOptions = "DENY";
        headers["X-XSS-Protection"] = "1; mode=block";
        headers["Referrer-Policy"] = "strict-origin-when-cross-origin";
        headers.ContentSecurityPolicy = "default-src 'self'; script-src 'self'; object-src 'none'";

        await next(context);
    }
}

public static class SecurityHeadersMiddlewareExtensions
{
    public static IApplicationBuilder UseSecurityHeaders(this IApplicationBuilder app) =>
        app.UseMiddleware<SecurityHeadersMiddleware>();
}
