using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();

var tenantId = builder.Configuration["AzureAd:TenantId"]
    ?? throw new InvalidOperationException("AzureAd:TenantId is required.");
var clientId = builder.Configuration["AzureAd:ClientId"];
var configuredAudience = builder.Configuration["AzureAd:Audience"];

// Entra emite el claim `aud` como el client ID (GUID) en tokens v2.0 y como el
// Application ID URI (api://<guid>) en tokens v1.0. Aceptamos ambas formas para
// que la demo funcione sin importar la versión del token.
var validAudiences = new List<string>();
if (!string.IsNullOrWhiteSpace(configuredAudience))
{
    validAudiences.Add(configuredAudience);
}
if (!string.IsNullOrWhiteSpace(clientId))
{
    validAudiences.Add(clientId);
    validAudiences.Add($"api://{clientId}");
}
if (validAudiences.Count == 0)
{
    throw new InvalidOperationException("AzureAd:Audience or AzureAd:ClientId is required.");
}

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.Authority = $"https://login.microsoftonline.com/{tenantId}/v2.0";
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidAudiences = validAudiences,
            ValidateLifetime = true,
            RoleClaimType = "roles"
        };
    });

builder.Services.AddAuthorization();

var app = builder.Build();

app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();
