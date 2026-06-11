using Microsoft.AspNetCore.Mvc;

namespace HeadersDemo.Controllers;

[ApiController]
[Route("api/secure")]
public class HeadersController : ControllerBase
{
    [HttpGet("check")]
    public IActionResult Check()
    {
        Response.Headers["X-Demo-Variante"] = "secure";

        return Ok(new
        {
            modo = "CON headers de seguridad (middleware de la diapositiva)",
            headersEsperadosEnRespuesta = new Dictionary<string, string>
            {
                ["X-Content-Type-Options"] = "nosniff",
                ["X-Frame-Options"] = "DENY",
                ["X-XSS-Protection"] = "1; mode=block",
                ["Referrer-Policy"] = "strict-origin-when-cross-origin",
                ["Content-Security-Policy"] = "default-src 'self'; script-src 'self'; object-src 'none'",
                ["Strict-Transport-Security"] = "solo en respuestas HTTPS (UseHsts)",
                ["CORS"] = "Origenes: https://myapp.com, https://admin.myapp.com"
            },
            comoVerificar = "curl -i http://localhost:8192/api/secure/check"
        });
    }
}
