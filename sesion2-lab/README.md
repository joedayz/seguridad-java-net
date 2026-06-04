# Sesión 2 — Laboratorios

| Carpeta | Descripción | Puerto API |
|---------|-------------|------------|
| [spring-security](spring-security) | Spring Boot + **Keycloak** (local) | 8081 |
| [aspnet-identity](aspnet-identity) | ASP.NET Core **Identity** + PostgreSQL | 8082 |
| [aspnet-identity-keycloak](aspnet-identity-keycloak) | ASP.NET Core + **Keycloak** (resource server) | 8085 |
| [entra-spring-security](entra-spring-security) | Spring Boot + **Microsoft Entra ID** | 8083 |
| [entra-aspnet](entra-aspnet) | ASP.NET Core + **Microsoft Entra ID** | 8084 |
| [method-security-keycloak](method-security-keycloak) | Spring **Method Security** (`@PreAuthorize` / `@PostAuthorize`) + Keycloak | 8086 |
| [aspnet-policies-keycloak](aspnet-policies-keycloak) | ASP.NET **Authorization Policies** (rol + claims) + Keycloak | 8087 |
| [pkce-client-keycloak](pkce-client-keycloak) | **Angular SPA** + PKCE + Keycloak + Resource Server (ejercicio 2) | SPA 8093 · API 8088 |

Keycloak en las demos avanzadas usa puertos **8090–8092** (no chocan con `spring-security` en 8080).

Las demos **Entra ID** comparten la misma configuración en Azure Portal (ver README de `entra-spring-security`).

## Windows sin PowerShell

Cada demo incluye la sección **「Windows — cmd y curl.exe (sin PowerShell)」** en su `README.md`, con `docker compose` y todos los pasos usando `curl.exe`.

| Demo | Sección en el README |
|------|----------------------|
| spring-security | [Windows — cmd y curl.exe](spring-security/README.md#windows--cmd-y-curlexe-sin-powershell) |
| aspnet-identity | [Windows — cmd y curl.exe](aspnet-identity/README.md#windows--cmd-y-curlexe-sin-powershell) |
| aspnet-identity-keycloak | [Windows — cmd y curl.exe](aspnet-identity-keycloak/README.md#windows--cmd-y-curlexe-sin-powershell) |
| entra-spring-security | [Windows — cmd y curl.exe](entra-spring-security/README.md#windows--cmd-y-curlexe-sin-powershell) |
| entra-aspnet | [Windows — cmd y curl.exe](entra-aspnet/README.md#windows--cmd-y-curlexe-sin-powershell) |

Los scripts `.ps1` son opcionales.

Los `.sh` del repo usan finales de línea **LF** (`.gitattributes`). Si ves `env: bash\r: No such file or directory`, convertid con `sed -i '' $'s/\r$//' *.sh` en la carpeta de la demo.
