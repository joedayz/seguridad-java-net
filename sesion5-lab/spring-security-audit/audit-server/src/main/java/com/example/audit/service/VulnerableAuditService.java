package com.example.audit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VulnerableAuditService {

    private static final Logger log = LoggerFactory.getLogger(VulnerableAuditService.class);

    public boolean login(String username, String password) {
        boolean ok = "ana".equals(username) && "Secr3t!".equals(password);
        // ANTES: texto libre, sin correlation ID, incluye password
        log.info("User {} tried login with password {} result={}", username, password, ok ? "OK" : "FAIL");
        return ok;
    }

    public void logAccess(String username, String path, int status) {
        log.info("Access user={} path={} status={}", username, path, status);
    }
}
