# Ejercicio 1 — Configuración completa de protección API (Spring Boot)

Implementación del **Ejercicio 1** del material: API Spring Boot con todas las capas de protección
estudiadas, orientada a superar una auditoría básica OWASP.

## Arquitectura

```
  navegador (http://localhost:8198)          curl -k
        │  fetch HTTPS + CORS                      │
        ▼                                          ▼
  ┌─────────────────────────────────────────────────────────┐
  │  API HTTPS :8443 (Spring Security)                       │
  │  · requiresSecure + HSTS                                   │
  │  · CSP con nonce, X-Frame-Options, Referrer-Policy       │
  │  · CORS restrictivo por entorno                          │
  │  · Rate limit Bucket4j + Redis (auth vs público)           │
  │  · API key (X-Api-Key) en rutas /api/**                    │
  └───────────────────────────┬───────────────────────────────┘
                              │
                              ▼
                        Redis :6380
```

| Servicio | URL |
|----------|-----|
| API (HTTPS) | https://localhost:8443 |
| Cliente CORS (HTTP) | http://localhost:8198 |
| Redis | localhost:6380 |

### Pasos del ejercicio cubiertos

| Paso | Implementación |
|------|----------------|
| **01 HTTPS + HSTS** | TLS en `:8443` (cert autofirmado en Docker; `mkcert` opcional en README) + HSTS en `ApiSecurityConfig` |
| **02 Rate limit Redis** | `Bucket4j` + `Lettuce` — **10/min** auth y **5/min** público en demo (diapositiva: 100/20) |
| **03 Security headers** | HSTS, CSP con **nonce**, `X-Frame-Options: DENY`, Referrer-Policy, CORS por perfil `dev`/`prod` |
| **04 SecurityHeaders.com** | Instrucciones con `mkcert` + túnel (ngrok/cloudflared) |

### Endpoints

| Método | Ruta | Auth | Rate limit |
|--------|------|------|------------|
| GET | `/` | No | — |
| GET | `/api/public/health` | No | 5/min por IP (demo) |
| GET/POST | `/api/datos` | `X-Api-Key: demo-user` | 10/min por API key (demo) |

---

## Cómo levantarlo

```bash
cd sesion3-lab/spring-api-protection
podman compose up --build
# o: docker compose up --build
# o: ./compose.sh up --build
```

---

## Cómo probar

### 1. Navegador (CORS + HTTPS)

1. Acepta el certificado: abre https://localhost:8443/
2. Abre el cliente: http://localhost:8198
3. Pulsa **GET /api/public/health** y **GET /api/datos**

### 2. curl (cabeceras de seguridad)

```bash
# Health público
curl -k -i https://localhost:8443/api/public/health

# Datos protegidos (API key)
curl -k -i https://localhost:8443/api/datos -H "X-Api-Key: demo-user"

# Cabeceras: Strict-Transport-Security, Content-Security-Policy, X-Frame-Options…
curl -k -I https://localhost:8443/
```

### 3. Rate limiting (429)

```bash
for i in $(seq 1 8); do
  curl -sk -o /dev/null -w "public $i: %{http_code}\n" https://localhost:8443/api/public/health
done

for i in $(seq 1 12); do
  curl -sk -o /dev/null -w "auth $i: %{http_code}\n" \
    -H "X-Api-Key: demo-user" https://localhost:8443/api/datos
done
```

### 4. CORS preflight

```bash
curl -k -i -X OPTIONS https://localhost:8443/api/datos \
  -H "Origin: http://localhost:8198" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: X-Api-Key"
```

---

## HTTPS con mkcert (paso 01 — recomendado para SecurityHeaders.com)

```bash
brew install mkcert   # macOS
mkcert -install
mkcert -pkcs12 -p12-password changeit -o certs/localhost.p12 localhost 127.0.0.1
```

Monta el certificado en `docker-compose.yml` o ejecuta en local:

```bash
SSL_KEY_STORE=file:../certs/localhost.p12 SSL_KEY_STORE_PASSWORD=changeit \
  SPRING_DATA_REDIS_HOST=localhost mvn spring-boot:run
```

### SecurityHeaders.com (paso 04)

El scanner necesita una URL **HTTPS pública**. Opciones:

```bash
# Con túnel (ejemplo ngrok)
ngrok http https://localhost:8443
```

Pega la URL `https://….ngrok.io` en https://securityheaders.com

---

## Perfiles CORS

| Perfil | Orígenes |
|--------|----------|
| `dev` (por defecto en Docker) | `http://localhost:8198` |
| `prod` | `https://app.midominio.com`, `https://admin.midominio.com` |

```bash
SPRING_PROFILES_ACTIVE=prod podman compose up --build
```

---

## Variables de entorno

| Variable | Default demo | Diapositiva |
|----------|--------------|-------------|
| `RATELIMIT_AUTH_CAPACITY` | 10 | 100 |
| `RATELIMIT_PUBLIC_CAPACITY` | 5 | 20 |
| `API_KEY` | demo-user | — |
| `CORS_ALLOWED_ORIGINS` | http://localhost:8198 | dominios explícitos |

---

## Ejecutar en local (sin contenedor)

```bash
# Redis
podman run -d --name redis-demo -p 6380:6379 redis:7-alpine

# Keystore TLS
chmod +x scripts/generate-keystore.sh && ./scripts/generate-keystore.sh

cd api-server
SPRING_DATA_REDIS_PORT=6380 mvn spring-boot:run
```

---

## Windows — cmd y curl.exe

```cmd
cd sesion3-lab\spring-api-protection
docker compose up --build
```

```cmd
curl.exe -k -i https://localhost:8443/api/public/health
curl.exe -k -i https://localhost:8443/api/datos -H "X-Api-Key: demo-user"
```
