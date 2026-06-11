package com.example.logging.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * MAL — registra password, JWT y numero de tarjeta en texto plano.
 */
@Service
public class AuthServiceVulnerable {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceVulnerable.class);

    public Map<String, Object> login(
            String username,
            String password,
            String jwtToken,
            String creditCardNumber) {

        log.info(
                "Login attempt username={}, password={}, token={}, card={}",
                username,
                password,
                jwtToken,
                creditCardNumber);

        boolean ok = username != null && !username.isBlank();
        return Map.of("autenticado", ok, "usuario", username != null ? username : "");
    }
}
