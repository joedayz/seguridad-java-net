using CsrfRazorDemo.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace CsrfRazorDemo.Pages.Vulnerable;

/// <summary>
/// ANTES — VULNERABLE. Anti-Forgery deshabilitado: un sitio externo puede forzar
/// el POST si el usuario tiene sesion activa.
/// </summary>
[IgnoreAntiforgeryToken]
public class TransferModel : PageModel
{
    private readonly WalletService wallet;

    public TransferModel(WalletService wallet)
    {
        this.wallet = wallet;
    }

    public double Balance { get; private set; }
    public string Message { get; private set; } = "";

    public void OnGet()
    {
        LoadState();
    }

    public IActionResult OnPostTransfer(string to, double amount)
    {
        wallet.Transfer(to, amount);
        LoadState();
        return Page();
    }

    public IActionResult OnPostReset()
    {
        wallet.Reset();
        return RedirectToPage();
    }

    private void LoadState()
    {
        Balance = wallet.GetBalance();
        Message = wallet.GetLastTransferMessage();
    }
}
