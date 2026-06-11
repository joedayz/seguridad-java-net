using Microsoft.AspNetCore.Mvc;
using XxeDemo.Services;

namespace XxeDemo.Controllers;

/// <summary>
///  POST /api/profile/vulnerable     -> DtdProcessing.Parse (XXE explotable)
///  POST /api/profile/seguro         -> XmlDocument + XmlReaderSettings seguro
///  POST /api/profile/seguro-reader  -> solo XmlReader endurecido (diapositiva)
/// </summary>
[ApiController]
[Route("api/profile")]
public class ProfileController : ControllerBase
{
    private delegate XmlProfileParser.ParseResult XmlParser(string xml);

    [HttpPost("vulnerable")]
    [Consumes("application/xml")]
    public IActionResult Vulnerable() =>
        Parse(Request.Body, "VULNERABLE (DtdProcessing.Parse + XmlUrlResolver)", XmlProfileParser.ParseVulnerable);

    [HttpPost("seguro")]
    [Consumes("application/xml")]
    public IActionResult Seguro() =>
        Parse(Request.Body, "SEGURO (XmlDocument + XxeMitigationExample)", XmlProfileParser.ParseSeguro);

    [HttpPost("seguro-reader")]
    [Consumes("application/xml")]
    public IActionResult SeguroReader() =>
        Parse(Request.Body, "SEGURO (XmlReader endurecido — XxeMitigationExample)", XmlProfileParser.ParseSeguroReader);

    private IActionResult Parse(Stream body, string modo, XmlParser parser)
    {
        using var reader = new StreamReader(body);
        var xml = reader.ReadToEnd();

        var respuesta = new Dictionary<string, object?>
        {
            ["modo"] = modo,
            ["xmlRecibido"] = xml
        };

        try
        {
            var resultado = parser(xml);
            respuesta["usernameExtraido"] = resultado.Username;
            respuesta["exito"] = true;
        }
        catch (Exception ex)
        {
            respuesta["exito"] = false;
            respuesta["error"] = $"{ex.GetType().Name}: {ex.Message}";
        }

        return Ok(respuesta);
    }
}
