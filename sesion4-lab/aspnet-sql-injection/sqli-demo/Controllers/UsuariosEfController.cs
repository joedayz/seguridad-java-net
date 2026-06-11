using Microsoft.AspNetCore.Mvc;
using SqlInjectionDemo.Services;

namespace SqlInjectionDemo.Controllers;

/// <summary>
/// Expone la MISMA busqueda con Entity Framework en tres variantes:
///   GET /api/usuarios-ef/vulnerable?username=...         -> FromSqlRaw + interpolacion
///   GET /api/usuarios-ef/seguro-interpolado?username=... -> FromSqlInterpolated
///   GET /api/usuarios-ef/seguro-parametros?username=...  -> FromSqlRaw + {0}
///   GET /api/usuarios-ef/seguro-linq?username=...        -> LINQ Where (recomendado)
///
/// La respuesta incluye el SQL ejecutado para que, en clase, se vea como la inyeccion
/// reescribe la consulta.
/// </summary>
[ApiController]
[Route("api/usuarios-ef")]
public class UsuariosEfController : ControllerBase
{
    private readonly UserEfSearchService _searchService;

    public UsuariosEfController(UserEfSearchService searchService)
    {
        _searchService = searchService;
    }

    [HttpGet("vulnerable")]
    public IActionResult Vulnerable([FromQuery] string username)
    {
        var (sql, users) = _searchService.SearchVulnerable(username);
        return Ok(new
        {
            modo = "VULNERABLE (FromSqlRaw con interpolacion de String)",
            usernameRecibido = username,
            sqlEjecutado = sql,
            totalFilas = users.Count,
            usuarios = users.Select(u => new
            {
                u.Id,
                email = u.Username,
                u.Nombre,
                u.Rol,
                u.NotaSecreta
            })
        });
    }

    [HttpGet("seguro-interpolado")]
    public IActionResult SeguroInterpolado([FromQuery] string username)
    {
        var (sql, users) = _searchService.SearchSeguroInterpolado(username);
        return Ok(new
        {
            modo = "SEGURO (FromSqlInterpolated)",
            usernameRecibido = username,
            sqlEjecutado = sql,
            totalFilas = users.Count,
            usuarios = users.Select(u => new
            {
                u.Id,
                email = u.Username,
                u.Nombre,
                u.Rol,
                u.NotaSecreta
            })
        });
    }

    [HttpGet("seguro-parametros")]
    public IActionResult SeguroParametros([FromQuery] string username)
    {
        var (sql, users) = _searchService.SearchSeguroParametros(username);
        return Ok(new
        {
            modo = "SEGURO (FromSqlRaw con parametros {0})",
            usernameRecibido = username,
            sqlEjecutado = sql,
            totalFilas = users.Count,
            usuarios = users.Select(u => new
            {
                u.Id,
                email = u.Username,
                u.Nombre,
                u.Rol,
                u.NotaSecreta
            })
        });
    }

    [HttpGet("seguro-linq")]
    public IActionResult SeguroLinq([FromQuery] string username)
    {
        var (consulta, users) = _searchService.SearchSeguroLinq(username);
        return Ok(new
        {
            modo = "SEGURO (LINQ Where — recomendado)",
            usernameRecibido = username,
            consultaEjecutada = consulta,
            totalFilas = users.Count,
            usuarios = users.Select(u => new
            {
                u.Id,
                email = u.Username,
                u.Nombre,
                u.Rol,
                u.NotaSecreta
            })
        });
    }
}
