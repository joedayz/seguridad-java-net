using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace EntraDemo.Controllers;

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
            message = "Hola! Este endpoint es publico, no requiere token.",
            scope = "public",
            idp = "Microsoft Entra ID"
        });
    }

    [HttpGet("me")]
    [Authorize]
    public IActionResult Me()
    {
        var username = FirstNonEmpty(
            User.FindFirstValue("preferred_username"),
            User.FindFirstValue("upn"),
            User.FindFirstValue(ClaimTypes.Name),
            User.FindFirstValue("name"),
            User.FindFirstValue(ClaimTypes.NameIdentifier));

        var email = User.FindFirstValue(ClaimTypes.Email)
            ?? User.FindFirstValue("email");

        var roles = User.FindAll("roles").Select(c => c.Value).ToList();
        var authorities = roles.Select(r => "ROLE_" + r).ToList();

        return Ok(new
        {
            message = "Estas autenticado.",
            user = username,
            email,
            authorities,
            roles
        });
    }

    [HttpGet("admin/hello")]
    [Authorize(Roles = "ADMIN")]
    public IActionResult AdminHello()
    {
        var username = FirstNonEmpty(
            User.FindFirstValue("preferred_username"),
            User.FindFirstValue("upn"),
            User.FindFirstValue(ClaimTypes.Name),
            User.FindFirstValue("name"),
            User.FindFirstValue(ClaimTypes.NameIdentifier));

        return Ok(new
        {
            message = "Bienvenido al area de administracion.",
            user = username,
            scope = "admin"
        });
    }

    private static string? FirstNonEmpty(params string?[] values)
    {
        foreach (var value in values)
        {
            if (!string.IsNullOrWhiteSpace(value))
            {
                return value;
            }
        }
        return null;
    }
}
