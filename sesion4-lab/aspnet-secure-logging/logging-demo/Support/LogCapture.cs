namespace LoggingDemo.Support;

/// <summary>Captura lineas de log para mostrarlas en la respuesta HTTP de la demo.</summary>
public class LogCapture
{
    private readonly List<string> _lines = [];

    public void Add(string line) => _lines.Add(line);

    public void Clear() => _lines.Clear();

    public IReadOnlyList<string> Snapshot() => _lines.ToList();
}
