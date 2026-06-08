using Microsoft.AspNetCore.Mvc;

namespace CorsDemo.Controllers;

[ApiController]
[Route("api")]
public class ApiController : ControllerBase
{
    [HttpGet("datos")]
    public IActionResult ObtenerDatos()
    {
        return Ok(new
        {
            message = "Datos servidos correctamente (origen CORS permitido).",
            timestamp = DateTimeOffset.UtcNow
        });
    }

    [HttpPost("datos")]
    public IActionResult CrearDatos([FromBody] object? body)
    {
        return Ok(new
        {
            message = "POST aceptado (origen CORS permitido).",
            received = body,
            timestamp = DateTimeOffset.UtcNow
        });
    }
}
