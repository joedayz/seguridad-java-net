package com.example.demo.web;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rate limiting con Bucket4j + Redis (paso 02 del ejercicio).
 * - Usuario autenticado: 100 req/min (configurable).
 * - Endpoints publicos: 20 req/min por IP (configurable).
 */
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<String> proxyManager;
    private final BucketConfiguration authenticatedBucketConfiguration;
    private final BucketConfiguration publicBucketConfiguration;

    public RateLimitFilter(ProxyManager<String> proxyManager,
                           BucketConfiguration authenticatedBucketConfiguration,
                           BucketConfiguration publicBucketConfiguration) {
        this.proxyManager = proxyManager;
        this.authenticatedBucketConfiguration = authenticatedBucketConfiguration;
        this.publicBucketConfiguration = publicBucketConfiguration;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        boolean isPublic = request.getRequestURI().startsWith("/api/public/");
        String bucketKey = resolveBucketKey(request, isPublic);
        BucketConfiguration configuration = isPublic
                ? publicBucketConfiguration
                : authenticatedBucketConfiguration;

        Bucket bucket = proxyManager.builder().build(bucketKey, () -> configuration);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(probe.getNanosToWaitForRefill() / 1_000_000_000L, 1);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.addHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"error\":\"too_many_requests\","
                        + "\"message\":\"Has superado el limite de peticiones.\","
                        + "\"retryAfterSeconds\":" + retryAfterSeconds + "}");
    }

    private String resolveBucketKey(HttpServletRequest request, boolean isPublic) {
        if (isPublic) {
            return "ratelimit:public:" + clientIp(request);
        }
        String apiKey = request.getHeader("X-Api-Key");
        if (apiKey != null && !apiKey.isBlank()) {
            return "ratelimit:auth:" + apiKey;
        }
        return "ratelimit:auth:ip:" + clientIp(request);
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
