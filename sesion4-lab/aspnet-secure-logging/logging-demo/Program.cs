var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddScoped<LoggingDemo.Services.PaymentService>();
builder.Services.AddScoped<LoggingDemo.Support.LogCapture>();

var app = builder.Build();

app.MapControllers();

app.Run();
