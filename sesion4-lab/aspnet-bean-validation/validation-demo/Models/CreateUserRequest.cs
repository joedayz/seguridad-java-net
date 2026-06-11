namespace ValidationDemo.Models;

/// <summary>
/// Modelo sin validacion (endpoint vulnerable).
/// </summary>
public class CreateUserRequest
{
    public string Username { get; set; } = "";
    public string Email { get; set; } = "";
    public int Age { get; set; }
}
