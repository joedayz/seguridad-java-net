using System.Xml;

namespace XxeDemo.Services;

/// <summary>
/// Mitigacion XXE — XmlReaderSettings endurecido (diapositiva
/// «XXE · Mitigacion en .NET»).
/// </summary>
public static class XxeMitigationExample
{
    /// <summary>
    /// Configuracion segura para <see cref="XmlReader.Create(Stream, XmlReaderSettings)"/>.
    /// </summary>
    public static XmlReaderSettings CreateSecureXmlReaderSettings() => new()
    {
        // Impide que el lector procese DTDs, bloqueando entidades externas.
        DtdProcessing = DtdProcessing.Prohibit,
        // Evita cualquier resolucion externa de recursos XML.
        XmlResolver = null
    };

    /// <summary>
    /// Crea un <see cref="XmlReader"/> endurecido sobre el XML de entrada.
    /// </summary>
    public static XmlReader CreateSecureReader(TextReader input) =>
        XmlReader.Create(input, CreateSecureXmlReaderSettings());
}
