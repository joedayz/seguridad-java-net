namespace SqlInjectionDemo.Data;

public static class Db
{
    // BD SQLite en memoria COMPARTIDA: mientras haya una conexion abierta
    // (la "keep-alive" de Program.cs), todas las demas conexiones que usen esta
    // misma cadena ven la misma base de datos.
    public const string ConnectionString = "Data Source=file:sqlidemo?mode=memory&cache=shared";
}
