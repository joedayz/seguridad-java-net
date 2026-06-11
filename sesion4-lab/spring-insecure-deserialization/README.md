# Demo Deserialización insegura — Spring Boot (antes / después)

Patrón de la diapositiva **«Patrones inseguros · Deserialización»**: `ObjectInputStream.readObject()` sobre
datos no confiables frente a **Jackson** con un tipo conocido.

| Variante | Endpoint |
|----------|----------|
| **ANTES — vulnerable** | `POST /api/users/vulnerable/deserialize` |
| **DESPUÉS — seguro** | `POST /api/users/seguro/deserialize` |
| Payload de ejemplo | `GET /api/users/sample-bytes` |

| Servicio | URL |
|----------|-----|
| API | http://localhost:8197 |

> Esta demo **no incluye** payloads de explotación (ysoserial). Muestra el anti-patrón y su corrección con
> datos legítimos. En producción, `readObject()` sin filtro de tipos puede derivar en **RCE**.

---

## Código vulnerable (MAL)

```java
try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
    return ois.readObject(); // acepta cualquier clase serializada
}
```

## Código seguro (BIEN)

```java
return objectMapper.readValue(data, UserDto.class); // solo el esquema esperado (JSON)
```

---

## Cómo levantarlo

```bash
cd spring-insecure-deserialization
docker compose up --build
```

Alternativa: `./compose.sh up --build` · Parar: `docker compose down`

---

## Cómo probar

**1. Obtener bytes serializados de ejemplo**

```bash
curl -s http://localhost:8197/api/users/sample-bytes | jq .
```

**2. Variante vulnerable** (deserializa el `User` legítimo)

```bash
PAYLOAD=$(curl -s http://localhost:8197/api/users/sample-bytes | jq -r .payloadBase64)

curl -s -X POST http://localhost:8197/api/users/vulnerable/deserialize \
  -H "Content-Type: application/json" \
  -d "{\"payloadBase64\":\"$PAYLOAD\"}" | jq .
```

**3. Variante segura** (JSON → `UserDto`)

```bash
curl -s -X POST http://localhost:8197/api/users/seguro/deserialize \
  -H "Content-Type: application/json" \
  -d '{"username":"ana","email":"ana@demo.local"}' | jq .
```

---

## Windows — cmd y curl.exe (sin PowerShell)

```cmd
cd spring-insecure-deserialization
docker compose up --build
curl.exe -s http://localhost:8197/api/users/sample-bytes
```
