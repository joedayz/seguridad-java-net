using System.Security.Claims;
using Dapper;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.Sqlite;
using ProfileApi.Auth;
using ProfileApi.Data;
using ProfileApi.Models;

namespace ProfileApi.Controllers;

/// <summary>
/// Ejercicio 2 — perfiles de usuario. Vulnerabilidades en /vulnerable:
/// 1) SQL Injection (interpolacion en queries Dapper)
/// 2) BOLA / IDOR (sin verificar que el usuario autenticado puede ver/editar el perfil)
/// 3) Sin validacion de entrada en bio (sin limite de longitud)
/// </summary>
[ApiController]
[Route("api/profile")]
public class ProfileController : ControllerBase
{
    // ==========================================================================
    // MAL — VULNERABLE
    // ==========================================================================

    [HttpGet("vulnerable/{userId}")]
    public async Task<IActionResult> GetProfileVulnerable(string userId)
    {
        // Sin verificar si el usuario autenticado puede ver este perfil (BOLA/IDOR).

        await using var db = new SqliteConnection(Db.ConnectionString);
        await db.OpenAsync();

        // VULN — SQLi: userId concatenado en la query.
        var query = $"SELECT UserId, UserName, Email, Bio, NotaPrivada FROM UserProfiles WHERE UserId = '{userId}'";
        var profile = await db.QueryFirstOrDefaultAsync<UserProfile>(query);

        if (profile == null)
        {
            return NotFound();
        }

        return Ok(new
        {
            modo = "VULNERABLE (SQLi + sin autorizacion)",
            sqlEjecutado = query,
            perfil = profile
        });
    }

    [HttpPut("vulnerable/{userId}/bio")]
    public async Task<IActionResult> UpdateBioVulnerable(string userId, [FromBody] string bio)
    {
        await using var db = new SqliteConnection(Db.ConnectionString);
        await db.OpenAsync();

        // VULN — SQLi en userId y bio; sin comprobar propiedad del recurso.
        var query = $"UPDATE UserProfiles SET Bio = '{bio}' WHERE UserId = '{userId}'";
        await db.ExecuteAsync(query);

        return NoContent();
    }

    // ==========================================================================
    // BIEN — CORREGIDO
    // ==========================================================================

    [HttpGet("seguro/{userId:guid}")]
    public async Task<IActionResult> GetProfileSeguro(Guid userId)
    {
        var currentUserId = DemoAuth.GetCurrentUserId(HttpContext);
        if (currentUserId == null)
        {
            return Unauthorized(new { error = "Cabecera X-User-Id requerida (simula JWT)" });
        }

        if (currentUserId != userId && !DemoAuth.IsAdmin(HttpContext))
        {
            return Forbid();
        }

        await using var db = new SqliteConnection(Db.ConnectionString);
        await db.OpenAsync();

        var profile = await db.QueryFirstOrDefaultAsync<UserProfile>(
            "SELECT UserId, UserName, Email, Bio, NotaPrivada FROM UserProfiles WHERE UserId = @UserId",
            new { UserId = userId.ToString() });

        if (profile == null)
        {
            return NotFound();
        }

        // No exponer NotaPrivada salvo al propio usuario o admin.
        if (currentUserId != userId && DemoAuth.IsAdmin(HttpContext))
        {
            return Ok(new { modo = "SEGURO (Admin)", perfil = profile });
        }

        return Ok(new
        {
            modo = "SEGURO (parametros + autorizacion)",
            perfil = new { profile.UserId, profile.UserName, profile.Email, profile.Bio }
        });
    }

    [HttpPut("seguro/{userId:guid}/bio")]
    public async Task<IActionResult> UpdateBioSeguro(Guid userId, [FromBody] UpdateBioRequest request)
    {
        var currentUserId = DemoAuth.GetCurrentUserId(HttpContext);
        if (currentUserId == null)
        {
            return Unauthorized(new { error = "Cabecera X-User-Id requerida (simula JWT)" });
        }

        if (currentUserId != userId)
        {
            return Forbid();
        }

        await using var db = new SqliteConnection(Db.ConnectionString);
        await db.OpenAsync();

        await db.ExecuteAsync(
            "UPDATE UserProfiles SET Bio = @Bio WHERE UserId = @UserId",
            new { Bio = request.Bio, UserId = userId.ToString() });

        return NoContent();
    }
}
