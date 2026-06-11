using System.Xml;

namespace XxeDemo.Services;

public static class XmlProfileParser
{
    public record ParseResult(string Username, string XmlRecibido);

    // ==========================================================================
    // ANTES — VULNERABLE
    // ==========================================================================

    /// <summary>
    /// PELIGRO: permite DTD y resuelve entidades externas (file://, http://).
    /// </summary>
    public static ParseResult ParseVulnerable(string xml)
    {
        var settings = new XmlReaderSettings
        {
            DtdProcessing = DtdProcessing.Parse,
            XmlResolver = new XmlUrlResolver()
        };

        var document = new XmlDocument { XmlResolver = new XmlUrlResolver() };
        using var reader = XmlReader.Create(new StringReader(xml), settings);
        document.Load(reader);

        var username = document.GetElementsByTagName("username")[0]!.InnerText;
        return new ParseResult(username, xml);
    }

    // ==========================================================================
    // DESPUES — SEGURO (XmlDocument + XmlReaderSettings)
    // ==========================================================================

    /// <summary>
    /// SEGURO: carga con <see cref="XxeMitigationExample.CreateSecureXmlReaderSettings"/>.
    /// </summary>
    public static ParseResult ParseSeguro(string xml)
    {
        var document = new XmlDocument { XmlResolver = null };
        using var reader = XxeMitigationExample.CreateSecureReader(new StringReader(xml));
        document.Load(reader);

        var username = document.GetElementsByTagName("username")[0]!.InnerText;
        return new ParseResult(username, xml);
    }

    // ==========================================================================
    // DESPUES — SEGURO (solo XmlReader — patron de la diapositiva)
    // ==========================================================================

    /// <summary>
    /// SEGURO: procesa el XML solo con <see cref="XmlReader"/> endurecido, sin
    /// <see cref="XmlDocument"/> (recomendado para parsing streaming).
    /// </summary>
    public static ParseResult ParseSeguroReader(string xml)
    {
        string? username = null;

        using var reader = XxeMitigationExample.CreateSecureReader(new StringReader(xml));
        while (reader.Read())
        {
            if (reader.NodeType == XmlNodeType.Element && reader.Name == "username")
            {
                username = reader.ReadElementContentAsString();
                break;
            }
        }

        return new ParseResult(username ?? string.Empty, xml);
    }
}
