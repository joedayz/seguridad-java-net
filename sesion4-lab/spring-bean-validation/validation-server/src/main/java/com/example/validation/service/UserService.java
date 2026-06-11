package com.example.validation.service;

import org.springframework.stereotype.Service;

import com.example.validation.dto.CreateUserRequest;

@Service
public class UserService {

    public void create(CreateUserRequest request) {
        // Simula persistencia: en una app real aqui iria el repositorio.
    }
}
