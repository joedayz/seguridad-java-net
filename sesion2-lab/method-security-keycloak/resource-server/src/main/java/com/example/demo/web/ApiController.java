package com.example.demo.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Document;
import com.example.demo.model.UserDto;
import com.example.demo.service.DocumentService;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final UserService userService;
    private final DocumentService documentService;

    public ApiController(UserService userService, DocumentService documentService) {
        this.userService = userService;
        this.documentService = documentService;
    }

    @GetMapping("/public/hello")
    public Map<String, Object> publicHello() {
        return Map.of(
            "message", "Demo Spring Method Security + Keycloak",
            "scope", "public",
            "hint", "Usa @PreAuthorize y @PostAuthorize en la capa de servicio");
    }

    @GetMapping("/users/{userId}")
    public UserDto getUser(@PathVariable String userId) {
        return userService.getUser(userId);
    }

    @GetMapping("/documents/{id}")
    public Document getDocument(@PathVariable Long id) {
        return documentService.getDocument(id);
    }
}
