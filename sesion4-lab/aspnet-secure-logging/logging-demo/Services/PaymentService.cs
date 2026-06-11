namespace LoggingDemo.Services;

public class PaymentService
{
    /// <summary>Tarjetas que terminan en 0000 simulan rechazo del banco.</summary>
    public void Charge(string cardNumber, string cvv, string customerToken)
    {
        if (cardNumber.EndsWith("0000", StringComparison.Ordinal))
        {
            throw new InvalidOperationException("Card declined by issuer");
        }
    }
}
