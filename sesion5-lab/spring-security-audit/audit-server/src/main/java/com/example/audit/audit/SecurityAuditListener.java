package com.example.audit.audit;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import net.logstash.logback.argument.StructuredArguments;

@Component
public class SecurityAuditListener implements ApplicationListener<AbstractAuthenticationEvent> {

    private static final Logger audit = LoggerFactory.getLogger("SECURITY_AUDIT");

    @Override
    public void onApplicationEvent(AbstractAuthenticationEvent event) {
        String principal = event.getAuthentication().getName();
        Map<String, Object> fields = baseFields(principal);

        if (event instanceof AuthenticationSuccessEvent) {
            fields.put("event_type", "AUTH_SUCCESS");
            fields.put("level", "INFO");
            fields.put("reason", "LOGIN_OK");
            audit.info("security_event", StructuredArguments.entries(fields));
        } else if (event instanceof AbstractAuthenticationFailureEvent failure) {
            fields.put("event_type", "AUTH_FAILURE");
            fields.put("level", "WARN");
            fields.put("reason", "INVALID_CREDENTIALS");
            fields.put("detail", failure.getException().getMessage());
            audit.warn("security_event", StructuredArguments.entries(fields));
        }
    }

    public void logAccess(
            String user,
            String path,
            String method,
            int status,
            long durationMs,
            String clientIp,
            String userAgent) {

        Map<String, Object> fields = baseFields(user);
        fields.put("event_type", "ACCESS");
        fields.put("level", status >= 400 ? "WARN" : "INFO");
        fields.put("resource", path);
        fields.put("http_method", method);
        fields.put("status_code", status);
        fields.put("duration_ms", durationMs);
        fields.put("client_ip", clientIp);
        fields.put("user_agent", userAgent);
        audit.info("security_event", StructuredArguments.entries(fields));
    }

    private static Map<String, Object> baseFields(String user) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("timestamp", java.time.Instant.now().toString());
        fields.put("correlation_id", MDC.get("correlation_id"));
        fields.put("user_id", user);
        return fields;
    }
}
