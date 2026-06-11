package com.example.logging.dto;

public record LoginRequest(
        String username,
        String password,
        String jwtToken,
        String creditCardNumber) {
}
