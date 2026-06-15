using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using System.Text.Json;
using Microsoft.IdentityModel.Tokens;

namespace JwtDemo.Services;

public class VulnerableJwtService(IConfiguration config)
{
    private readonly string _secret = config["Jwt:Vulnerable:Secret"] ?? "MiSecreto12";

    public string IssueToken(string subject, string role)
    {
        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_secret));
        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);
        var token = new JwtSecurityToken(
            claims: [new Claim("sub", subject), new Claim("role", role)],
            expires: DateTime.UtcNow.AddHours(1),
            signingCredentials: creds);
        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    public object Validate(string token)
    {
        var parts = token.Split('.');
        if (parts.Length < 2)
        {
            throw new ArgumentException("Token malformado");
        }

        var headerJson = Encoding.UTF8.GetString(Base64UrlDecode(parts[0]));
        using var header = JsonDocument.Parse(headerJson);
        var alg = header.RootElement.GetProperty("alg").GetString() ?? "";

        JwtSecurityToken jwt;
        if (alg.Equals("none", StringComparison.OrdinalIgnoreCase))
        {
            var payloadJson = Encoding.UTF8.GetString(Base64UrlDecode(parts[1]));
            using var payload = JsonDocument.Parse(payloadJson);
            var claims = payload.RootElement.EnumerateObject()
                .Select(p => new Claim(p.Name, p.Value.ToString()))
                .ToList();
            jwt = new JwtSecurityToken(claims: claims);
        }
        else
        {
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_secret));
            var handler = new JwtSecurityTokenHandler();
            handler.ValidateToken(token, new TokenValidationParameters
            {
                ValidateIssuer = false,
                ValidateAudience = false,
                IssuerSigningKey = key,
                ValidateIssuerSigningKey = true
            }, out var validated);
            jwt = (JwtSecurityToken)validated;
        }

        return new
        {
            valid = true,
            algorithm = alg,
            subject = jwt.Claims.FirstOrDefault(c => c.Type is "sub" or ClaimTypes.NameIdentifier)?.Value,
            role = jwt.Claims.FirstOrDefault(c => c.Type == "role")?.Value,
            secretLength = _secret.Length,
            issues = new[] {
                "Acepta algoritmo none",
                $"Secreto HMAC debil ({_secret.Length} chars)",
                "No valida iss ni aud"
            }
        };
    }

    private static byte[] Base64UrlDecode(string input)
    {
        var padded = input.Replace('-', '+').Replace('_', '/');
        switch (padded.Length % 4)
        {
            case 2: padded += "=="; break;
            case 3: padded += "="; break;
        }
        return Convert.FromBase64String(padded);
    }
}
