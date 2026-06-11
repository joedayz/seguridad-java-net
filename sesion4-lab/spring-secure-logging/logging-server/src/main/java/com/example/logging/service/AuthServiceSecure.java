package com.example.logging.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * BIEN — solo datos no sensibles; tarjeta enmascarada; nunca password ni JWT.
 */
@Service
public class AuthServiceSecure {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceSecure.class);

    public Map<String, Object> login(
            String username,
            String password,
            String jwtToken,
            String creditCardNumber) {

        String cardMasked = maskCard(creditCardNumber);
        log.info("Login attempt username={}, cardLast4={}", username, cardMasked);

        boolean ok = username != null
                && !username.isBlank()
                && password != null
                && password.length() >= 4;

        log.info("Login outcome username={}, success={}", username, ok);

        return Map.of("autenticado", ok, "usuario", username != null ? username : "");
    }

    private static String maskCard(String card) {
        if (card == null || card.length() < 4) {
            return "****";
        }
        return "****" + card.substring(card.length() - 4);
    }
}
