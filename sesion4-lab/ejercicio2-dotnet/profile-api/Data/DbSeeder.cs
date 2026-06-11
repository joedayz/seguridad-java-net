using Microsoft.Data.Sqlite;

namespace ProfileApi.Data;

public static class DbSeeder
{
    public static readonly Guid AnaId = Guid.Parse("11111111-1111-1111-1111-111111111111");
    public static readonly Guid LuisId = Guid.Parse("22222222-2222-2222-2222-222222222222");
    public static readonly Guid AdminId = Guid.Parse("33333333-3333-3333-3333-333333333333");

    public static void Seed(SqliteConnection connection)
    {
        using var cmd = connection.CreateCommand();
        cmd.CommandText = """
            DROP TABLE IF EXISTS UserProfiles;
            CREATE TABLE UserProfiles (
                UserId   TEXT PRIMARY KEY,
                UserName TEXT NOT NULL,
                Email    TEXT NOT NULL,
                Bio      TEXT NOT NULL,
                NotaPrivada TEXT NOT NULL
            );
            INSERT INTO UserProfiles (UserId, UserName, Email, Bio, NotaPrivada) VALUES
                ('11111111-1111-1111-1111-111111111111', 'Ana Garcia',  'ana@acme.com',  'Bio publica de Ana',  'NOTA INTERNA Ana: revision salarial pendiente'),
                ('22222222-2222-2222-2222-222222222222', 'Luis Perez',  'luis@acme.com', 'Bio publica de Luis', 'NOTA INTERNA Luis: candidato a despido'),
                ('33333333-3333-3333-3333-333333333333', 'Root Admin',  'admin@acme.com','Bio del administrador','NOTA INTERNA Admin: clave API rotacion Q3');
            """;
        cmd.ExecuteNonQuery();
    }
}
