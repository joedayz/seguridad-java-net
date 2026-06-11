using Microsoft.AspNetCore.Diagnostics;

namespace LoggingDemo.Handlers;

/// <summary>
/// BIEN — manejo centralizado (paridad con @RestControllerAdvice de Spring).
/// Solo aplica a endpoints que dejan propagar la excepcion (p. ej. /api/orders/seguro/**).
/// </summary>
public class GlobalExceptionHandler : IExceptionHandler
{
    private readonly ILogger<GlobalExceptionHandler> _logger;

    public GlobalExceptionHandler(ILogger<GlobalExceptionHandler> logger)
    {
        _logger = logger;
    }

    public async ValueTask<bool> TryHandleAsync(
        HttpContext httpContext,
        Exception exception,
        CancellationToken cancellationToken)
    {
        var errorId = Guid.NewGuid().ToString("N")[..8];

        _logger.LogError(
            exception,
            "Unhandled exception {Method} {Path} errorId={ErrorId}",
            httpContext.Request.Method,
            httpContext.Request.Path,
            errorId);

        httpContext.Response.StatusCode = StatusCodes.Status500InternalServerError;
        await httpContext.Response.WriteAsJsonAsync(new
        {
            modo = "SEGURO — IExceptionHandler (sin stack trace ni mensaje interno)",
            message = "Ha ocurrido un error inesperado.",
            code = "ERR_INTERNAL_SERVER_ERROR",
            errorId
        }, cancellationToken);

        return true;
    }
}
