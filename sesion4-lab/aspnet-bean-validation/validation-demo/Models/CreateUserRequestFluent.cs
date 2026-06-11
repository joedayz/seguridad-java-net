namespace ValidationDemo.Models;

/// <summary>
/// Modelo sin atributos: las reglas viven en <see cref="Validators.CreateUserValidator"/>.
/// </summary>
public class CreateUserRequestFluent
{
    public string Username { get; set; } = "";
    public string Email { get; set; } = "";
    public int Age { get; set; }
}
