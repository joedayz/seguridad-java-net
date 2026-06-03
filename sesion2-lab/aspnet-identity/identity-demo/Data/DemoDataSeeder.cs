using System.Net.Sockets;
using IdentityDemo.Models;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Npgsql;

namespace IdentityDemo.Data;

public static class DemoDataSeeder
{
    public const string DemoPassword = "Password123!";

    private const int MaxConnectAttempts = 30;
    private static readonly TimeSpan ConnectRetryDelay = TimeSpan.FromSeconds(2);

    public static async Task SeedAsync(IServiceProvider services)
    {
        using var scope = services.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();
        var logger = scope.ServiceProvider
            .GetRequiredService<ILoggerFactory>()
            .CreateLogger(nameof(DemoDataSeeder));

        await EnsureDatabaseReadyAsync(db, logger);

        var roleManager = scope.ServiceProvider.GetRequiredService<RoleManager<IdentityRole>>();
        var userManager = scope.ServiceProvider.GetRequiredService<UserManager<ApplicationUser>>();

        foreach (var role in new[] { "Admin", "User" })
        {
            if (!await roleManager.RoleExistsAsync(role))
            {
                await roleManager.CreateAsync(new IdentityRole(role));
            }
        }

        await EnsureUserAsync(userManager, "alice", "alice@example.com", DemoPassword, "Admin", "User");
        await EnsureUserAsync(userManager, "bob", "bob@example.com", DemoPassword, "User");
    }

    private static async Task EnsureUserAsync(
        UserManager<ApplicationUser> userManager,
        string username,
        string email,
        string password,
        params string[] roles)
    {
        var user = await userManager.FindByNameAsync(username);
        if (user is not null)
        {
            return;
        }

        user = new ApplicationUser
        {
            UserName = username,
            Email = email,
            EmailConfirmed = true
        };

        var result = await userManager.CreateAsync(user, password);
        if (!result.Succeeded)
        {
            var errors = string.Join(", ", result.Errors.Select(e => e.Description));
            throw new InvalidOperationException($"No se pudo crear el usuario {username}: {errors}");
        }

        await userManager.AddToRolesAsync(user, roles);
    }

    /// <summary>
    /// PostgreSQL (sobre todo en Podman) puede rechazar conexiones justo despues de que
    /// pg_isready marque el contenedor como healthy; reintentamos antes de fallar.
    /// </summary>
    private static async Task EnsureDatabaseReadyAsync(
        ApplicationDbContext db,
        ILogger logger,
        CancellationToken cancellationToken = default)
    {
        for (var attempt = 1; attempt <= MaxConnectAttempts; attempt++)
        {
            try
            {
                await db.Database.EnsureCreatedAsync(cancellationToken);
                if (attempt > 1)
                {
                    logger.LogInformation("PostgreSQL disponible tras {Attempt} intentos.", attempt);
                }
                return;
            }
            catch (Exception ex) when (attempt < MaxConnectAttempts && IsTransientDbError(ex))
            {
                logger.LogWarning(
                    "PostgreSQL no disponible (intento {Attempt}/{Max}): {Message}. Reintentando en {Delay}s...",
                    attempt,
                    MaxConnectAttempts,
                    ex.GetBaseException().Message,
                    ConnectRetryDelay.TotalSeconds);
                await Task.Delay(ConnectRetryDelay, cancellationToken);
            }
        }

        throw new InvalidOperationException(
            $"No se pudo conectar a PostgreSQL tras {MaxConnectAttempts} intentos.");
    }

    private static bool IsTransientDbError(Exception ex)
    {
        for (var current = ex; current is not null; current = current.InnerException)
        {
            if (current is NpgsqlException or SocketException or TimeoutException or IOException)
            {
                return true;
            }
        }
        return false;
    }
}
