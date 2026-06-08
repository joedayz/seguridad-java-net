using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;

namespace RateLimitDemo.Controllers;

[ApiController]
[Route("api")]
public class ApiController : ControllerBase
{
    [HttpGet("datos-sensibles")]
    [EnableRateLimiting("api-policy")]
    public IActionResult ObtenerDatos()
    {
        return Ok(new
        {
            message = "Datos servidos correctamente (dentro del limite de peticiones).",
            timestamp = DateTimeOffset.UtcNow
        });
    }

    // Endpoint sin rate limiting, para comparar.
    [HttpGet("publico")]
    public IActionResult Publico()
    {
        return Ok(new
        {
            message = "Endpoint sin rate limiting: responde siempre.",
            timestamp = DateTimeOffset.UtcNow
        });
    }
}
