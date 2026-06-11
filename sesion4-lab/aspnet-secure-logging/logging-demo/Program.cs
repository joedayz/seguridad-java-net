using LoggingDemo.Handlers;
using LoggingDemo.Services;
using LoggingDemo.Support;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddScoped<PaymentService>();
builder.Services.AddScoped<OrderService>();
builder.Services.AddScoped<LogCapture>();
builder.Services.AddExceptionHandler<GlobalExceptionHandler>();
builder.Services.AddProblemDetails();

var app = builder.Build();

app.UseExceptionHandler();
app.MapControllers();

app.Run();
