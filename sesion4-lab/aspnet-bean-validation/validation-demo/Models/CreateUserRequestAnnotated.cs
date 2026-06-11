using System.ComponentModel.DataAnnotations;

namespace ValidationDemo.Models;

/// <summary>
/// Data Annotations en el modelo (diapositiva).
/// </summary>
public class CreateUserRequestAnnotated
{
    [Required]
    [StringLength(50, MinimumLength = 3)]
    [RegularExpression(@"^[a-zA-Z0-9_-]+$", ErrorMessage = "Solo caracteres alfanumericos")]
    public string Username { get; set; } = "";

    [Required, EmailAddress]
    public string Email { get; set; } = "";

    [Range(18, 120)]
    public int Age { get; set; }
}
