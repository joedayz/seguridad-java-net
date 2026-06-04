package com.example.demo.service;

import java.util.Map;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import com.example.demo.model.Document;

@Service
public class DocumentService {

    private static final Map<Long, Document> DOCUMENTS = Map.of(
        1L, new Document(1L, "alice", "Informe de arquitectura (Alice)"),
        2L, new Document(2L, "bob", "Notas de sprint (Bob)"));

    @PostAuthorize("returnObject.ownerId == authentication.name")
    public Document getDocument(Long id) {
        Document document = DOCUMENTS.get(id);
        if (document == null) {
            throw new IllegalArgumentException("Documento no encontrado: " + id);
        }
        return document;
    }
}
