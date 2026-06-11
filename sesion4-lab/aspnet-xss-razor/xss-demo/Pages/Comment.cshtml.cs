using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace XssRazorDemo.Pages;

/// <summary>
/// ANTES — VULNERABLE. Toma la entrada del usuario y la muestra con Html.Raw en la vista.
/// </summary>
public class CommentModel : PageModel
{
    public string UserComment { get; set; } = "";

    public void OnGet()
    {
        // Reflejado por query string (como en la diapositiva).
        UserComment = Request.Query["comment"].ToString();
    }

    public void OnPost([FromForm] string comment)
    {
        // PELIGRO: la entrada del usuario va "tal cual" al modelo.
        UserComment = comment;
    }
}
