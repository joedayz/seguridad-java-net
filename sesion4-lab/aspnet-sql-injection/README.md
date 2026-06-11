# Demo SQL Injection — ASP.NET Core (antes / después)

Demo del **antes y después** de SQL Injection en ASP.NET Core. El **mismo** endpoint de
búsqueda por email se expone en dos variantes para verlas lado a lado:

| Variante | Endpoint | Cómo construye la query |
|----------|----------|-------------------------|
| **ANTES — vulnerable** | `GET /api/usuarios/vulnerable?email=...` | Interpola la entrada en la SQL (`$"... '{email}'"`) |
| **DESPUÉS — seguro** | `GET /api/usuarios/seguro?email=...` | Consulta parametrizada (`$email` + `Parameters.AddWithValue`) |

Es el mismo problema de la diapositiva *«SQL Injection · Código vulnerable en .NET»*
(concatenación de strings en la query) y su corrección con parámetros.

> La diapositiva usa `Microsoft.Data.SqlClient` (SQL Server). Para que la demo arranque sin
> instalar SQL Server, aquí usamos **SQLite embebido** (`Microsoft.Data.Sqlite`). El patrón
> vulnerable (concatenación) y la corrección (parámetros) son **idénticos** en ambos.

La respuesta JSON incluye el campo **`sqlEjecutado`** para que en clase se vea *cómo* la
inyección reescribe la consulta.

## ¿Por qué es explotable?

La versión vulnerable monta la query así:

```csharp
var sql = $"SELECT id, email, nombre, rol, nota_secreta FROM users WHERE email = '{email}'";
```

Si el atacante envía `email = ' OR '1'='1`, la query resultante es:

```sql
SELECT id, email, nombre, rol, nota_secreta FROM users WHERE email = '' OR '1'='1'
```

La condición `'1'='1'` es siempre verdadera → la base de datos devuelve **todas** las filas,
incluida la `nota_secreta` del usuario `ADMIN` (una `API_KEY` simulada). La versión segura
trata ese texto como un email literal y no encuentra nada.

## Datos de ejemplo (SQLite en memoria)

| email | nombre | rol | nota_secreta |
|-------|--------|-----|--------------|
| ana@acme.com | Ana Garcia | USER | Borrador campana marketing Q3 |
| luis@acme.com | Luis Perez | USER | Revision de nomina pendiente |
| admin@acme.com | Root Admin | ADMIN | API_KEY=sk-live-9f3a7c21 (NO COMPARTIR) |

### Puerto

| Servicio | URL |
|----------|-----|
| API (ASP.NET Core) | http://localhost:8182 |

---

## Cómo levantarlo

La primera vez tarda un poco porque compila la app de .NET.

### Podman (macOS / Linux / Windows)

```bash
podman machine start   # solo la primera vez o si está parada
podman compose up --build
```

### Docker Desktop (macOS / Linux / Windows)

```bash
docker compose up --build
docker compose down
```

### Script de ayuda (detecta Podman o Docker)

```bash
chmod +x compose.sh
./compose.sh up --build
./compose.sh down
```

En Windows con PowerShell: `.\compose.ps1 up --build`.

---

## Cómo probar (la demo del antes / después)

> Usamos `--data-urlencode` para que `curl` codifique correctamente los espacios y comillas
> del payload. Si pones el payload directamente en la URL, `curl` fallará.

### macOS / Linux (bash)

**1. Búsqueda normal — la app funciona como se espera**

```bash
curl -s -G "http://localhost:8182/api/usuarios/vulnerable" --data-urlencode "email=ana@acme.com"
```

Devuelve solo a Ana (`totalFilas: 1`).

**2. ANTES (vulnerable) — inyección `' OR '1'='1` → vuelca toda la tabla**

```bash
curl -s -G "http://localhost:8182/api/usuarios/vulnerable" --data-urlencode "email=' OR '1'='1"
```

Devuelve **los 3 usuarios** (`totalFilas: 3`), incluida la `API_KEY` del admin.

**3. DESPUÉS (seguro) — el mismo payload no hace nada**

```bash
curl -s -G "http://localhost:8182/api/usuarios/seguro" --data-urlencode "email=' OR '1'='1"
```

Devuelve `totalFilas: 0`: la consulta parametrizada busca un email que literalmente sea
`' OR '1'='1` y, como no existe, no devuelve filas.

**4. (Opcional) Exfiltración dirigida al admin con comentario `--`**

```bash
curl -s -G "http://localhost:8182/api/usuarios/vulnerable" --data-urlencode "email=' OR rol='ADMIN' --"
```

Devuelve solo la fila del `ADMIN`.

---

## Windows — cmd y curl.exe (sin PowerShell)

Abre **Símbolo del sistema (cmd)** en la carpeta `aspnet-sql-injection` y usa `curl.exe`
(incluido en Windows 10/11).

```cmd
docker compose up --build
```

En otra ventana cmd:

```cmd
curl.exe -s -G "http://localhost:8182/api/usuarios/vulnerable" --data-urlencode "email=ana@acme.com"
curl.exe -s -G "http://localhost:8182/api/usuarios/vulnerable" --data-urlencode "email=' OR '1'='1"
curl.exe -s -G "http://localhost:8182/api/usuarios/seguro"     --data-urlencode "email=' OR '1'='1"
```

---

## Ejecutar en local (sin contenedor)

```bash
cd sqli-demo
dotnet run
```

---

## La corrección, en una línea

```csharp
// ANTES (vulnerable): la entrada se interpola y se ejecuta como SQL
var sql = $"... WHERE email = '{email}'";

// DESPUÉS (seguro): la entrada se vincula como dato, nunca como SQL
command.CommandText = "... WHERE email = $email";
command.Parameters.AddWithValue("$email", email);
```

> En Entity Framework, LINQ ya genera SQL parametrizado. Si necesitas SQL nativo usa
> `FromSqlInterpolated` (parametriza) en lugar de `FromSqlRaw` con concatenación.
