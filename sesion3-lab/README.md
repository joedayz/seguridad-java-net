# Sesión 3 — Laboratorios

Demos de **rate limiting** y **CORS** en los dos stacks.

## Rate Limiting

Ambas devuelven **`429 Too Many Requests`** con la cabecera **`Retry-After`** al superar el límite.

| Carpeta | Descripción | Puerto API |
|---------|-------------|------------|
| [spring-rate-limit](spring-rate-limit) | Spring Boot + **Bucket4j** (token-bucket) | 8181 |
| [aspnet-rate-limit](aspnet-rate-limit) | ASP.NET Core 7+ **middleware nativo** (FixedWindow) | 8182 |

Límite por defecto: **5 peticiones/minuto** (configurable; las diapositivas usan 100/min).

## CORS

| Carpeta | Descripción | Puerto API |
|---------|-------------|------------|
| [spring-cors](spring-cors) | Spring Security — orígenes explícitos, sin `*` + credenciales | API 8183 · clientes 8193/8195/8196 |
| [aspnet-cors](aspnet-cors) | ASP.NET Core — políticas `PoliticaProduccion` / `PoliticaDesarrollo` | API 8184 · clientes 8194/8197 |

## Ejercicio 2 — Key Vault (ASP.NET)

| Carpeta | Descripción | Puerto |
|---------|-------------|--------|
| [aspnet-keyvault](aspnet-keyvault) | Secretos en **Azure Key Vault** + `DefaultAzureCredential` (mismo tenant Entra) | 8085 |

## Ejercicio 1 — Protección API integrada (Spring)

| Carpeta | Descripción | Puertos |
|---------|-------------|---------|
| [spring-api-protection](spring-api-protection) | HTTPS + HSTS + Bucket4j/Redis + security headers + CORS | API **8443** · cliente 8198 · Redis 6380 |

## Windows sin PowerShell

Cada demo incluye la sección **「Windows — cmd y curl.exe (sin PowerShell)」** en su `README.md`,
con `docker compose` y todos los pasos usando `curl.exe`.

Los scripts `.ps1` son opcionales.

Los `.sh` del repo usan finales de línea **LF** (`.gitattributes`). Si ves
`env: bash\r: No such file or directory`, convertid con `sed -i '' $'s/\r$//' *.sh` en la
carpeta de la demo.
