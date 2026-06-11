using System.Text.RegularExpressions;
using LoggingDemo.Models;

namespace LoggingDemo.Services;

public partial class OrderService
{
    public OrderDto FindById(string id)
    {
        if (string.IsNullOrEmpty(id) || !DigitsOnly().IsMatch(id))
        {
            throw new ArgumentException(
                $"ID de pedido invalido; tabla interna orders_legacy rechazo formato: {id}");
        }

        throw new InvalidOperationException(
            $"Pedido no encontrado en shard orders_legacy para id={id}");
    }

    [GeneratedRegex(@"^\d+$")]
    private static partial Regex DigitsOnly();
}
