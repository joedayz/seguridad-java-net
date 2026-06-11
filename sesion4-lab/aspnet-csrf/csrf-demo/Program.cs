using CsrfRazorDemo.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddRazorPages();
builder.Services.AddHttpContextAccessor();
builder.Services.AddScoped<WalletService>();

// Program.cs — AntiForgery habilitado por defecto en Razor Pages.
builder.Services.AddAntiforgery(options =>
{
    options.HeaderName = "X-CSRF-TOKEN"; // Para peticiones AJAX
    options.Cookie.SameSite = SameSiteMode.Strict;
    // En produccion con HTTPS: CookieSecurePolicy.Always
    options.Cookie.SecurePolicy = CookieSecurePolicy.SameAsRequest;
});

builder.Services.AddSession(options =>
{
    options.Cookie.SameSite = SameSiteMode.Lax;
    options.Cookie.HttpOnly = true;
});

var app = builder.Build();

app.UseStaticFiles();
app.UseRouting();
app.UseSession();
app.UseAntiforgery();
app.MapRazorPages();

app.Run();

// En un controlador MVC:
// [HttpPost]
// [ValidateAntiForgeryToken]
// public IActionResult Transfer(TransferViewModel model) { ... }
//
// Para Minimal APIs / JWT — desactivar AntiForgery es seguro si TODAS las peticiones
// de mutacion requieren cabecera Authorization (el atacante no puede forzarla).
