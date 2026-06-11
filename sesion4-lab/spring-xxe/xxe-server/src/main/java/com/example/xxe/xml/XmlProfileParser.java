package com.example.xxe.xml;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parseo de perfiles XML en dos variantes para la demo del antes / despues.
 */
public final class XmlProfileParser {

    private XmlProfileParser() {
    }

    public record ParseResult(String username, String xmlRecibido) {
    }

    // ==========================================================================
    // ANTES — VULNERABLE
    // ==========================================================================

    /**
     * PELIGRO: DocumentBuilderFactory sin restricciones procesa entidades externas.
     */
    public static ParseResult parseVulnerable(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        var factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        String username = document.getElementsByTagName("username").item(0).getTextContent();
        return new ParseResult(username, xml);
    }

    // ==========================================================================
    // DESPUES — SEGURO (DOM)
    // ==========================================================================

    /**
     * SEGURO: {@link XxeMitigationExample#secureDocumentBuilderFactory()}.
     */
    public static ParseResult parseSeguro(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = XxeMitigationExample.secureDocumentBuilderFactory()
                .newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        String username = document.getElementsByTagName("username").item(0).getTextContent();
        return new ParseResult(username, xml);
    }

    // ==========================================================================
    // DESPUES — SEGURO (SAX)
    // ==========================================================================

    /**
     * SEGURO: {@link XxeMitigationExample#secureSAXParserFactory()} (variante SAX
     * de la diapositiva).
     */
    public static ParseResult parseSeguroSax(String xml) throws Exception {
        var handler = new UsernameSaxHandler();
        SAXParser parser = XxeMitigationExample.secureSAXParserFactory().newSAXParser();
        parser.parse(new InputSource(new StringReader(xml)), handler);
        return new ParseResult(handler.getUsername(), xml);
    }

    private static final class UsernameSaxHandler extends DefaultHandler {

        private boolean inUsername;
        private final StringBuilder username = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            inUsername = "username".equals(localName) || "username".equals(qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (inUsername) {
                username.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if ("username".equals(localName) || "username".equals(qName)) {
                inUsername = false;
            }
        }

        String getUsername() {
            return username.toString().trim();
        }
    }
}
