# Sesión 5 — Laboratorios

**Monitorización, auditoría y simulación de ataques** (Módulo IV · Sesión 5 de 5).

Demos alineadas con [docs/Sesion-5.pdf](../docs/Sesion-5.pdf): logging estructurado, Correlation ID,
JWT, Actuator, IDOR, herramientas SAST/DAST/SCA y laboratorio integrador SaaS.

---

## Índice de demos

| Puerto | Bloque | Tema | Carpeta | Stack |
|--------|--------|------|---------|-------|
| **8199** | 1 | Auditoría + Correlation ID | [spring-security-audit](spring-security-audit) | Spring Boot + Security |
| **8200** | 1 | Auditoría + middleware | [aspnet-security-audit](aspnet-security-audit) | ASP.NET Core |
| **8201** | 2 | JWT — none / secreto débil | [spring-jwt-attacks](spring-jwt-attacks) | Spring Boot + jjwt |
| **8202** | 2 | JWT — none / secreto débil | [aspnet-jwt-attacks](aspnet-jwt-attacks) | ASP.NET Core |
| **8208** | 3 | Laboratorio integrador SaaS | [ejercicio-integrador-saas](ejercicio-integrador-saas) | Actuator + IDOR + secretos |

**Requisitos base:** Docker Desktop o Podman con `compose`. Puertos **8199–8208** libres.

---

## Mapa sesión ↔ demos

| Bloque (PDF) | Duración | Demos recomendadas |
|--------------|----------|-------------------|
| **1 · Auditoría y trazabilidad** | 60 min | `spring-security-audit`, `aspnet-security-audit` |
| **2 · Simulación de ataques** | 50 min | `spring-jwt-attacks`, `aspnet-jwt-attacks`, scripts Semgrep/ZAP |
| **3 · Laboratorio integrador** | 50 min | `ejercicio-integrador-saas` + matriz de riesgos |

---

## Cómo ejecutar cualquier demo

```bash
cd <carpeta-de-la-demo>
docker compose up --build
```

Alternativa: `./compose.sh up --build` · Parar: `docker compose down`

---

## Scripts de análisis (Bloque 2)

| Script | Herramienta | Instalación |
|--------|-------------|-------------|
| [scripts/semgrep-scan.sh](scripts/semgrep-scan.sh) | Semgrep (SAST) | `brew install semgrep` |
| [scripts/zap-baseline.sh](scripts/zap-baseline.sh) | OWASP ZAP (DAST) | Solo Docker |
| [scripts/scan-secrets.sh](scripts/scan-secrets.sh) | Gitleaks (secretos) | `brew install gitleaks` o Docker |

```bash
# SAST sobre todo sesion5-lab
./scripts/semgrep-scan.sh .

# DAST contra el integrador (con la API levantada en 8208)
./scripts/zap-baseline.sh http://host.docker.internal:8208

# Secretos hardcodeados en el integrador
./scripts/scan-secrets.sh ejercicio-integrador-saas
```

En **Linux**, sustituye `host.docker.internal` por la IP del host o usa `--network host`.

---

## Herramientas opcionales para la clase

No son obligatorias para las demos Docker; se usan en el Bloque 2:

| Herramienta | Tipo | Uso en la sesión |
|-------------|------|------------------|
| [Semgrep](https://semgrep.dev) | SAST | Patrones SQLi, secretos, Actuator |
| [OWASP ZAP](https://www.zaproxy.org) | DAST | Baseline scan en CI / staging |
| [Gitleaks](https://github.com/gitleaks/gitleaks) | Secret scanning | Historial Git y pre-commit |
| [Burp Suite](https://portswigger.net/burp) | DAST manual | Repeater, Intruder, JWT Editor |
| [Trivy](https://github.com/aquasecurity/trivy) | SCA / imágenes | CVEs en dependencias e imágenes Docker |

---

## Windows sin PowerShell

Cada `README.md` de demo incluye sección **cmd + curl.exe**.

---

## Relación con sesiones anteriores

| Sesión | Conexión con Sesión 5 |
|--------|----------------------|
| 2 | JWT/OAuth — base para ataques JWT |
| 3 | CORS, rate limit — perímetro del integrador |
| 4 | SQLi, IDOR, logging — hallazgos que el integrador revisa de nuevo con SAST/DAST |
