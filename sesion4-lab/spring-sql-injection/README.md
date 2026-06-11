# Demo SQL Injection — Spring Boot (antes / después)

Demo del **antes y después** de SQL Injection en Spring Boot. Cada caso expone el **mismo**
endpoint en dos variantes para verlas lado a lado:

### Búsqueda por email (`UsuarioRepository`)

| Variante | Endpoint | Cómo construye la query |
|----------|----------|-------------------------|
| **ANTES — vulnerable** | `GET /api/usuarios/vulnerable?email=...` | Concatena la entrada en la SQL (`Statement`) |
| **DESPUÉS — seguro** | `GET /api/usuarios/seguro?email=...` | `PreparedStatement` con parámetro vinculado |

### Búsqueda de productos con `LIKE` (`ProductSearchService`)

| Variante | Endpoint | Cómo construye la query |
|----------|----------|-------------------------|
| **ANTES — vulnerable** | `GET /api/productos/vulnerable?q=...` | Concatena la entrada dentro del `LIKE '%...%'` |
| **DESPUÉS — seguro** | `GET /api/productos/seguro?q=...` | `PreparedStatement` con `LIKE ?` y `%` en el parámetro |

### Búsqueda por email con Hibernate/JPA (`UserSearchService`)

| Variante | Endpoint | Cómo construye la query |
|----------|----------|-------------------------|
| **ANTES — vulnerable** | `GET /api/usuarios-jpa/vulnerable?email=...` | Concatena la entrada en el HQL (`EntityManager.createQuery`) |
| **DESPUÉS — seguro** | `GET /api/usuarios-jpa/seguro?email=...` | HQL con parámetro nombrado (`:email` + `setParameter`) |

> Usar JPA/Hibernate **no** protege por sí solo: si concatenas la entrada en el HQL, la
> inyección sigue siendo posible. La respuesta de este caso expone `consultaEjecutada`
> (HQL) en lugar de `sqlEjecutado`.

Es exactamente el patrón vulnerable de las diapositivas *«SQL Injection · Código vulnerable —
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

### Caso productos (`LIKE`)

La versión vulnerable monta la query así:

```java
String sql = "SELECT id, name, price FROM products WHERE name LIKE '%" + searchTerm + "%'";
```

Si el atacante envía `q = ' OR '1'='1' --`, la query resultante es:

```sql
SELECT id, name, price FROM products WHERE name LIKE '%' OR '1'='1' --%'
```

El comentario `--` anula el resto de la consulta → devuelve **todos** los productos,
incluida la licencia interna de alto valor. La versión segura trata ese texto como un
nombre literal y no encuentra nada.

### Caso Hibernate/JPA (HQL)

La versión vulnerable monta el HQL así:

```java
String hql = "FROM User u WHERE u.email = '" + email + "'";
return entityManager.createQuery(hql, User.class).getResultList();
```

Si el atacante envía `email = ' OR '1'='1`, el HQL resultante es:

```hql
FROM User u WHERE u.email = '' OR '1'='1'
```

Hibernate lo traduce a SQL con la misma condición siempre verdadera → devuelve **todos** los
usuarios. La versión segura vincula el valor con un parámetro nombrado:

```java
String hql = "FROM User u WHERE u.email = :email";
return entityManager.createQuery(hql, User.class)
        .setParameter("email", email)
        .getResultList();
```

## Datos de ejemplo (BD H2 en memoria)

| email | nombre | rol | nota_secreta |
|-------|--------|-----|--------------|
| ana@acme.com | Ana Garcia | USER | Borrador campana marketing Q3 |
| luis@acme.com | Luis Perez | USER | Revision de nomina pendiente |
| admin@acme.com | Root Admin | ADMIN | API_KEY=sk-live-9f3a7c21 (NO COMPARTIR) |

| name | price |
|------|-------|
| Laptop Pro 15 | 1299.99 |
| Mouse inalambrico | 29.99 |
| Teclado mecanico | 89.50 |
| Monitor 27 pulgadas | 349.00 |
| Licencia interna ERP | 9999.00 |

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

### Productos — búsqueda con `LIKE`

**1. Búsqueda normal**

```bash
curl -s -G "http://localhost:8181/api/productos/vulnerable" --data-urlencode "q=Laptop"
```

Devuelve solo `Laptop Pro 15` (`totalFilas: 1`).

**2. ANTES (vulnerable) — inyección `' OR '1'='1' --` → vuelca todos los productos**

```bash
curl -s -G "http://localhost:8181/api/productos/vulnerable" --data-urlencode "q=' OR '1'='1' --"
```

Devuelve **los 5 productos** (`totalFilas: 5`), incluida la licencia interna.

**3. DESPUÉS (seguro) — el mismo payload no hace nada**

```bash
curl -s -G "http://localhost:8181/api/productos/seguro" --data-urlencode "q=' OR '1'='1' --"
```

Devuelve `totalFilas: 0`: el `PreparedStatement` busca un nombre que literalmente contenga
`' OR '1'='1' --` y, como no existe, no devuelve filas.

### Usuarios — Hibernate/JPA (HQL)

**1. Búsqueda normal**

```bash
curl -s -G "http://localhost:8181/api/usuarios-jpa/vulnerable" --data-urlencode "email=ana@acme.com"
```

**2. ANTES (vulnerable) — inyección `' OR '1'='1` → vuelca toda la tabla**

```bash
curl -s -G "http://localhost:8181/api/usuarios-jpa/vulnerable" --data-urlencode "email=' OR '1'='1"
```

Fíjate en `consultaEjecutada`: el HQL inyectado queda visible en la respuesta.

**3. DESPUÉS (seguro) — el mismo payload no hace nada**

```bash
curl -s -G "http://localhost:8181/api/usuarios-jpa/seguro" --data-urlencode "email=' OR '1'='1"
```

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
curl.exe -s -G "http://localhost:8181/api/productos/vulnerable" --data-urlencode "q=' OR '1'='1' --"
curl.exe -s -G "http://localhost:8181/api/productos/seguro"     --data-urlencode "q=' OR '1'='1' --"
curl.exe -s -G "http://localhost:8181/api/usuarios-jpa/vulnerable" --data-urlencode "email=' OR '1'='1"
curl.exe -s -G "http://localhost:8181/api/usuarios-jpa/seguro"     --data-urlencode "email=' OR '1'='1"
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
String sql = "... WHERE name LIKE '%" + searchTerm + "%'";

// DESPUÉS (seguro): la entrada se vincula como dato, nunca como SQL
String sql = "... WHERE email = ?";
ps.setString(1, email);

String sql = "... WHERE name LIKE ?";
ps.setString(1, "%" + searchTerm + "%");

// Hibernate/JPA (HQL)
String hql = "FROM User u WHERE u.email = '" + email + "'";           // vulnerable
String hql = "FROM User u WHERE u.email = :email";                    // seguro
em.createQuery(hql, User.class).setParameter("email", email);
```

> Regla de oro: **nunca** construyas SQL ni HQL concatenando entrada del usuario. Usa
> siempre consultas parametrizadas (`PreparedStatement`, parámetros nombrados en HQL/JPQL)
> o APIs del ORM que las generen por ti (p. ej. Spring Data `findByEmail`).
