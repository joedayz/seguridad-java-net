package com.example.integrador.service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.integrador.config.ReportsProperties;
import com.example.integrador.xml.ReportXmlParser;

@Service
public class ReportGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerationService.class);
    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final InMemoryRateLimiter rateLimiter;
    private final ReportsProperties properties;

    public ReportGenerationService(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedJdbc,
            InMemoryRateLimiter rateLimiter,
            ReportsProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbc = namedJdbc;
        this.rateLimiter = rateLimiter;
        this.properties = properties;
    }

    public Map<String, Object> generateVulnerable(String xml) throws Exception {
        log.info("VULNERABLE report request xml={}", xml);

        ReportXmlParser.ReportRequest request = ReportXmlParser.parseVulnerable(xml);

        String sql = "SELECT id, name, category, price FROM products WHERE category = '"
                + request.category() + "'";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        String pdfName = request.fileName() + ".pdf";
        String pdfContent = buildFakePdf(rows);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("modo", "VULNERABLE (XXE + SQLi + sin API key + sin rate limit + nombre PDF sin validar)");
        body.put("sqlEjecutado", sql);
        body.put("nombrePdf", pdfName);
        body.put("filas", rows.size());
        body.put("pdf", pdfContent);
        body.put("vectoresAbiertos", List.of(
                "XXE en parser XML",
                "SQLi en filtro category",
                "Sin autenticacion API key",
                "Sin rate limiting",
                "Path traversal posible en fileName"));
        return body;
    }

    public Map<String, Object> generateSeguro(String xml, String apiKey, String clientId) throws Exception {
        if (!properties.apiKey().equals(apiKey)) {
            throw new UnauthorizedException("API key invalida");
        }

        var limit = properties.rateLimit();
        boolean allowed = rateLimiter.tryConsume(
                clientId,
                limit.capacity(),
                Duration.ofSeconds(limit.windowSeconds()));
        if (!allowed) {
            throw new RateLimitExceededException("Limite de peticiones excedido");
        }

        ReportXmlParser.ReportRequest request = ReportXmlParser.parseSeguro(xml);

        if (!SAFE_FILENAME.matcher(request.fileName()).matches()) {
            throw new IllegalArgumentException("fileName solo permite [a-zA-Z0-9_-] hasta 64 caracteres");
        }

        String sql = "SELECT id, name, category, price FROM products WHERE category = :category";
        var params = new MapSqlParameterSource("category", request.category());
        List<Map<String, Object>> rows = namedJdbc.queryForList(sql, params);

        String pdfName = request.fileName() + ".pdf";
        String pdfContent = buildFakePdf(rows);

        log.info(
                "AUDIT report_generated clientId={} category={} rows={} pdf={}",
                clientId,
                request.category(),
                rows.size(),
                pdfName);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("modo", "SEGURO (XML endurecido + SQL parametrizado + API key + rate limit + audit log)");
        body.put("nombrePdf", pdfName);
        body.put("filas", rows.size());
        body.put("pdf", pdfContent);
        body.put("mitigaciones", List.of(
                "DTD/entidades externas bloqueadas",
                "Consulta parametrizada",
                "API key validada",
                "Rate limit por cliente",
                "Nombre de fichero en allowlist"));
        return body;
    }

    private static String buildFakePdf(List<Map<String, Object>> rows) {
        StringBuilder sb = new StringBuilder("%PDF-1.4 demo\n-- productos --\n");
        for (Map<String, Object> row : rows) {
            sb.append(row.get("name")).append(" | ").append(row.get("price")).append('\n');
        }
        return sb.toString();
    }

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}
