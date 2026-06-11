using System.Globalization;

namespace CsrfRazorDemo.Services;

/// <summary>
/// Saldo simulado en sesion HTTP (como una sesion bancaria autenticada).
/// </summary>
public class WalletService
{
    private const double InitialBalance = 1000.0;
    private const string BalanceKey = "wallet.balance";
    private const string MessageKey = "wallet.message";

    private readonly IHttpContextAccessor httpContextAccessor;

    public WalletService(IHttpContextAccessor httpContextAccessor)
    {
        this.httpContextAccessor = httpContextAccessor;
    }

    private ISession Session => httpContextAccessor.HttpContext!.Session;

    public double GetBalance()
    {
        var raw = Session.GetString(BalanceKey);
        return double.TryParse(raw, NumberStyles.Any, CultureInfo.InvariantCulture, out var balance)
            ? balance
            : InitialBalance;
    }

    public string GetLastTransferMessage() => Session.GetString(MessageKey) ?? "";

    public void Transfer(string to, double amount)
    {
        var balance = GetBalance();
        if (amount <= 0)
        {
            Session.SetString(MessageKey, "Importe invalido.");
            return;
        }
        if (amount > balance)
        {
            Session.SetString(MessageKey, "Saldo insuficiente.");
            return;
        }

        balance -= amount;
        Session.SetString(BalanceKey, balance.ToString(CultureInfo.InvariantCulture));
        Session.SetString(MessageKey, $"Transferidos {amount} EUR a {to}.");
    }

    public void Reset()
    {
        Session.SetString(BalanceKey, InitialBalance.ToString(CultureInfo.InvariantCulture));
        Session.Remove(MessageKey);
    }
}
