using System.ComponentModel.DataAnnotations;
using IdentityDemo.Models;
using IdentityDemo.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;

namespace IdentityDemo.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    private readonly UserManager<ApplicationUser> _userManager;
    private readonly SignInManager<ApplicationUser> _signInManager;
    private readonly JwtTokenService _jwtTokenService;
    private readonly IWebHostEnvironment _environment;

    public AuthController(
        UserManager<ApplicationUser> userManager,
        SignInManager<ApplicationUser> signInManager,
        JwtTokenService jwtTokenService,
        IWebHostEnvironment environment)
    {
        _userManager = userManager;
        _signInManager = signInManager;
        _jwtTokenService = jwtTokenService;
        _environment = environment;
    }

    [HttpPost("register")]
    [AllowAnonymous]
    public async Task<IActionResult> Register([FromBody] RegisterRequest request)
    {
        var user = new ApplicationUser
        {
            UserName = request.Username,
            Email = request.Email
        };

        var result = await _userManager.CreateAsync(user, request.Password);
        if (!result.Succeeded)
        {
            return BadRequest(new
            {
                message = "No se pudo registrar el usuario.",
                errors = result.Errors.Select(e => e.Description)
            });
        }

        await _userManager.AddToRoleAsync(user, "User");

        var token = await _userManager.GenerateEmailConfirmationTokenAsync(user);
        var response = new
        {
            message = "Usuario registrado. Debes confirmar el email antes de iniciar sesion.",
            email = user.Email
        };

        if (_environment.IsDevelopment())
        {
            return Ok(new
            {
                response.message,
                response.email,
                confirmationToken = token,
                hint = "En demo usa POST /api/auth/confirm-email con email y confirmationToken."
            });
        }

        return Ok(response);
    }

    [HttpPost("confirm-email")]
    [AllowAnonymous]
    public async Task<IActionResult> ConfirmEmail([FromBody] ConfirmEmailRequest request)
    {
        var user = await _userManager.FindByEmailAsync(request.Email);
        if (user is null)
        {
            return BadRequest(new { message = "Email no encontrado." });
        }

        var result = await _userManager.ConfirmEmailAsync(user, request.Token);
        if (!result.Succeeded)
        {
            return BadRequest(new
            {
                message = "No se pudo confirmar el email.",
                errors = result.Errors.Select(e => e.Description)
            });
        }

        return Ok(new { message = "Email confirmado. Ya puedes iniciar sesion." });
    }

    [HttpPost("login")]
    [AllowAnonymous]
    public async Task<IActionResult> Login([FromBody] LoginRequest request)
    {
        var user = await _userManager.FindByNameAsync(request.Username);
        if (user is null)
        {
            return Unauthorized(new { message = "Credenciales invalidas." });
        }

        if (!await _userManager.IsEmailConfirmedAsync(user))
        {
            return Unauthorized(new
            {
                message = "Debes confirmar tu email antes de iniciar sesion.",
                email = user.Email
            });
        }

        var result = await _signInManager.CheckPasswordSignInAsync(
            user, request.Password, lockoutOnFailure: true);

        if (result.IsLockedOut)
        {
            return StatusCode(StatusCodes.Status423Locked, new
            {
                message = "Cuenta bloqueada por demasiados intentos fallidos. Espera 15 minutos."
            });
        }

        if (!result.Succeeded)
        {
            return Unauthorized(new { message = "Credenciales invalidas." });
        }

        var accessToken = await _jwtTokenService.CreateTokenAsync(user, _userManager);
        return Ok(new
        {
            accessToken,
            tokenType = "Bearer",
            expiresIn = 3600,
            user = user.UserName
        });
    }
}

public record RegisterRequest(
    [Required] string Username,
    [Required, EmailAddress] string Email,
    [Required] string Password);

public record ConfirmEmailRequest(
    [Required, EmailAddress] string Email,
    [Required] string Token);

public record LoginRequest(
    [Required] string Username,
    [Required] string Password);
