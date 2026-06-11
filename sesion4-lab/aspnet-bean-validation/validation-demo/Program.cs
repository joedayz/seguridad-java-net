using FluentValidation;
using FluentValidation.AspNetCore;
using ValidationDemo.Validators;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddScoped<ValidationDemo.Services.UserService>();

// FluentValidation — validacion automatica en endpoints con modelo asociado a un validator
builder.Services.AddFluentValidationAutoValidation();
builder.Services.AddValidatorsFromAssemblyContaining<CreateUserValidator>();

var app = builder.Build();

app.MapControllers();

app.Run();
