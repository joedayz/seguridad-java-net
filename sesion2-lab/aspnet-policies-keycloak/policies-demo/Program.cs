using System.Security.Claims;
using System.Text.Json;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();

var issuer = builder.Configuration["Keycloak:Issuer"]
    ?? "http://localhost:8091/realms/demo";
var metadataAddress = builder.Configuration["Keycloak:MetadataAddress"]
    ?? $"{issuer}/.well-known/openid-configuration";

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.MetadataAddress = metadataAddress;
        options.RequireHttpsMetadata = false;
        options.MapInboundClaims = false;
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidIssuer = issuer,
            ValidateAudience = false,
            ValidateLifetime = true,
            NameClaimType = "preferred_username",
            RoleClaimType = ClaimTypes.Role
        };
        options.Events = new JwtBearerEvents
        {
            OnTokenValidated = context =>
            {
                MapRealmRoles(context);
                return Task.CompletedTask;
            }
        };
    });

builder.Services.AddAuthorization(options =>
{
    options.AddPolicy("SeniorDeveloper", policy =>
        policy.RequireRole("Developer")
            .RequireClaim("seniority", "senior")
            .RequireClaim("department", "engineering"));
});

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
