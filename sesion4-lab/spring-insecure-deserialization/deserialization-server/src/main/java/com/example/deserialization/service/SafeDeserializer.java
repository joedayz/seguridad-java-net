package com.example.deserialization.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.example.deserialization.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * BIEN — Jackson con tipo conocido en lugar de serializacion Java nativa (diapositiva).
 */
@Service
public class SafeDeserializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserDto deserialize(byte[] jsonUtf8) throws IOException {
        return objectMapper.readValue(jsonUtf8, UserDto.class);
    }
}
