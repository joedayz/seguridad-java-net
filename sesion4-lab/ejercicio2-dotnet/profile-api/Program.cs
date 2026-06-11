using Microsoft.Data.Sqlite;
using ProfileApi.Data;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();

var app = builder.Build();

var keepAlive = new SqliteConnection(Db.ConnectionString);
keepAlive.Open();
DbSeeder.Seed(keepAlive);
app.Lifetime.ApplicationStopping.Register(() => keepAlive.Dispose());

app.MapControllers();

app.Run();
