var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();

var productionOrigin = builder.Configuration["Cors:ProductionOrigin"]
    ?? "https://app.midominio.com";
var developmentOrigin = builder.Configuration["Cors:DevelopmentOrigin"]
    ?? "http://localhost:8194";

builder.Services.AddCors(options =>
{
    options.AddPolicy("PoliticaProduccion", policy =>
    {
        policy.WithOrigins(productionOrigin)
            .WithMethods("GET", "POST", "PUT")
            .WithHeaders("Authorization", "Content-Type")
            .AllowCredentials()
            .SetPreflightMaxAge(TimeSpan.FromSeconds(3600));
    });

    options.AddPolicy("PoliticaDesarrollo", policy =>
    {
        policy.WithOrigins(developmentOrigin)
            .AllowAnyMethod()
            .AllowAnyHeader();
    });
});

var app = builder.Build();

var env = app.Environment;
app.UseCors(env.IsProduction() ? "PoliticaProduccion" : "PoliticaDesarrollo");

app.MapControllers();

app.Run();
