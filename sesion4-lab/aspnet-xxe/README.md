# Demo XXE — ASP.NET Core (antes / después)

Endpoint que parsea perfiles de usuario en XML. Misma idea que la demo Java.

| Variante | Endpoint | Parser |
|----------|----------|--------|
| **ANTES — vulnerable** | `POST /api/profile/vulnerable` | `DtdProcessing.Parse` + `XmlUrlResolver` |
| **DESPUÉS — seguro (DOM)** | `POST /api/profile/seguro` | `XmlDocument` + `XxeMitigationExample` |
| **DESPUÉS — seguro (Reader)** | `POST /api/profile/seguro-reader` | Solo `XmlReader` endurecido |

| Servicio | URL |
|----------|-----|
| API | http://localhost:8188 |

---

## Payload XXE clásico — lectura de archivo

Archivo en Docker: `/app/demo-secrets/secret.txt`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE foo [
  <!ENTITY xxe SYSTEM "file:///app/demo-secrets/secret.txt">
]>
<userProfile>
  <username>&xxe;</username>
</userProfile>
```

**Vulnerable** → `usernameExtraido` contiene el secreto.  
**Seguro** → `exito: false` (DTD prohibido).

---

## Variante SSRF — metadatos simulados

`GET /internal/mock-metadata/iam/security-credentials/demo-role`

Ver `payloads/ssrf-metadata.xml` (puerto **8188**).

---

## La corrección (`XxeMitigationExample.cs`)

```csharp
var settings = new XmlReaderSettings
{
    // Impide que el lector procese DTDs, bloqueando entidades externas.
    DtdProcessing = DtdProcessing.Prohibit,
    // Evita cualquier resolución externa de recursos XML.
    XmlResolver = null
};

using var reader = XmlReader.Create(input, settings);
while (reader.Read())
{
    // Procesa el XML de forma segura.
}
```

> Preferir **JSON** cuando sea posible. Si debes parsear XML, usa siempre
> `DtdProcessing.Prohibit` y `XmlResolver = null`. Evita `XmlDocument` con DTD
> habilitado.

---

## Cómo levantarlo

```bash
docker compose up --build
```

---

## Cómo probar

```bash
# Normal
curl -s -X POST http://localhost:8188/api/profile/vulnerable \
  -H "Content-Type: application/xml" \
  -d '<userProfile><username>ana</username></userProfile>'

# XXE — lectura de archivo (vulnerable)
curl -s -X POST http://localhost:8188/api/profile/vulnerable \
  -H "Content-Type: application/xml" \
  --data-binary @payloads/file-read.xml

# Mismo payload en seguro → falla
curl -s -X POST http://localhost:8188/api/profile/seguro-reader \
  -H "Content-Type: application/xml" \
  --data-binary @payloads/file-read.xml
```
