namespace AuditDemo.Services;

using AuditDemo.Support;

public class VulnerableAuditService(LogCapture logCapture)
{
    public bool Login(string username, string password)
    {
        var ok = username == "ana" && password == "Secr3t!";
        var line = $"User {username} tried login with password {password} result={(ok ? "OK" : "FAIL")}";
        logCapture.Add(line);
        return ok;
    }

    public void LogAccess(string username, string path, int status)
    {
        logCapture.Add($"Access user={username} path={path} status={status}");
    }
}
