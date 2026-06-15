using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;

namespace JwtDemo.Services;

public class SecureJwtService(IConfiguration config)
{
    private readonly string _secret = config["Jwt:Secure:Secret"]!;
    private readonly string _issuer = config["Jwt:Secure:Issuer"]!;
    private readonly string _audience = config["Jwt:Secure:Audience"]!;

    public string IssueToken(string subject, string role)
    {
        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_secret));
        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);
        var token = new JwtSecurityToken(
            issuer: _issuer,
            audience: _audience,
            claims: [new Claim("sub", subject), new Claim("role", role)],
            expires: DateTime.UtcNow.AddHours(1),
            signingCredentials: creds);
        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    public object Validate(string token)
    {
        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_secret));
        var handler = new JwtSecurityTokenHandler();
        var parameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidIssuer = _issuer,
            ValidateAudience = true,
            ValidAudience = _audience,
            ValidateLifetime = true,
            IssuerSigningKey = key,
            ValidAlgorithms = [SecurityAlgorithms.HmacSha256]
        };

        var principal = handler.ValidateToken(token, parameters, out var validated);
        var jwt = (JwtSecurityToken)validated;

        return new
        {
            valid = true,
            algorithm = "HS256",
            subject = principal.FindFirst("sub")?.Value,
            role = principal.FindFirst("role")?.Value,
            issuer = jwt.Issuer,
            audience = jwt.Audiences.FirstOrDefault()
        };
    }
}
