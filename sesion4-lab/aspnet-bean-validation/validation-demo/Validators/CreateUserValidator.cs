using FluentValidation;
using ValidationDemo.Models;

namespace ValidationDemo.Validators;

/// <summary>
/// FluentValidation — mas expresivo para reglas complejas. Separa las reglas del modelo
/// y permite reutilizarlas en tests unitarios sin el framework web.
/// </summary>
public class CreateUserValidator : AbstractValidator<CreateUserRequestFluent>
{
    public CreateUserValidator()
    {
        RuleFor(x => x.Username)
            .NotEmpty().Length(3, 50)
            .Matches(@"^[a-zA-Z0-9_-]+$")
            .WithMessage("Solo caracteres alfanumericos");

        RuleFor(x => x.Email).NotEmpty().EmailAddress();

        RuleFor(x => x.Age).InclusiveBetween(18, 120);
    }
}
