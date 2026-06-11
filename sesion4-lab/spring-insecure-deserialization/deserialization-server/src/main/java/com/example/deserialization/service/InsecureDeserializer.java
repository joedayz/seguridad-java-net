package com.example.deserialization.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.springframework.stereotype.Service;

/**
 * MAL — {@code ObjectInputStream.readObject()} sobre datos no confiables (diapositiva
 * «Patrones inseguros · Deserializacion»). Sin filtro de tipos ni lista blanca de clases.
 */
@Service
public class InsecureDeserializer {

    public Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ois.readObject();
        }
    }
}
