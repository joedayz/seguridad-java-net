package com.example.headers.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class HeadersCheckController {

  @GetMapping("/api/insecure/check")
  public Map<String, Object> insecureCheck(HttpServletResponse response) {
    response.setHeader("X-Demo-Variante", "insecure");
    return respuesta(
        "SIN headers de seguridad (headers deshabilitados en Spring Security)",
        Map.of(
            "X-Content-Type-Options", "(ausente)",
            "X-Frame-Options", "(ausente)",
            "Strict-Transport-Security", "(ausente)",
            "Content-Security-Policy", "(ausente)"));
  }

  @RequestMapping("/api/secure/check")
  public Map<String, Object> secureCheck(HttpServletResponse response) {
    response.setHeader("X-Demo-Variante", "secure");
    return respuesta(
        "CON headers de seguridad (configuracion de la diapositiva)",
        Map.of(
            "X-Content-Type-Options", "nosniff (anadido por Spring Security)",
            "X-Frame-Options", "DENY",
            "Strict-Transport-Security", "max-age=31536000; includeSubDomains (solo en HTTPS)",
            "Content-Security-Policy", "default-src 'self'; script-src 'self'; object-src 'none'",
            "CSRF", "CookieCsrfTokenRepository (cookie XSRF-TOKEN)",
            "Sesion", "STATELESS (API REST / JWT)"));
  }

  private Map<String, Object> respuesta(String modo, Map<String, String> headersEsperados) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("modo", modo);
    body.put("headersEsperadosEnRespuesta", headersEsperados);
    body.put("comoVerificar", "curl -i http://localhost:8191/api/secure/check");
    return body;
  }
}
