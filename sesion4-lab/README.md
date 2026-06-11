# Sesión 4 — Laboratorios

Demos de **vulnerabilidades y desarrollo seguro** (OWASP Top 10) en los dos stacks.

## SQL Injection — el antes y el después

El **mismo** endpoint de búsqueda por email, en dos variantes para verlas lado a lado:
una **vulnerable** (concatenación de la entrada en la SQL) y otra **segura** (consulta
parametrizada). La respuesta incluye el `sqlEjecutado` para ver cómo la inyección
`' OR '1'='1` reescribe la query y vuelca toda la tabla, incluida una `API_KEY` simulada.

| Carpeta | Descripción | Puerto API |
|---------|-------------|------------|
| [spring-sql-injection](spring-sql-injection) | Spring Boot + H2 · `Statement` (vulnerable) vs `PreparedStatement` (seguro) | 8181 |
| [aspnet-sql-injection](aspnet-sql-injection) | ASP.NET Core + SQLite · interpolación (vulnerable) vs consulta parametrizada (seguro) | 8182 |

Endpoints (idénticos en ambos stacks):

| Variante | Endpoint |
|----------|----------|
| ANTES — vulnerable | `GET /api/usuarios/vulnerable?email=...` |
| DESPUÉS — seguro | `GET /api/usuarios/seguro?email=...` |

Prueba rápida (sustituye el puerto por 8181 Spring / 8182 .NET):

```bash
# Normal
curl -s -G "http://localhost:8181/api/usuarios/vulnerable" --data-urlencode "email=ana@acme.com"
# Inyección en la versión vulnerable → vuelca toda la tabla
curl -s -G "http://localhost:8181/api/usuarios/vulnerable" --data-urlencode "email=' OR '1'='1"
# Mismo payload en la versión segura → 0 filas
curl -s -G "http://localhost:8181/api/usuarios/seguro"     --data-urlencode "email=' OR '1'='1"
```

## Windows sin PowerShell

Cada demo incluye la sección **「Windows — cmd y curl.exe (sin PowerShell)」** en su
`README.md`, con `docker compose` y todos los pasos usando `curl.exe`.

Los scripts `.ps1` son opcionales.

Los `.sh` del repo usan finales de línea **LF** (`.gitattributes`). Si ves
`env: bash\r: No such file or directory`, convertid con `sed -i '' $'s/\r$//' *.sh` en la
carpeta de la demo.
