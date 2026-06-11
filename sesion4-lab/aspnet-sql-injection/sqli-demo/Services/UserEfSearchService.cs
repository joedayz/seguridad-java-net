using Microsoft.EntityFrameworkCore;
using SqlInjectionDemo.Data;

namespace SqlInjectionDemo.Services;

/// <summary>
/// Busqueda de usuarios con Entity Framework Core. Contiene TRES implementaciones
/// para comparar lado a lado:
///
///  - <see cref="SearchVulnerable"/>: ANTES. <c>FromSqlRaw</c> con interpolacion de
///    string (la entrada se concatena en la SQL). Es explotable.
///  - <see cref="SearchSeguroInterpolado"/>: DESPUES (opcion 1). <c>FromSqlInterpolated</c>
///    parametriza automaticamente los valores interpolados.
///  - <see cref="SearchSeguroParametros"/>: DESPUES (opcion 2). <c>FromSqlRaw</c> con
///    marcadores <c>{0}</c> y argumentos separados (parametros vinculados).
///  - <see cref="SearchSeguroLinq"/>: DESPUES (opcion 3, recomendada). LINQ
///    (<c>Where</c>); EF Core genera SQL parametrizado automaticamente.
/// </summary>
public class UserEfSearchService
{
    private readonly AppDbContext _context;

    public UserEfSearchService(AppDbContext context)
    {
        _context = context;
    }

    // ==========================================================================
    // ANTES — VULNERABLE
    // ==========================================================================

    /// <summary>
    /// PELIGRO: interpola la entrada del usuario directamente en la SQL antes de
    /// pasarla a <c>FromSqlRaw</c>. Un atacante puede alterar la logica de la query
    /// (p. ej. <c>' OR '1'='1</c>).
    /// </summary>
    public (string SqlEjecutado, List<User> Users) SearchVulnerable(string username)
    {
        var sql = $"SELECT * FROM users WHERE email = '{username}'";

        var users = _context.Users
            .FromSqlRaw(sql)
            .ToList();

        return (sql, users);
    }

    // ==========================================================================
    // DESPUES — SEGURO (opcion 1: FromSqlInterpolated)
    // ==========================================================================

    /// <summary>
    /// SEGURO: <c>FromSqlInterpolated</c> convierte la interpolacion en parametros
    /// vinculados; el motor trata la entrada como dato, no como SQL.
    /// </summary>
    public (string SqlEjecutado, List<User> Users) SearchSeguroInterpolado(string username)
    {
        const string plantilla = "SELECT * FROM users WHERE email = {0}";

        var users = _context.Users
            .FromSqlInterpolated($"SELECT * FROM users WHERE email = {username}")
            .ToList();

        return ($"{plantilla}   [parametro vinculado: {username}]", users);
    }

    // ==========================================================================
    // DESPUES — SEGURO (opcion 2: FromSqlRaw con parametros)
    // ==========================================================================

    /// <summary>
    /// SEGURO: <c>FromSqlRaw</c> con marcadores <c>{0}</c> y el valor en argumentos
    /// separados. EF Core lo traduce a una consulta parametrizada.
    /// </summary>
    public (string SqlEjecutado, List<User> Users) SearchSeguroParametros(string username)
    {
        const string sql = "SELECT * FROM users WHERE email = {0}";

        var users = _context.Users
            .FromSqlRaw(sql, username)
            .ToList();

        return ($"{sql}   [parametro vinculado: {username}]", users);
    }

    // ==========================================================================
    // DESPUES — SEGURO (opcion 3: LINQ — recomendada)
    // ==========================================================================

    /// <summary>
    /// SEGURO (recomendado): LINQ traduce la expresion a SQL parametrizado. No hace
    /// falta SQL nativo salvo que sea estrictamente necesario.
    /// </summary>
    public (string ConsultaEjecutada, List<User> Users) SearchSeguroLinq(string username)
    {
        var users = _context.Users
            .Where(u => u.Username == username)
            .ToList();

        return (
            "LINQ: Users.Where(u => u.Username == @username)   [EF Core genera SQL parametrizado]",
            users);
    }
}
