using AuditDemo.Middleware;
using AuditDemo.Services;
using AuditDemo.Support;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddScoped<VulnerableAuditService>();
builder.Services.AddScoped<LogCapture>();

var app = builder.Build();

app.UseMiddleware<CorrelationIdMiddleware>();
app.UseMiddleware<SecurityAuditMiddleware>();
app.MapControllers();

app.Run();
