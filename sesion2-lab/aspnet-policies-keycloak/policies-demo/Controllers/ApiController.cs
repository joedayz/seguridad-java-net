using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace PoliciesDemo.Controllers;

[ApiController]
[Route("api")]
public class ApiController : ControllerBase
{
    [HttpGet("public/hello")]
    [AllowAnonymous]
    public IActionResult PublicHello()
    {
        return Ok(new
        {
            message = "Demo ASP.NET Authorization Policies + Keycloak",
            scope = "public",
            hint = "La politica SeniorDeveloper exige rol Developer + claims seniority y department"
        });
    }

    [HttpGet("me")]
    [Authorize]
    public IActionResult Me()
    {
        var username = User.Identity?.Name
            ?? User.FindFirstValue("preferred_username");
        var roles = User.FindAll(ClaimTypes.Role).Select(c => c.Value).ToList();

        return Ok(new
        {
            message = "Estas autenticado.",
            user = username,
            seniority = User.FindFirstValue("seniority"),
            department = User.FindFirstValue("department"),
            roles
        });
    }

    [Authorize(Policy = "SeniorDeveloper")]
    [HttpGet("internal/architecture")]
    public IActionResult GetArchitectureDocs()
    {
        var username = User.Identity?.Name
            ?? User.FindFirstValue("preferred_username");

        return Ok(new
        {
            message = "Documentacion interna de arquitectura.",
            user = username,
            policy = "SeniorDeveloper",
            documents = new[]
            {
                "ADR-001: Autenticacion con Keycloak",
                "ADR-002: Politicas de autorizacion en ASP.NET Core"
            }
        });
    }
}
