# Demo SQL Injection — Spring Boot (antes / después)

Demo del **antes y después** de SQL Injection en Spring Boot. El **mismo** endpoint de
búsqueda por email se expone en dos variantes para verlas lado a lado:

| Variante | Endpoint | Cómo construye la query |
|----------|----------|-------------------------|
| **ANTES — vulnerable** | `GET /api/usuarios/vulnerable?email=...` | Concatena la entrada en la SQL (`Statement`) |
| **DESPUÉS — seguro** | `GET /api/usuarios/seguro?email=...` | `PreparedStatement` con parámetro vinculado |

Es exactamente el patrón vulnerable de la diapositiva *«SQL Injection · Código vulnerable —
Ejemplo real en Java»* (`Statement` + concatenación), y su corrección con consultas
parametrizadas.

La respuesta JSON incluye el campo **`sqlEjecutado`** para que en clase se vea *cómo* la
inyección reescribe la consulta.

## ¿Por qué es explotable?

La versión vulnerable monta la query así:

```java
String sql = "SELECT id, email, nombre, rol, nota_secreta FROM users WHERE email = '" + email + "'";
```

Si el atacante envía `email = ' OR '1'='1`, la query resultante es:

```sql
SELECT id, email, nombre, rol, nota_secreta FROM users WHERE email = '' OR '1'='1'
```

La condición `'1'='1'` es siempre verdadera → la base de datos devuelve **todas** las filas,
incluida la `nota_secreta` del usuario `ADMIN` (una `API_KEY` simulada). La versión segura
trata ese texto como un email literal y no encuentra nada.

## Datos de ejemplo (BD H2 en memoria)

| email | nombre | rol | nota_secreta |
|-------|--------|-----|--------------|
| ana@acme.com | Ana Garcia | USER | Borrador campana marketing Q3 |
| luis@acme.com | Luis Perez | USER | Revision de nomina pendiente |
| admin@acme.com | Root Admin | ADMIN | API_KEY=sk-live-9f3a7c21 (NO COMPARTIR) |

La BD es **H2 en memoria**: la demo arranca sin instalar nada y se siembra al levantar.

### Puerto

| Servicio | URL |
|----------|-----|
| API (Spring Boot) | http://localhost:8181 |

---

## Cómo levantarlo

La primera vez tarda un poco porque compila la app de Spring Boot.

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
curl -s -G "http://localhost:8181/api/usuarios/vulnerable" --data-urlencode "email=ana@acme.com"
```

Devuelve solo a Ana (`totalFilas: 1`).

**2. ANTES (vulnerable) — inyección `' OR '1'='1` → vuelca toda la tabla**

```bash
curl -s -G "http://localhost:8181/api/usuarios/vulnerable" --data-urlencode "email=' OR '1'='1"
```

Devuelve **los 3 usuarios** (`totalFilas: 3`), incluida la `API_KEY` del admin. Fíjate en
`sqlEjecutado`: la condición inyectada quedó dentro de la query.

**3. DESPUÉS (seguro) — el mismo payload no hace nada**

```bash
curl -s -G "http://localhost:8181/api/usuarios/seguro" --data-urlencode "email=' OR '1'='1"
```

Devuelve `totalFilas: 0`: el `PreparedStatement` busca un email que literalmente sea
`' OR '1'='1` y, como no existe, no devuelve filas.

**4. (Opcional) Exfiltración dirigida al admin con comentario `--`**

```bash
curl -s -G "http://localhost:8181/api/usuarios/vulnerable" --data-urlencode "email=' OR rol='ADMIN' --"
```

Devuelve solo la fila del `ADMIN`. El `--` comenta el resto de la consulta.

---

## Windows — cmd y curl.exe (sin PowerShell)

Abre **Símbolo del sistema (cmd)** en la carpeta `spring-sql-injection` y usa `curl.exe`
(incluido en Windows 10/11).

```cmd
docker compose up --build
```

En otra ventana cmd:

```cmd
curl.exe -s -G "http://localhost:8181/api/usuarios/vulnerable" --data-urlencode "email=ana@acme.com"
curl.exe -s -G "http://localhost:8181/api/usuarios/vulnerable" --data-urlencode "email=' OR '1'='1"
curl.exe -s -G "http://localhost:8181/api/usuarios/seguro"     --data-urlencode "email=' OR '1'='1"
```

---

## Ejecutar en local (sin contenedor)

```bash
cd sqli-server
mvn spring-boot:run
```

---

## La corrección, en una línea

```java
// ANTES (vulnerable): la entrada se concatena y se ejecuta como SQL
String sql = "... WHERE email = '" + email + "'";

// DESPUÉS (seguro): la entrada se vincula como dato, nunca como SQL
String sql = "... WHERE email = ?";
ps.setString(1, email);
```

> Regla de oro: **nunca** construyas SQL concatenando entrada del usuario. Usa siempre
> consultas parametrizadas (`PreparedStatement`) o un ORM que las genere por ti.
