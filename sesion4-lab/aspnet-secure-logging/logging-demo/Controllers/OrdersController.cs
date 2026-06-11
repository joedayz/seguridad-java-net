using LoggingDemo.Services;
using Microsoft.AspNetCore.Mvc;

namespace LoggingDemo.Controllers;

/// <summary>
///  GET /api/orders/vulnerable/{id} — devuelve mensaje y stack trace al cliente
///  GET /api/orders/seguro/{id}       — <see cref="Handlers.GlobalExceptionHandler"/> responde sin detalles internos
/// </summary>
[ApiController]
[Route("api/orders")]
public class OrdersController : ControllerBase
{
    private readonly OrderService _orderService;

    public OrdersController(OrderService orderService)
    {
        _orderService = orderService;
    }

    [HttpGet("vulnerable/{id}")]
    public IActionResult GetVulnerable(string id)
    {
        try
        {
            var order = _orderService.FindById(id);
            return Ok(new { pedido = order });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new
            {
                modo = "VULNERABLE — excepcion y stack trace expuestos al cliente",
                error = ex.Message,
                stackTrace = ex.ToString()
            });
        }
    }

    [HttpGet("seguro/{id}")]
    public IActionResult GetSeguro(string id)
    {
        var order = _orderService.FindById(id);
        return Ok(new { pedido = order });
    }
}
