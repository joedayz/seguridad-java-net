using Microsoft.AspNetCore.Mvc;

namespace KeyVaultDemo.Controllers;

[ApiController]
[Route("api/public")]
public class PublicController : ControllerBase
{
    [HttpGet("hello")]
    public IActionResult Hello()
    {
        return Ok(new
        {
            message = "API Key Vault demo — endpoint publico.",
            hint = "Usa GET /api/config/status para comprobar que los secretos vienen del vault."
        });
    }
}
