using System.Security.Claims;
using System.Text.Json;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();

// El "iss" del token: siempre el host por el que los clientes piden el token (localhost).
var issuer = builder.Configuration["Keycloak:Issuer"]
    ?? "http://localhost:8080/realms/demo";

// Donde la API descarga la metadata/JWKS para validar la firma. En local apunta a localhost;
// en docker-compose se sobreescribe a http://keycloak:8080 (red interna del compose).
var metadataAddress = builder.Configuration["Keycloak:MetadataAddress"]
    ?? $"{issuer}/.well-known/openid-configuration";

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.MetadataAddress = metadataAddress;
        // Keycloak en la demo va por HTTP (sin TLS), asi que no exigimos metadata por HTTPS.
        options.RequireHttpsMetadata = false;
        // Conservamos los nombres de claim originales de Keycloak (preferred_username, email, realm_access)
        // en vez de remapearlos a las URIs largas de .NET.
        options.MapInboundClaims = false;
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidIssuer = issuer,
            // El client de Keycloak emite audience "account"; en la demo no validamos audience.
            ValidateAudience = false,
            ValidateLifetime = true,
            NameClaimType = "preferred_username",
            RoleClaimType = ClaimTypes.Role
        };
        options.Events = new JwtBearerEvents
        {
            // Keycloak guarda los roles de realm en el claim "realm_access.roles".
            // Los convertimos en claims de rol para que funcione [Authorize(Roles = "ADMIN")].
            OnTokenValidated = context =>
            {
                MapRealmRoles(context);
                return Task.CompletedTask;
            }
        };
    });

builder.Services.AddAuthorization();

var app = builder.Build();

app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();

static void MapRealmRoles(TokenValidatedContext context)
{
    if (context.Principal?.Identity is not ClaimsIdentity identity)
    {
        return;
    }

    var realmAccess = context.Principal.FindFirst("realm_access")?.Value;
    if (string.IsNullOrEmpty(realmAccess))
    {
        return;
    }

    using var document = JsonDocument.Parse(realmAccess);
    if (!document.RootElement.TryGetProperty("roles", out var roles)
        || roles.ValueKind != JsonValueKind.Array)
    {
        return;
    }

    foreach (var role in roles.EnumerateArray())
    {
        var value = role.GetString();
        if (!string.IsNullOrEmpty(value))
        {
            identity.AddClaim(new Claim(ClaimTypes.Role, value));
        }
    }
}
