using System.Globalization;
using System.Threading.RateLimiting;
using Microsoft.AspNetCore.RateLimiting;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();

// Valores bajos para que el 429 sea facil de provocar en una demo en vivo.
// La diapositiva usa PermitLimit = 100; aqui lo bajamos a 5. Se puede ajustar con la
// variable de entorno RATELIMIT_PERMIT_LIMIT.
var permitLimit = int.TryParse(builder.Configuration["RateLimit:PermitLimit"], out var p) ? p : 5;
var windowSeconds = int.TryParse(builder.Configuration["RateLimit:WindowSeconds"], out var w) ? w : 60;

builder.Services.AddRateLimiter(options =>
{
    options.AddFixedWindowLimiter("api-policy", o =>
    {
        o.Window = TimeSpan.FromSeconds(windowSeconds);
        o.PermitLimit = permitLimit;
        o.QueueProcessingOrder = QueueProcessingOrder.OldestFirst;
        // QueueLimit = 0 -> el exceso se rechaza de inmediato (mas claro para la demo).
        // La diapositiva usa QueueLimit = 10 (encola hasta 10 peticiones antes de rechazar).
        o.QueueLimit = 0;
    });

    options.RejectionStatusCode = StatusCodes.Status429TooManyRequests;

    // Anadimos la cabecera Retry-After para que el cliente implemente backoff exponencial.
    options.OnRejected = async (context, token) =>
    {
        if (context.Lease.TryGetMetadata(MetadataName.RetryAfter, out var retryAfter))
        {
            context.HttpContext.Response.Headers.RetryAfter =
                ((int)retryAfter.TotalSeconds).ToString(CultureInfo.InvariantCulture);
        }
        else
        {
            context.HttpContext.Response.Headers.RetryAfter =
                windowSeconds.ToString(CultureInfo.InvariantCulture);
        }

        context.HttpContext.Response.ContentType = "application/json";
        await context.HttpContext.Response.WriteAsync(
            "{\"error\":\"too_many_requests\"," +
            "\"message\":\"Has superado el limite de peticiones. Reintenta mas tarde.\"}",
            token);
    };
});

var app = builder.Build();

app.UseRateLimiter();

app.MapControllers();

app.Run();
