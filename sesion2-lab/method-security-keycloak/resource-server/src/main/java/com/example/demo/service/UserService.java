package com.example.demo.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.demo.model.UserDto;

@Service
public class UserService {

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #userId == authentication.name)")
    public UserDto getUser(String userId) {
        return switch (userId) {
            case "alice" -> new UserDto("alice", "Alice Admin", "alice@example.com");
            case "bob" -> new UserDto("bob", "Bob User", "bob@example.com");
            default -> throw new IllegalArgumentException("Usuario no encontrado: " + userId);
        };
    }
}
