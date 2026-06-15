using JwtDemo.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddSingleton<VulnerableJwtService>();
builder.Services.AddSingleton<SecureJwtService>();

var app = builder.Build();
app.MapControllers();
app.Run();
