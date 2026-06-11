package com.example.xxe.xml;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Mitigacion XXE — deshabilitar DTDs y entidades externas (diapositiva
 * «XXE · Mitigacion en Java»).
 */
public final class XxeMitigationExample {

    private XxeMitigationExample() {
    }

    /**
     * {@link DocumentBuilderFactory} endurecido para DOM (el que usa la demo en
     * {@code /api/profile/seguro}).
     */
    public static DocumentBuilderFactory secureDocumentBuilderFactory()
            throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        // Bloquea completamente los DOCTYPE
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        // Deshabilita entidades externas generales y de parametro
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        // Evita expansion de referencias a entidades
        dbf.setExpandEntityReferences(false);
        // Opcional: ignora DTDs externas / buenas practicas adicionales
        dbf.setXIncludeAware(false);
        dbf.setNamespaceAware(true);

        // Endurecimiento extra JAXP 1.5+
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        return dbf;
    }

    /**
     * {@link SAXParserFactory} endurecido (alternativa SAX con las mismas
     * restricciones).
     */
    public static SAXParserFactory secureSAXParserFactory()
            throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        SAXParserFactory spf = SAXParserFactory.newInstance();

        // Bloquea completamente los DOCTYPE
        spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        // Deshabilita entidades externas generales y de parametro
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        // Buenas practicas adicionales
        spf.setXIncludeAware(false);
        spf.setNamespaceAware(true);

        return spf;
    }
}
