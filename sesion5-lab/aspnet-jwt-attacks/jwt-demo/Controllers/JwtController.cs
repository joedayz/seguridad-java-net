using JwtDemo.Services;
using Microsoft.AspNetCore.Mvc;

namespace JwtDemo.Controllers;

[ApiController]
[Route("api/jwt")]
public class JwtController : ControllerBase
{
    private readonly VulnerableJwtService _vulnerable;
    private readonly SecureJwtService _secure;

    public JwtController(VulnerableJwtService vulnerable, SecureJwtService secure)
    {
        _vulnerable = vulnerable;
        _secure = secure;
    }

    [HttpPost("vulnerable/issue")]
    public IActionResult IssueVulnerable([FromBody] Dictionary<string, string> body)
    {
        var subject = body.GetValueOrDefault("subject", "user1");
        var role = body.GetValueOrDefault("role", "USER");
        return Ok(new
        {
            modo = "VULNERABLE",
            token = _vulnerable.IssueToken(subject, role),
            hint = "Prueba alg=none: eyJhbGciOiJubmUifQ.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9."
        });
    }

    [HttpGet("vulnerable/verify")]
    public IActionResult VerifyVulnerable([FromHeader(Name = "Authorization")] string authorization)
    {
        var token = authorization.Replace("Bearer ", "");
        return Ok(new { modo = "VULNERABLE", claims = _vulnerable.Validate(token) });
    }

    [HttpPost("seguro/issue")]
    public IActionResult IssueSecure([FromBody] Dictionary<string, string> body)
    {
        var subject = body.GetValueOrDefault("subject", "user1");
        var role = body.GetValueOrDefault("role", "USER");
        return Ok(new
        {
            modo = "SEGURO",
            token = _secure.IssueToken(subject, role)
        });
    }

    [HttpGet("seguro/verify")]
    public IActionResult VerifySecure([FromHeader(Name = "Authorization")] string authorization)
    {
        var token = authorization.Replace("Bearer ", "");
        try
        {
            return Ok(new { modo = "SEGURO", claims = _secure.Validate(token) });
        }
        catch (Exception ex)
        {
            return Unauthorized(new { modo = "SEGURO", valid = false, error = ex.Message });
        }
    }
}
