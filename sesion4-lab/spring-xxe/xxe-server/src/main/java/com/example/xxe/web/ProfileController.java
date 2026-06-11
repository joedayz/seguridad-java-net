package com.example.xxe.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.xxe.xml.XmlProfileParser;
import com.example.xxe.xml.XmlProfileParser.ParseResult;

/**
 * Recibe perfiles de usuario en XML:
 *
 *  POST /api/profile/vulnerable   -> sin restricciones (XXE explotable)
 *  POST /api/profile/seguro       -> DOM con XxeMitigationExample
 *  POST /api/profile/seguro-sax   -> SAX con XxeMitigationExample
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @FunctionalInterface
    private interface XmlParser {
        ParseResult parse(String xml) throws Exception;
    }

    @PostMapping(value = "/vulnerable", consumes = MediaType.APPLICATION_XML_VALUE)
    public Map<String, Object> vulnerable(@RequestBody String xml) {
        return aRespuesta(
                "VULNERABLE (DocumentBuilderFactory sin restricciones)",
                xml,
                XmlProfileParser::parseVulnerable);
    }

    @PostMapping(value = "/seguro", consumes = MediaType.APPLICATION_XML_VALUE)
    public Map<String, Object> seguro(@RequestBody String xml) {
        return aRespuesta(
                "SEGURO (DocumentBuilderFactory endurecido — XxeMitigationExample)",
                xml,
                XmlProfileParser::parseSeguro);
    }

    @PostMapping(value = "/seguro-sax", consumes = MediaType.APPLICATION_XML_VALUE)
    public Map<String, Object> seguroSax(@RequestBody String xml) {
        return aRespuesta(
                "SEGURO (SAXParserFactory endurecido — XxeMitigationExample)",
                xml,
                XmlProfileParser::parseSeguroSax);
    }

    private Map<String, Object> aRespuesta(String modo, String xml, XmlParser parser) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("modo", modo);
        respuesta.put("xmlRecibido", xml);

        try {
            ParseResult resultado = parser.parse(xml);
            respuesta.put("usernameExtraido", resultado.username());
            respuesta.put("exito", true);
        } catch (Exception e) {
            respuesta.put("exito", false);
            respuesta.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        return respuesta;
    }
}
