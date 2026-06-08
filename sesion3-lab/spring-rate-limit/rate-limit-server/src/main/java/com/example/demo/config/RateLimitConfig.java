package com.example.demo.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

/**
 * Configura el "bucket" (cubo de tokens) que usa el algoritmo token-bucket de Bucket4j.
 *
 * Para que el 429 sea facil de provocar en una demo en vivo, los valores por defecto son
 * pequenos (5 peticiones por minuto). La diapositiva usa 100/min; aqui lo bajamos para no
 * tener que lanzar 100 peticiones. Se pueden ajustar via variables de entorno.
 */
@Configuration
public class RateLimitConfig {

    @Bean
    public Bucket crearBucket(
            @Value("${ratelimit.capacity:5}") long capacity,
            @Value("${ratelimit.refill-tokens:5}") long refillTokens,
            @Value("${ratelimit.refill-period-seconds:60}") long refillPeriodSeconds) {

        Bandwidth limite = Bandwidth.classic(
                capacity,
                Refill.greedy(refillTokens, Duration.ofSeconds(refillPeriodSeconds)));

        return Bucket.builder().addLimit(limite).build();
    }
}
