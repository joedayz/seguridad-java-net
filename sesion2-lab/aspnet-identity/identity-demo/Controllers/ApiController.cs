using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace IdentityDemo.Controllers;

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
            scope = "public"
        });
    }

    [HttpGet("me")]
    [Authorize]
    public IActionResult Me()
    {
        var username = User.FindFirstValue("preferred_username")
            ?? User.FindFirstValue(ClaimTypes.Name)
            ?? User.Identity?.Name;
        var email = User.FindFirstValue(ClaimTypes.Email);
        var authorities = User.FindAll(ClaimTypes.Role).Select(c => "ROLE_" + c.Value).ToList();

        return Ok(new
        {
            message = "Estas autenticado.",
            user = username,
            email,
            authorities
        });
    }

    [HttpGet("admin/hello")]
    [Authorize(Roles = "Admin")]
    public IActionResult AdminHello()
    {
        var username = User.FindFirstValue("preferred_username")
            ?? User.FindFirstValue(ClaimTypes.Name)
            ?? User.Identity?.Name;

        return Ok(new
        {
            message = "Bienvenido al area de administracion.",
            user = username,
            scope = "admin"
        });
    }
}
