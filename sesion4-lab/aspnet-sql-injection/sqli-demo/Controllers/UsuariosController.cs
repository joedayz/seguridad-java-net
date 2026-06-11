using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.Sqlite;
using SqlInjectionDemo.Data;

namespace SqlInjectionDemo.Controllers;

/// <summary>
/// Expone la MISMA busqueda por email en dos variantes para la demo del
/// "antes y despues":
///   GET /api/usuarios/vulnerable?email=...  -> concatenacion (explotable)
///   GET /api/usuarios/seguro?email=...      -> consulta parametrizada (a prueba de SQLi)
///
/// La respuesta incluye el SQL ejecutado para que, en clase, se vea como la
/// inyeccion reescribe la consulta.
/// </summary>
[ApiController]
[Route("api/usuarios")]
public class UsuariosController : ControllerBase
{
    // ==========================================================================
    // ANTES — VULNERABLE
    // ==========================================================================
    [HttpGet("vulnerable")]
    public IActionResult Vulnerable([FromQuery] string email)
    {
        // PELIGRO: la entrada del usuario se concatena directamente en la SQL.
        // Un atacante puede inyectar SQL controlando el parametro "email".
        // Ejemplo de payload malicioso:  ' OR '1'='1
        var sql = $"SELECT id, email, nombre, rol, nota_secreta FROM users WHERE email = '{email}'";

        using var connection = new SqliteConnection(Db.ConnectionString);
        connection.Open();
        using var command = connection.CreateCommand();
        command.CommandText = sql;

        var usuarios = LeerUsuarios(command);
        return Ok(new
        {
            modo = "VULNERABLE (concatenacion de String)",
            emailRecibido = email,
            sqlEjecutado = sql,
            totalFilas = usuarios.Count,
            usuarios
        });
    }

    // ==========================================================================
    // DESPUES — SEGURO
    // ==========================================================================
    [HttpGet("seguro")]
    public IActionResult Seguro([FromQuery] string email)
    {
        // SEGURO: $email es un marcador de posicion; el valor se vincula con
        // Parameters.AddWithValue y el motor lo trata como dato, no como SQL.
        // La inyeccion deja de funcionar: ' OR '1'='1 se busca como un email literal.
        const string sql = "SELECT id, email, nombre, rol, nota_secreta FROM users WHERE email = $email";

        using var connection = new SqliteConnection(Db.ConnectionString);
        connection.Open();
        using var command = connection.CreateCommand();
        command.CommandText = sql;
        command.Parameters.AddWithValue("$email", email);

        var usuarios = LeerUsuarios(command);
        return Ok(new
        {
            modo = "SEGURO (consulta parametrizada)",
            emailRecibido = email,
            sqlEjecutado = $"{sql}   [parametro vinculado: {email}]",
            totalFilas = usuarios.Count,
            usuarios
        });
    }

    private static List<object> LeerUsuarios(SqliteCommand command)
    {
        var usuarios = new List<object>();
        using var reader = command.ExecuteReader();
        while (reader.Read())
        {
            usuarios.Add(new
            {
                id = reader.GetInt64(0),
                email = reader.GetString(1),
                nombre = reader.GetString(2),
                rol = reader.GetString(3),
                notaSecreta = reader.IsDBNull(4) ? null : reader.GetString(4)
            });
        }
        return usuarios;
    }
}
