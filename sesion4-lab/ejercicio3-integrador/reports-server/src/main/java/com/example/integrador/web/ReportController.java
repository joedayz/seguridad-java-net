package com.example.integrador.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.integrador.service.ReportGenerationService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportGenerationService reportService;

    public ReportController(ReportGenerationService reportService) {
        this.reportService = reportService;
    }

    @PostMapping(value = "/vulnerable/generate", consumes = MediaType.APPLICATION_XML_VALUE)
    public Map<String, Object> generateVulnerable(@RequestBody String xml) throws Exception {
        return reportService.generateVulnerable(xml);
    }

    @PostMapping(value = "/seguro/generate", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Map<String, Object>> generateSeguro(
            @RequestBody String xml,
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Client-Id", defaultValue = "anonymous") String clientId)
            throws Exception {

        try {
            return ResponseEntity.ok(reportService.generateSeguro(xml, apiKey, clientId));
        } catch (ReportGenerationService.UnauthorizedException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ex.getMessage()));
        } catch (ReportGenerationService.RateLimitExceededException ex) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
