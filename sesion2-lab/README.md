# Sesión 2 — Laboratorios

| Carpeta | Descripción | Puerto API |
|---------|-------------|------------|
| [spring-security](spring-security) | Spring Boot + **Keycloak** (local) | 8081 |
| [aspnet-identity](aspnet-identity) | ASP.NET Core **Identity** + PostgreSQL | 8082 |
| [entra-spring-security](entra-spring-security) | Spring Boot + **Microsoft Entra ID** | 8083 |
| [entra-aspnet](entra-aspnet) | ASP.NET Core + **Microsoft Entra ID** | 8084 |

Las demos **Entra ID** comparten la misma configuración en Azure Portal (ver README de `entra-spring-security`).

## Windows sin PowerShell

Cada demo incluye la sección **「Windows — cmd y curl.exe (sin PowerShell)」** en su `README.md`, con `docker compose` y todos los pasos usando `curl.exe`.

| Demo | Sección en el README |
|------|----------------------|
| spring-security | [Windows — cmd y curl.exe](spring-security/README.md#windows--cmd-y-curlexe-sin-powershell) |
| aspnet-identity | [Windows — cmd y curl.exe](aspnet-identity/README.md#windows--cmd-y-curlexe-sin-powershell) |
| entra-spring-security | [Windows — cmd y curl.exe](entra-spring-security/README.md#windows--cmd-y-curlexe-sin-powershell) |
| entra-aspnet | [Windows — cmd y curl.exe](entra-aspnet/README.md#windows--cmd-y-curlexe-sin-powershell) |

Los scripts `.ps1` son opcionales.

Los `.sh` del repo usan finales de línea **LF** (`.gitattributes`). Si ves `env: bash\r: No such file or directory`, convertid con `sed -i '' $'s/\r$//' *.sh` en la carpeta de la demo.
