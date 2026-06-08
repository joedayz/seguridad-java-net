using Microsoft.AspNetCore.Mvc;

namespace KeyVaultDemo.Controllers;

[ApiController]
[Route("api/config")]
public class ConfigController : ControllerBase
{
    private readonly IConfiguration _configuration;

    public ConfigController(IConfiguration configuration)
    {
        _configuration = configuration;
    }

    [HttpGet("status")]
    public IActionResult Status()
    {
        var keyVaultUri = _configuration["KeyVault:Uri"];
        var connectionString = _configuration.GetConnectionString("Default");
        var stripeKey = _configuration["ApiKeys:Stripe"];
        var sendGridKey = _configuration["ApiKeys:SendGrid"];

        return Ok(new
        {
            message = "Los valores sensibles se leen en runtime desde Azure Key Vault (no estan en el repo).",
            keyVault = new
            {
                configured = !string.IsNullOrWhiteSpace(keyVaultUri),
                uri = keyVaultUri
            },
            secretsLoaded = new
            {
                connectionString = Mask(connectionString),
                stripe = Mask(stripeKey),
                sendGrid = Mask(sendGridKey)
            },
            allSecretsPresent = !string.IsNullOrWhiteSpace(connectionString)
                                && !string.IsNullOrWhiteSpace(stripeKey)
                                && !string.IsNullOrWhiteSpace(sendGridKey)
        });
    }

    private static string? Mask(string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        if (value.Length <= 4)
        {
            return "****";
        }

        return "****" + value[^4..];
    }
}
