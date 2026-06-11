# Ejercicio integrador — Auditoría de `POST /api/reports/generate`

Escenario de la diapositiva **«Taller avanzado · Ejercicio integrador»**: un endpoint B2B que recibe XML,
consulta la BD, genera un PDF simulado y lo devuelve.

| Versión | Endpoint | Auth |
|---------|----------|------|
| **MAL** | `POST /api/reports/vulnerable/generate` | Ninguna |
| **BIEN** | `POST /api/reports/seguro/generate` | `X-Api-Key: demo-reports-key` |

Puerto: **8198**

---

## Vectores a evaluar (versión MAL)

| Vector | Problema en MAL | Mitigación en BIEN |
|--------|-----------------|-------------------|
| XML | Parser sin bloquear DTD/XXE | `disallow-doctype-decl` + sin entidades externas |
| SQL | `category` concatenado en la query | `NamedParameterJdbcTemplate` |
| Auth | Sin API key | Cabecera `X-Api-Key` |
| Abuso | Sin límite de peticiones | Rate limit en memoria (5/min por `X-Client-Id`) |
| PDF / fichero | `fileName` sin validar (path traversal) | Allowlist `[a-zA-Z0-9_-]{1,64}` |
| Logging | XML completo en log | Audit log sin datos sensibles |

---

## XML de ejemplo

```xml
<?xml version="1.0" encoding="UTF-8"?>
<report>
  <category>electronics</category>
  <fileName>informe-ventas</fileName>
</report>
```

Guarda el bloque en `report.xml` o usa `curl --data-binary @report.xml`.

---

## Cómo levantarlo

```bash
cd ejercicio3-integrador
docker compose up --build
```

Alternativa: `./compose.sh up --build`

---

## Cómo probar

**Informe legítimo — vulnerable**

```bash
curl -s -X POST http://localhost:8198/api/reports/vulnerable/generate \
  -H "Content-Type: application/xml" \
  --data-binary @- <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<report>
  <category>electronics</category>
  <fileName>informe-ventas</fileName>
</report>
EOF
```

**SQLi en `category` (MAL)** — fíjate en `sqlEjecutado` y el número de filas:

```bash
curl -s -X POST http://localhost:8198/api/reports/vulnerable/generate \
  -H "Content-Type: application/xml" \
  --data-binary @- <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<report>
  <category>electronics' OR '1'='1</category>
  <fileName>informe</fileName>
</report>
EOF
```

**Mismo informe — seguro** (requiere API key):

```bash
curl -s -X POST http://localhost:8198/api/reports/seguro/generate \
  -H "Content-Type: application/xml" \
  -H "X-Api-Key: demo-reports-key" \
  -H "X-Client-Id: cliente-b2b-1" \
  --data-binary @- <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<report>
  <category>electronics</category>
  <fileName>informe-ventas</fileName>
</report>
EOF
```

**`fileName` malicioso — seguro devuelve 400**

```bash
curl -s -X POST http://localhost:8198/api/reports/seguro/generate \
  -H "Content-Type: application/xml" \
  -H "X-Api-Key: demo-reports-key" \
  --data-binary @- <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<report>
  <category>electronics</category>
  <fileName>../../../etc/passwd</fileName>
</report>
EOF
```

**Rate limit (429)** — ejecuta más de 5 veces seguidas con el mismo `X-Client-Id`.

---

## Windows — cmd y curl.exe (sin PowerShell)

```cmd
cd ejercicio3-integrador
docker compose up --build
```

Crea `report.xml` con el bloque XML de arriba y:

```cmd
curl.exe -s -X POST http://localhost:8198/api/reports/seguro/generate -H "Content-Type: application/xml" -H "X-Api-Key: demo-reports-key" --data-binary @report.xml
```
