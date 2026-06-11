# Demo XSS — Spring Boot + Thymeleaf (antes / después)

Demo del **antes y después** de Cross-Site Scripting (XSS) reflejado en Spring Boot con
Thymeleaf. El **mismo** formulario de comentarios se expone en dos variantes:

| Variante | URL | Qué hace mal / bien |
|----------|-----|---------------------|
| **ANTES — vulnerable** | http://localhost:8183/comments | `th:utext` renderiza HTML sin escapar |
| **DESPUÉS — seguro** | http://localhost:8183/secure-comments | `HtmlUtils.htmlEscape()` + `th:text` |

Es el patrón de la diapositiva *«XSS · Código vulnerable en Spring Boot + Thymeleaf»*:
la entrada del usuario llega al modelo sin sanitizar y la plantilla usa `th:utext`, que
ejecuta `<script>` en el navegador.

## ¿Por qué es explotable?

**Controlador vulnerable** — pasa la entrada tal cual al modelo:

```java
model.addAttribute("comment", comment);
```

**Plantilla vulnerable** — renderiza sin escapar:

```html
<div th:utext="${comment}"></div>
```

Si el usuario envía `<script>alert('XSS')</script>`, el navegador lo ejecuta.

Equivalente inseguro en JSP: `${comment}` o `<c:out escapeXml="false">`.

## La corrección

**Controlador seguro** — escapa en el servidor:

```java
String safeComment = HtmlUtils.htmlEscape(comment);
model.addAttribute("comment", safeComment);
```

**Plantilla segura** — escapa automáticamente:

```html
<div th:text="${comment}"></div>
```

Con el mismo payload, el texto se muestra literalmente (`&lt;script&gt;...`) y el script
no se ejecuta.

---

## Cómo levantarlo

Requisitos: Docker Desktop o Podman con `compose`.

### Podman (macOS / Linux / Windows)

```bash
cd spring-xss-thymeleaf
podman machine start   # solo la primera vez o si está parada
podman compose up --build
```

### Docker Desktop

```bash
cd spring-xss-thymeleaf
docker compose up --build
docker compose down
```

### Script de ayuda

```bash
chmod +x compose.sh
./compose.sh up --build
./compose.sh down
```

En Windows con PowerShell: `.\compose.ps1 up --build`.

| Servicio | URL |
|----------|-----|
| Inicio | http://localhost:8183/ |
| Vulnerable | http://localhost:8183/comments |
| Seguro | http://localhost:8183/secure-comments |

---

## Cómo probar (la demo del antes / después)

Abre en el navegador (no sirve `curl` para ver el `alert`):

1. **Inicio** — http://localhost:8183/
2. **ANTES** — entra en *Vulnerable*, pega en el textarea:
   ```html
   <script>alert('XSS')</script>
   ```
   Pulsa *Enviar* → verás el cuadro de alerta del navegador.
3. **DESPUÉS** — entra en *Seguro*, pega el mismo payload → se muestra como texto;
   no hay `alert`.

Variantes útiles en clase:

```html
<img src=x onerror="alert('XSS')">
<a href="javascript:alert('XSS')">click</a>
```

---

## Ejecutar en local (sin contenedor)

```bash
cd xss-server
mvn spring-boot:run
```

---

## Regla de oro

- Usa **`th:text`** (escapado por defecto), no `th:utext`, salvo que el HTML provenga de
  una fuente 100 % confiable y esté sanitizado.
- Si escapas en el controlador, usa **`HtmlUtils.htmlEscape()`** (o equivalente).
- Nunca reflejes entrada del usuario con salida sin escapar (Thymeleaf, JSP, Razor, etc.).

---

## Windows — cmd y curl.exe (sin PowerShell)

```cmd
cd spring-xss-thymeleaf
docker compose up --build
```

Abre en el navegador: http://localhost:8183/comments
