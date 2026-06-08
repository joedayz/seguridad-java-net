package com.example.demo.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.security.CspNonceFilter;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home(HttpServletRequest request) {
        Object nonceAttr = request.getAttribute(CspNonceFilter.NONCE_ATTRIBUTE);
        String nonce = nonceAttr != null ? nonceAttr.toString() : "";

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <title>API Protection Demo</title>
                </head>
                <body>
                  <h1>Ejercicio 1 — Proteccion completa de API</h1>
                  <p>HTTPS + HSTS + CSP con nonce + rate limiting Redis + CORS restrictivo.</p>
                  <button id="btn">Probar /api/public/health</button>
                  <pre id="out"></pre>
                  <script nonce="%s">
                    document.getElementById('btn').addEventListener('click', async () => {
                      const out = document.getElementById('out');
                      out.textContent = 'Llamando...';
                      try {
                        const r = await fetch('/api/public/health');
                        out.textContent = JSON.stringify(await r.json(), null, 2);
                      } catch (e) {
                        out.textContent = e.message;
                      }
                    });
                  </script>
                </body>
                </html>
                """.formatted(nonce);
    }
}
