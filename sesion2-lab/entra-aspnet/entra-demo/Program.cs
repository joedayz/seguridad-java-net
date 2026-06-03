using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();

var tenantId = builder.Configuration["AzureAd:TenantId"]
    ?? throw new InvalidOperationException("AzureAd:TenantId is required.");
var audience = builder.Configuration["AzureAd:Audience"]
    ?? builder.Configuration["AzureAd:ClientId"]
    ?? throw new InvalidOperationException("AzureAd:Audience or AzureAd:ClientId is required.");

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.Authority = $"https://login.microsoftonline.com/{tenantId}/v2.0";
        options.Audience = audience;
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
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
