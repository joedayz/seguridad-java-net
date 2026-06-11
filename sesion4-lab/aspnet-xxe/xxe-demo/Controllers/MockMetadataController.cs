using Microsoft.AspNetCore.Mvc;

namespace XxeDemo.Controllers;

[ApiController]
public class MockMetadataController : ControllerBase
{
    [HttpGet("/internal/mock-metadata/iam/security-credentials/demo-role")]
    public IActionResult MockAwsCredentials() => Ok(new
    {
        AccessKeyId = "AKIA_DEMO_XXE_LEAK",
        SecretAccessKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
        Token = "xxe-demo-session-token",
        Expiration = "2026-12-31T23:59:59Z"
    });
}
