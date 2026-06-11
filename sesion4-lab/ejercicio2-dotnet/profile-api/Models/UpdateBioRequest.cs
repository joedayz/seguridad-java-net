using System.ComponentModel.DataAnnotations;

namespace ProfileApi.Models;

public class UpdateBioRequest
{
    [MaxLength(500, ErrorMessage = "Bio no puede superar 500 caracteres")]
    public string Bio { get; set; } = "";
}
