package com.example.deserialization.web;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.deserialization.dto.DeserializeBytesRequest;
import com.example.deserialization.dto.UserDto;
import com.example.deserialization.model.User;
import com.example.deserialization.service.InsecureDeserializer;
import com.example.deserialization.service.SafeDeserializer;
import com.example.deserialization.service.SerializationSample;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final InsecureDeserializer insecureDeserializer;
    private final SafeDeserializer safeDeserializer;
    private final SerializationSample serializationSample;

    public UserController(
            InsecureDeserializer insecureDeserializer,
            SafeDeserializer safeDeserializer,
            SerializationSample serializationSample) {
        this.insecureDeserializer = insecureDeserializer;
        this.safeDeserializer = safeDeserializer;
        this.serializationSample = serializationSample;
    }

    @GetMapping("/sample-bytes")
    public Map<String, Object> sampleBytes() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("descripcion", "Bytes Java serializados de un User legitimo (solo para probar la variante vulnerable)");
        body.put("payloadBase64", serializationSample.sampleUserBase64());
        body.put("advertencia",
                "ObjectInputStream sin filtro acepta cualquier gadget chain; en produccion puede llevar a RCE");
        return body;
    }

    @PostMapping("/vulnerable/deserialize")
    public ResponseEntity<Map<String, Object>> deserializeVulnerable(@RequestBody DeserializeBytesRequest req)
            throws Exception {
        byte[] bytes = Base64.getDecoder().decode(req.payloadBase64());
        Object obj = insecureDeserializer.deserialize(bytes);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("modo", "VULNERABLE — ObjectInputStream.readObject() sin validar tipo");
        body.put("claseDeserializada", obj.getClass().getName());

        if (obj instanceof User user) {
            body.put("usuario", Map.of("username", user.getUsername(), "email", user.getEmail()));
        } else {
            body.put("objeto", String.valueOf(obj));
        }

        body.put("riesgo",
                "Un atacante puede enviar payloads serializados maliciosos (ysoserial) si el endpoint acepta bytes arbitrarios");
        return ResponseEntity.ok(body);
    }

    @PostMapping("/seguro/deserialize")
    public ResponseEntity<Map<String, Object>> deserializeSeguro(@RequestBody UserDto user) throws Exception {
        byte[] json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(user);
        UserDto parsed = safeDeserializer.deserialize(json);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("modo", "SEGURO — Jackson ObjectMapper.readValue(data, UserDto.class)");
        body.put("usuario", Map.of("username", parsed.username(), "email", parsed.email()));
        return ResponseEntity.ok(body);
    }
}
