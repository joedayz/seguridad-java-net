using System.Text.Encodings.Web;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace XssRazorDemo.Pages;

/// <summary>
/// DESPUES — SEGURO. La vista usa @@Model.UserComment (escapado automatico). El code-behind
/// muestra HtmlEncoder.Default.Encode() para codificacion explicita cuando haga falta.
/// </summary>
public class SecureCommentModel : PageModel
{
    public string UserComment { get; set; } = "";

    public string EncodedComment { get; private set; } = "";

    public void OnGet()
    {
        UserComment = Request.Query["comment"].ToString();
        EncodedComment = HtmlEncoder.Default.Encode(UserComment);
    }

    public void OnPost([FromForm] string comment)
    {
        UserComment = comment;
        // Ejemplo de codificacion explicita en code-behind cuando necesitas una cadena
        // segura para mostrar o almacenar en forma codificada HTML.
        EncodedComment = HtmlEncoder.Default.Encode(UserComment);
    }
}
