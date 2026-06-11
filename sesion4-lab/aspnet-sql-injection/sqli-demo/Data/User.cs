using System.ComponentModel.DataAnnotations.Schema;

namespace SqlInjectionDemo.Data;

/// <summary>
/// Entidad EF Core sobre la tabla <c>users</c>. La propiedad <see cref="Username"/>
/// se mapea a la columna <c>email</c> (identificador de login en los datos de ejemplo).
/// </summary>
[Table("users")]
public class User
{
    [Column("id")]
    public long Id { get; set; }

    [Column("email")]
    public string Username { get; set; } = "";

    [Column("nombre")]
    public string Nombre { get; set; } = "";

    [Column("rol")]
    public string Rol { get; set; } = "";

    [Column("nota_secreta")]
    public string? NotaSecreta { get; set; }
}
