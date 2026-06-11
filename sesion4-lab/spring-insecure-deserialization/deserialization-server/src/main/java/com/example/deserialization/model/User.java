package com.example.deserialization.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * POJO de ejemplo para la demo. En la variante vulnerable cualquier clase serializada
 * puede llegar a {@code ObjectInputStream.readObject()} sin filtro de tipos.
 */
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
    private String email;

    public User() {
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
