# Demo XSS — Razor Pages y ASP.NET Core (antes / después)

Demo del **antes y después** de Cross-Site Scripting (XSS) reflejado en ASP.NET Core con
Razor Pages. El **mismo** comentario de usuario se muestra en dos variantes:

| Variante | URL | Qué hace mal / bien |
|----------|-----|---------------------|
| **ANTES — vulnerable** | http://localhost:8184/comments | `@Html.Raw(Model.UserComment)` |
| **DESPUÉS — seguro** | http://localhost:8184/secure-comments | `@Model.UserComment` + `HtmlEncoder` |

Es el patrón de la diapositiva *«XSS .NET · Razor Pages y ASP.NET Core»*.

## ¿Por qué es explotable?

**Vista vulnerable** (`Pages/Comment.cshtml`):

```razor
@Html.Raw(Model.UserComment)
```

`Html.Raw` renderiza el string **sin codificar HTML**. Si el usuario envía
`<script>alert('XSS')</script>`, el navegador lo ejecuta.

**Code-behind** — toma la entrada del query string o del formulario sin sanitizar:

```csharp
UserComment = Request.Query["comment"].ToString();
```

## La corrección

**Vista segura** (`Pages/SecureComment.cshtml`):

```razor
@Model.UserComment
```

Razor codifica HTML automáticamente con `@`. Siempre preferir que Razor codifique los
valores.

**Code-behind seguro** — codificación explícita cuando haga falta:

```csharp
EncodedComment = HtmlEncoder.Default.Encode(UserComment);
```

---

## Cómo levantarlo

```bash
docker compose up --build
# o: ./compose.sh up --build
```

| Servicio | URL |
|----------|-----|
| Inicio | http://localhost:8184/ |
| Vulnerable | http://localhost:8184/comments |
| Seguro | http://localhost:8184/secure-comments |

---

## Cómo probar (la demo del antes / después)

Abre en el navegador:

1. **ANTES** — http://localhost:8184/comments  
   Pega en el textarea: `<script>alert('XSS')</script>` → verás el `alert`.

2. **Por query string** (como en la diapositiva):  
   http://localhost:8184/comments?comment=%3Cscript%3Ealert('XSS')%3C/script%3E

3. **DESPUÉS** — http://localhost:8184/secure-comments  
   Mismo payload → texto literal, sin `alert`.

---

## Ejecutar en local (sin contenedor)

```bash
cd xss-demo
dotnet run --urls http://localhost:8184
```

---

## Regla de oro

- **Nunca** uses `@Html.Raw()` con entrada del usuario.
- Usa **`@Model.Propiedad`** y deja que Razor codifique automáticamente.
- Si necesitas codificar en C#, usa **`HtmlEncoder.Default.Encode()`**.
