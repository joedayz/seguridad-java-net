package com.example.deserialization.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.example.deserialization.model.User;

@Component
public class SerializationSample {

    public String sampleUserBase64() throws IOException {
        return Base64.getEncoder().encodeToString(serializeUser(new User("ana", "ana@demo.local")));
    }

    public byte[] serializeUser(User user) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(user);
            return bos.toByteArray();
        }
    }
}
