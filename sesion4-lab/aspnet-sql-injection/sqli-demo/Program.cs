using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using SqlInjectionDemo.Data;
using SqlInjectionDemo.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlite(Db.ConnectionString));
builder.Services.AddScoped<UserEfSearchService>();

var app = builder.Build();

// Conexion "keep-alive": mantiene viva la BD SQLite en memoria compartida
// durante toda la vida de la app y la siembra con los usuarios de ejemplo.
var keepAlive = new SqliteConnection(Db.ConnectionString);
keepAlive.Open();
DbSeeder.Seed(keepAlive);
app.Lifetime.ApplicationStopping.Register(() => keepAlive.Dispose());

app.MapControllers();

app.Run();
