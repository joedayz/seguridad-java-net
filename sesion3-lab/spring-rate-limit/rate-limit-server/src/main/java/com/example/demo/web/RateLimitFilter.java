package com.example.demo.web;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro HTTP que aplica rate limiting a todas las rutas bajo /api/.
 *
 * Por cada peticion intenta consumir 1 token del bucket:
 *  - Si hay tokens disponibles -> deja pasar la peticion y publica cabeceras X-Rate-Limit-*.
 *  - Si NO hay tokens -> responde 429 Too Many Requests con la cabecera Retry-After,
 *    para que el cliente implemente backoff exponencial.
 */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final Bucket bucket;

    public RateLimitFilter(Bucket bucket) {
        this.bucket = bucket;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Solo limitamos la API; recursos estaticos o actuator quedarian fuera.
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000L;

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.addHeader("Retry-After", String.valueOf(Math.max(retryAfterSeconds, 1)));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"error\":\"too_many_requests\","
                        + "\"message\":\"Has superado el limite de peticiones. Reintenta mas tarde.\","
                        + "\"retryAfterSeconds\":" + Math.max(retryAfterSeconds, 1) + "}");
    }
}
