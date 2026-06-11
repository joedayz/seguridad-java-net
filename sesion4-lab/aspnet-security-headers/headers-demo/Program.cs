using HeadersDemo.Middleware;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();

// CORS restringido (diapositiva)
builder.Services.AddCors(options =>
{
    options.AddPolicy("SecureCors", policy => policy
        .WithOrigins("https://myapp.com", "https://admin.myapp.com")
        .WithMethods("GET", "POST", "PUT", "DELETE")
        .AllowCredentials());
});

var app = builder.Build();

app.UseStaticFiles();

// ==========================================================================
// DESPUES — middleware de seguridad (diapositiva) — no aplica a /api/insecure/*
// ==========================================================================
app.UseWhen(
    ctx => !ctx.Request.Path.StartsWithSegments("/api/insecure"),
    secure =>
    {
        secure.UseHsts();
        // En produccion con HTTPS: secure.UseHttpsRedirection();

        secure.UseSecurityHeaders();
        secure.UseCors("SecureCors");
    });

// ==========================================================================
// ANTES — sin middleware de seguridad
// ==========================================================================
app.MapGet("/api/insecure/check", () => Results.Json(new
{
    modo = "SIN headers de seguridad",
    headersEsperadosEnRespuesta = new Dictionary<string, string>
    {
        ["X-Content-Type-Options"] = "(ausente)",
        ["X-Frame-Options"] = "(ausente)",
        ["Content-Security-Policy"] = "(ausente)",
        ["Referrer-Policy"] = "(ausente)"
    },
    comoVerificar = "curl -i http://localhost:8192/api/insecure/check"
}));

app.MapControllers();

app.Run();
