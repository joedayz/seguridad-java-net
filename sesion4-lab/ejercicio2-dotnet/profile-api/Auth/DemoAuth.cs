using System.Security.Claims;

namespace ProfileApi.Auth;

/// <summary>
/// Simula usuario autenticado con cabeceras (para la demo sin montar JWT completo).
/// X-User-Id: GUID del usuario logueado
/// X-User-Role: Admin (opcional)
/// </summary>
public static class DemoAuth
{
    public static Guid? GetCurrentUserId(HttpContext httpContext)
    {
        var raw = httpContext.Request.Headers["X-User-Id"].FirstOrDefault();
        return Guid.TryParse(raw, out var id) ? id : null;
    }

    public static bool IsAdmin(HttpContext httpContext) =>
        string.Equals(httpContext.Request.Headers["X-User-Role"].FirstOrDefault(), "Admin",
            StringComparison.OrdinalIgnoreCase);

    public static ClaimsPrincipal? TryBuildPrincipal(HttpContext httpContext)
    {
        var userId = GetCurrentUserId(httpContext);
        if (userId == null)
        {
            return null;
        }

        var claims = new List<Claim>
        {
            new(ClaimTypes.NameIdentifier, userId.Value.ToString())
        };
        if (IsAdmin(httpContext))
        {
            claims.Add(new Claim(ClaimTypes.Role, "Admin"));
        }

        return new ClaimsPrincipal(new ClaimsIdentity(claims, "Demo"));
    }
}
