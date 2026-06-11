package com.example.integrador.xml;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class ReportXmlParser {

    public record ReportRequest(String category, String fileName, String xmlRecibido) {
    }

    private ReportXmlParser() {
    }

    public static ReportRequest parseVulnerable(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        String category = text(document, "category");
        String fileName = text(document, "fileName");
        return new ReportRequest(category, fileName, xml);
    }

    public static ReportRequest parseSeguro(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = SecureXml.secureDocumentBuilderFactory().newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        String category = text(document, "category");
        String fileName = text(document, "fileName");
        return new ReportRequest(category, fileName, xml);
    }

    private static String text(Document document, String tag) {
        var nodes = document.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return "";
        }
        return nodes.item(0).getTextContent().trim();
    }
}
