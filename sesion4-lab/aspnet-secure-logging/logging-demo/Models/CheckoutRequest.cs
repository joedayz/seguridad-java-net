namespace LoggingDemo.Models;

public record CheckoutRequest(
    string CardNumber,
    string Cvv,
    string CustomerToken);
