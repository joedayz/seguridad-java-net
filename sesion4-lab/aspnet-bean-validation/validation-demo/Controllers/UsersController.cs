using Microsoft.AspNetCore.Mvc;
using ValidationDemo.Models;
using ValidationDemo.Services;

namespace ValidationDemo.Controllers;

/// <summary>
///  POST /api/users/vulnerable       -> sin validacion
///  POST /api/users/seguro-anotaciones -> Data Annotations ([ApiController] -> 400)
///  POST /api/users/seguro-fluent      -> FluentValidation (auto -> 400)
/// </summary>
[ApiController]
[Route("api/users")]
public class UsersController : ControllerBase
{
    private readonly UserService userService;

    public UsersController(UserService userService)
    {
        this.userService = userService;
    }

    [HttpPost("vulnerable")]
    public IActionResult CreateVulnerable([FromBody] CreateUserRequest req)
    {
        userService.Create(req.Username, req.Email, req.Age);
        return StatusCode(201, new
        {
            modo = "VULNERABLE (sin validacion)",
            mensaje = "Usuario aceptado sin validar entrada",
            usuario = req
        });
    }

    [HttpPost("seguro-anotaciones")]
    public IActionResult CreateWithAnnotations([FromBody] CreateUserRequestAnnotated req)
    {
        userService.Create(req.Username, req.Email, req.Age);
        return StatusCode(201, new
        {
            modo = "SEGURO (Data Annotations)",
            mensaje = "Usuario creado con datos validados",
            usuario = req
        });
    }

    [HttpPost("seguro-fluent")]
    public IActionResult CreateWithFluent([FromBody] CreateUserRequestFluent req)
    {
        userService.Create(req.Username, req.Email, req.Age);
        return StatusCode(201, new
        {
            modo = "SEGURO (FluentValidation)",
            mensaje = "Usuario creado con datos validados",
            usuario = req
        });
    }
}
