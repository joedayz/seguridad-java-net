using Azure.Identity;

var builder = WebApplication.CreateBuilder(args);

// Ejercicio 2 — Despues: sin secretos en appsettings; se cargan desde Azure Key Vault.
var keyVaultUri = builder.Configuration["KeyVault:Uri"];
if (!string.IsNullOrWhiteSpace(keyVaultUri))
{
    builder.Configuration.AddAzureKeyVault(
        new Uri(keyVaultUri),
        new DefaultAzureCredential());
}

builder.Services.AddControllers();

var app = builder.Build();
app.MapControllers();
app.Run();
