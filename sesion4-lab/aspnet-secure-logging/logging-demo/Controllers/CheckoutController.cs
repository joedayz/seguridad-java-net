using LoggingDemo.Models;
using LoggingDemo.Services;
using LoggingDemo.Support;
using Microsoft.AspNetCore.Mvc;

namespace LoggingDemo.Controllers;

/// <summary>
///  POST /api/checkout/vulnerable — loguea PII y la devuelve en el cuerpo del error
///  POST /api/checkout/seguro       — mensaje generico al cliente; log sin datos sensibles
/// </summary>
[ApiController]
[Route("api/checkout")]
public class CheckoutController : ControllerBase
{
    private readonly PaymentService _paymentService;
    private readonly ILogger<CheckoutController> _logger;
    private readonly LogCapture _logCapture;

    public CheckoutController(
        PaymentService paymentService,
        ILogger<CheckoutController> logger,
        LogCapture logCapture)
    {
        _paymentService = paymentService;
        _logger = logger;
        _logCapture = logCapture;
    }

    [HttpPost("vulnerable")]
    public IActionResult CheckoutVulnerable([FromBody] CheckoutRequest request)
    {
        _logCapture.Clear();

        try
        {
            var logLine =
                $"Charge attempt card={request.CardNumber}, cvv={request.Cvv}, token={request.CustomerToken}";
            _logger.LogInformation("{LogLine}", logLine);
            _logCapture.Add(logLine);

            _paymentService.Charge(request.CardNumber, request.Cvv, request.CustomerToken);
            return Ok(new { modo = "VULNERABLE", mensaje = "Pago procesado" });
        }
        catch (Exception ex)
        {
            // MAL — expone excepcion completa y datos de pago al cliente.
            var errorBody =
                $"Error: {ex}\n" +
                $"Card={request.CardNumber}\n" +
                $"Cvv={request.Cvv}\n" +
                $"Token={request.CustomerToken}";

            _logCapture.Add($"ERROR response leaked PII: card={request.CardNumber}");
            return Content(errorBody, "text/plain");
        }
    }

    [HttpPost("seguro")]
    public IActionResult CheckoutSeguro([FromBody] CheckoutRequest request)
    {
        _logCapture.Clear();

        try
        {
            var cardMasked = MaskCard(request.CardNumber);
            var logLine = $"Charge attempt cardLast4={cardMasked}";
            _logger.LogInformation("{LogLine}", logLine);
            _logCapture.Add(logLine);

            _paymentService.Charge(request.CardNumber, request.Cvv, request.CustomerToken);
            return Ok(new
            {
                modo = "SEGURO",
                mensaje = "Pago procesado",
                lineasLog = _logCapture.Snapshot()
            });
        }
        catch (Exception ex)
        {
            // BIEN — log interno sin PII; respuesta generica al usuario.
            _logger.LogError(ex, "Checkout failed");
            _logCapture.Add("Checkout failed (detalles solo en sistema de monitorizacion interno)");

            return StatusCode(500, new
            {
                modo = "SEGURO",
                error = "No se pudo procesar el pago. Intente de nuevo mas tarde.",
                lineasLog = _logCapture.Snapshot()
            });
        }
    }

    private static string MaskCard(string card)
    {
        if (string.IsNullOrEmpty(card) || card.Length < 4)
        {
            return "****";
        }

        return "****" + card[^4..];
    }
}
