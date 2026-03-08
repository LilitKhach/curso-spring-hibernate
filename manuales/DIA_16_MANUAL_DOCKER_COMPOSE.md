# Día 16: Docker Compose — Orquestando su Aplicación Completa

Ayer dockerizaron su aplicación. Hoy le agregan una base de datos real y un panel de administración, todo levantándose con un solo comando.

Prof. Juan Marcelo Gutierrez Miranda

**Curso IFCD0014 — Semana 4, Día 16 (Martes)**
**Objetivo:** Entender Docker Compose, migrar de H2 a PostgreSQL, definir servicios en `docker-compose.yml`, usar volúmenes para persistencia y redes para comunicación entre contenedores.

> Este manual es de consulta. Sigan los pasos con Docker Desktop corriendo y el proyecto abierto.

---

# PARTE I — EL PROBLEMA

# 1. Nuestra App Usa H2: No Es Suficiente

Hasta ahora su aplicación Spring Boot usa H2, una base de datos en memoria. Cuando detienen la app, los datos desaparecen.

```
╔══════════════════════════════════════════════════════════════╗
║              H2 vs POSTGRESQL                                ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  H2 (lo que tienen ahora):                                   ║
║  - Base de datos en memoria                                  ║
║  - Datos desaparecen al reiniciar                            ║
║  - Perfecta para desarrollo y tests                          ║
║  - No requiere instalación                                   ║
║                                                              ║
║  PostgreSQL (lo que usan en producción):                     ║
║  - Base de datos real, en disco                              ║
║  - Datos persisten entre reinicios                           ║
║  - Usada por miles de empresas                               ║
║  - Requiere un servidor (o un contenedor Docker)             ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

Además, una aplicación real no es solo "la app". Necesita:

- La aplicación Spring Boot
- Una base de datos (PostgreSQL)
- Quizás un panel de administración de la BD (Adminer)
- Quizás un servidor de caché (Redis)
- Quizás un servidor de mensajería (RabbitMQ)

Ejecutar un `docker run` por cada servicio es tedioso y propenso a errores. Docker Compose resuelve esto: **definen todo en un archivo YAML y levantan todo con un solo comando**.

---

# PARTE II — DE H2 A POSTGRESQL

# 2. Cambios en el Proyecto Spring Boot

## Paso 1: Agregar el driver PostgreSQL al pom.xml

Dentro de `<dependencies>` en el `pom.xml`, agregar:

```xml
        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
```

> Si usan Spring Boot parent, no necesitan especificar la versión — Spring Boot la gestiona.

Pueden dejar H2 también (útil para tests):

```xml
        <!-- H2 para tests -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
```

## Paso 2: Configurar application.properties para PostgreSQL

Actualizar `src/main/resources/application.properties`:

```properties
# ============================================================
# Configuracion de base de datos — PostgreSQL
# ============================================================
spring.datasource.url=jdbc:postgresql://db:5432/pizzeria
spring.datasource.username=postgres
spring.datasource.password=secret
spring.datasource.driver-class-name=org.postgresql.Driver

# ============================================================
# Configuracion de JPA/Hibernate
# ============================================================
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### ¿Por qué la URL dice "db" y no "localhost"?

Fíjense en la URL: `jdbc:postgresql://db:5432/pizzeria`

El `db` no es localhost. Es el **nombre del servicio** de PostgreSQL en Docker Compose. Cuando Docker Compose levanta los contenedores, crea una red interna donde cada servicio se identifica por su nombre. Esto lo vemos en la sección 5.

> Si ejecutan la app fuera de Docker (desde IntelliJ), cambien `db` por `localhost`.

---

# PARTE III — DOCKER COMPOSE

# 3. El Archivo docker-compose.yml

Creen un archivo `docker-compose.yml` en la raíz del proyecto (al lado del `Dockerfile`):

```yaml
version: '3.8'

services:
  # =====================================================
  # Servicio 1: La aplicacion Spring Boot
  # =====================================================
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/pizzeria
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=secret
    depends_on:
      - db

  # =====================================================
  # Servicio 2: Base de datos PostgreSQL
  # =====================================================
  db:
    image: postgres:16-alpine
    environment:
      - POSTGRES_DB=pizzeria
      - POSTGRES_PASSWORD=secret
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  # =====================================================
  # Servicio 3: Adminer (panel web para ver la BD)
  # =====================================================
  adminer:
    image: adminer
    ports:
      - "9090:8080"

# =======================================================
# Volumenes (para que los datos persistan)
# =======================================================
volumes:
  postgres_data:
```

## Explicación sección por sección

### `services:` — Los contenedores que se van a crear

Cada bloque dentro de `services` define un contenedor:

| Servicio | Qué es | Imagen |
|----------|--------|--------|
| `app` | Su aplicación Spring Boot | Se construye desde el `Dockerfile` local |
| `db` | PostgreSQL | `postgres:16-alpine` de Docker Hub |
| `adminer` | Panel web para administrar la BD | `adminer` de Docker Hub |

### `app:` — Su aplicación

```yaml
  app:
    build: .                    # Construir imagen desde el Dockerfile en "."
    ports:
      - "8080:8080"             # Puerto local:puerto contenedor
    environment:                # Variables de entorno que Spring Boot lee
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/pizzeria
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=secret
    depends_on:                 # No iniciar hasta que "db" este listo
      - db
```

### `db:` — PostgreSQL

```yaml
  db:
    image: postgres:16-alpine   # Descargar de Docker Hub (no build local)
    environment:
      - POSTGRES_DB=pizzeria    # Crear la base de datos "pizzeria" al iniciar
      - POSTGRES_PASSWORD=secret
    ports:
      - "5432:5432"             # Para conectarse desde herramientas externas
    volumes:
      - postgres_data:/var/lib/postgresql/data   # Persistir datos
```

### `adminer:` — Panel de administración

```yaml
  adminer:
    image: adminer              # Imagen oficial de Adminer
    ports:
      - "9090:8080"             # Adminer corre en 8080, lo exponemos en 9090
```

### `volumes:` — Almacenamiento persistente

```yaml
volumes:
  postgres_data:                # Volumen con nombre — los datos sobreviven
```

---

# 4. Ejecutar con Compose

## Levantar todo

Desde la raíz del proyecto (donde está `docker-compose.yml`):

```bash
# Levantar en primer plano (ven todos los logs)
docker-compose up
```

Van a ver los logs de los tres servicios mezclados, con colores diferentes. Para salir: `Ctrl + C` (detiene todo).

Para trabajar más cómodos:

```bash
# Levantar en segundo plano
docker-compose up -d
```

La primera vez tarda más porque construye la imagen de la app y descarga PostgreSQL y Adminer.

## Ver el estado

```bash
# Ver que servicios estan corriendo
docker-compose ps
```

## Ver los logs

```bash
# Ver logs de todos los servicios
docker-compose logs

# Seguir logs de un servicio especifico en vivo
docker-compose logs -f app

# Ver solo los ultimos 50 logs de la base de datos
docker-compose logs --tail=50 db
```

## Detener todo

```bash
# Detener y eliminar contenedores (los datos del volumen se mantienen)
docker-compose down

# Detener y eliminar TODO, incluyendo volumenes (CUIDADO: borra datos)
docker-compose down -v
```

## Reconstruir después de cambios en el código

Si cambian código Java y quieren que se refleje en el contenedor:

```bash
# Reconstruir y levantar
docker-compose up --build -d
```

## Tabla de referencia

```
╔═══════════════════════════════════════════════════════════════╗
║              COMANDOS DOCKER COMPOSE — REFERENCIA             ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  docker-compose up              → levantar todo (foreground)  ║
║  docker-compose up -d           → levantar en background      ║
║  docker-compose up --build -d   → reconstruir + levantar      ║
║  docker-compose ps              → estado de los servicios     ║
║  docker-compose logs -f app     → seguir logs de "app"        ║
║  docker-compose down            → bajar todo                  ║
║  docker-compose down -v         → bajar todo + borrar datos   ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```

---

# PARTE IV — REDES, VOLÚMENES Y ADMINER

# 5. Redes en Docker Compose

Docker Compose crea una **red interna** automáticamente. Todos los servicios definidos en el mismo `docker-compose.yml` están en la misma red y pueden comunicarse por nombre.

```
╔══════════════════════════════════════════════════════════════╗
║              RED INTERNA DE DOCKER COMPOSE                    ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  ┌─────────────── Red: pizzeria_default ───────────────┐     ║
║  │                                                     │     ║
║  │  ┌─────────┐    ┌──────────┐    ┌──────────┐       │     ║
║  │  │   app   │───→│    db    │    │ adminer  │       │     ║
║  │  │ :8080   │    │  :5432   │←───│  :8080   │       │     ║
║  │  └─────────┘    └──────────┘    └──────────┘       │     ║
║  │                                                     │     ║
║  └─────────────────────────────────────────────────────┘     ║
║                                                              ║
║  "app" se conecta a "db" usando jdbc:postgresql://db:5432    ║
║  "adminer" se conecta a "db" usando el hostname "db"         ║
║                                                              ║
║  Desde SU MÁQUINA:                                           ║
║  - app:     http://localhost:8080                             ║
║  - adminer: http://localhost:9090                             ║
║  - db:      localhost:5432 (con pgAdmin, DBeaver, etc.)      ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

Por eso la URL JDBC usa `db` y no `localhost`: dentro de la red de Docker, el servicio PostgreSQL se llama `db` (el nombre que le pusieron en el `docker-compose.yml`).

---

# 6. Volúmenes: Persistencia de Datos

## Sin volumen: los datos desaparecen

```bash
# Si hacen esto, TODOS los datos de PostgreSQL se borran:
docker-compose down
docker-compose up -d
# → La base de datos esta vacia de nuevo
```

Es como usar un `ArrayList` en vez de una base de datos — al reiniciar, todo se pierde.

## Con volumen: los datos sobreviven

En nuestro `docker-compose.yml` definimos:

```yaml
    volumes:
      - postgres_data:/var/lib/postgresql/data
```

Esto le dice a Docker: "guarda el contenido de `/var/lib/postgresql/data` (donde PostgreSQL almacena sus archivos) en un volumen llamado `postgres_data`". Ese volumen vive fuera del contenedor.

```bash
# Con volumen, los datos sobreviven:
docker-compose down       # detiene todo, pero el volumen sigue
docker-compose up -d      # los datos siguen ahi

# Solo si hacen esto se borran:
docker-compose down -v    # la opcion -v borra los volumenes
```

## Inspeccionar volúmenes

```bash
# Listar volumenes
docker volume ls

# Ver detalles de un volumen
docker volume inspect pizzeria_postgres_data
```

---

# 7. Adminer: Ver la Base de Datos

Adminer es un panel web ligero (un solo archivo PHP) para administrar bases de datos. Lo levantamos como parte de Docker Compose.

## Acceder a Adminer

Con los contenedores corriendo, abran el navegador en:

```
http://localhost:9090
```

## Datos de conexión

| Campo | Valor |
|-------|-------|
| **Sistema** | PostgreSQL |
| **Servidor** | db |
| **Usuario** | postgres |
| **Contraseña** | secret |
| **Base de datos** | pizzeria |

> El servidor es `db`, no `localhost`, porque Adminer está dentro de la red de Docker y se conecta al servicio por su nombre.

Una vez dentro, van a ver las tablas que Hibernate creó automáticamente a partir de sus entidades `@Entity`. Pueden navegar los datos, ejecutar queries SQL y verificar que todo funciona.

---

# 8. Variables de Entorno y Spring Boot

Spring Boot tiene una característica muy útil: lee variables de entorno y las convierte a propiedades automáticamente.

```
╔══════════════════════════════════════════════════════════════╗
║      VARIABLES DE ENTORNO → PROPIEDADES DE SPRING BOOT       ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Variable de entorno            →  Propiedad de Spring       ║
║  ─────────────────────          ────────────────────────     ║
║  SPRING_DATASOURCE_URL          →  spring.datasource.url     ║
║  SPRING_DATASOURCE_USERNAME     →  spring.datasource.username║
║  SPRING_DATASOURCE_PASSWORD     →  spring.datasource.password║
║  SERVER_PORT                    →  server.port                ║
║                                                              ║
║  REGLA DE CONVERSIÓN:                                        ║
║  1. Puntos (.) → guiones bajos (_)                           ║
║  2. Minúsculas → MAYÚSCULAS                                  ║
║                                                              ║
║  spring.datasource.url  →  SPRING_DATASOURCE_URL             ║
║                                                              ║
║  Las variables de entorno tienen PRIORIDAD sobre              ║
║  application.properties. Así pueden tener un properties      ║
║  para desarrollo y otro comportamiento en Docker.            ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

En nuestro `docker-compose.yml`, las variables `SPRING_DATASOURCE_*` sobreescriben lo que diga `application.properties`. Esto es muy útil: pueden tener `application.properties` apuntando a `localhost` para desarrollar desde IntelliJ, y Docker Compose lo sobreescribe con `db` cuando corre en contenedor.

---

# PARTE V — PRÁCTICA

# 9. Ejercicio: Dockerizar su Proyecto Personal con Compose

Apliquen todo lo anterior a su proyecto personal. Sigan estos pasos en orden:

**Paso 1: Dockerfile**

Si no lo crearon ayer, creen el `Dockerfile` (multi-stage) en la raíz del proyecto. Usen el modelo de la sección 5 del día 15.

**Paso 2: Agregar PostgreSQL al pom.xml**

```xml
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
```

**Paso 3: Actualizar application.properties**

```properties
spring.datasource.url=jdbc:postgresql://db:5432/miproyecto
spring.datasource.username=postgres
spring.datasource.password=secret
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> Cambien `miproyecto` por el nombre de su base de datos.

**Paso 4: Crear docker-compose.yml**

Usen el modelo de la sección 3. Adapten:

- Nombre del servicio `app` (pueden dejarlo como está)
- Nombre de la base de datos en `POSTGRES_DB`
- La URL en `SPRING_DATASOURCE_URL` debe coincidir

**Paso 5: Levantar todo**

```bash
docker-compose up --build -d
```

**Paso 6: Verificar**

1. `docker-compose ps` — tres servicios corriendo
2. `http://localhost:8080` — su API responde
3. `http://localhost:9090` — Adminer muestra las tablas
4. Prueben los endpoints con Postman

**Paso 7: Verificar persistencia**

1. Creen datos con POST desde Postman
2. `docker-compose down` (sin -v)
3. `docker-compose up -d`
4. Los datos siguen ahí

---

# PARTE VI — KUBERNETES Y RESUMEN

# 10. Kubernetes: Mención Breve

Docker Compose resuelve el problema de orquestar varios contenedores en **una sola máquina**. ¿Pero qué pasa cuando necesitan 100 contenedores en 10 servidores?

```
╔══════════════════════════════════════════════════════════════╗
║           DOCKER COMPOSE vs KUBERNETES                        ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  DOCKER COMPOSE:                                             ║
║  - Una sola máquina                                          ║
║  - Perfecto para desarrollo y proyectos pequeños             ║
║  - Archivo docker-compose.yml                                ║
║  - Comandos: docker-compose up/down                          ║
║                                                              ║
║  KUBERNETES (K8s):                                           ║
║  - Múltiples máquinas (clúster)                              ║
║  - Para producción a gran escala                             ║
║  - Auto-healing: si un contenedor muere, lo recrea           ║
║  - Auto-scaling: más tráfico → más contenedores              ║
║  - Load balancing: distribuye tráfico entre réplicas         ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  CONCEPTOS BÁSICOS DE KUBERNETES:                            ║
║                                                              ║
║  Pod           = grupo de contenedores (mínima unidad)       ║
║  Deployment    = define cuántas réplicas de un Pod quieren   ║
║  Service       = punto de acceso estable al Deployment       ║
║  Namespace     = separación lógica (dev, staging, prod)      ║
║                                                              ║
║  No vamos a practicar K8s en este curso, pero ahora          ║
║  saben que existe y para qué sirve.                          ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

Lo importante: si les preguntan en una entrevista, pueden decir que conocen Docker y Docker Compose para desarrollo, y que saben que Kubernetes es la solución para orquestación a escala en producción.

---

# 11. Resumen: Comandos Docker Compose

```
╔═══════════════════════════════════════════════════════════════╗
║            COMANDOS DEL DÍA 16 — DOCKER COMPOSE              ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  LEVANTAR:                                                    ║
║  docker-compose up              → foreground (ver logs)       ║
║  docker-compose up -d           → background                  ║
║  docker-compose up --build -d   → reconstruir + levantar      ║
║                                                               ║
║  MONITOREAR:                                                  ║
║  docker-compose ps              → estado de servicios         ║
║  docker-compose logs            → logs de todos                ║
║  docker-compose logs -f app     → seguir logs de "app"        ║
║                                                               ║
║  DETENER:                                                     ║
║  docker-compose down            → bajar todo (datos quedan)   ║
║  docker-compose down -v         → bajar todo + borrar datos   ║
║                                                               ║
║  VOLÚMENES:                                                   ║
║  docker volume ls               → listar volumenes            ║
║  docker volume inspect <nombre> → detalles del volumen        ║
║                                                               ║
║  ARCHIVOS CLAVE:                                              ║
║  Dockerfile                     → receta de la imagen         ║
║  docker-compose.yml             → orquestacion de servicios   ║
║  application.properties         → configuracion Spring Boot   ║
║  pom.xml                        → dependencia postgresql      ║
║                                                               ║
║  URLS DE ACCESO:                                              ║
║  http://localhost:8080           → su aplicacion               ║
║  http://localhost:9090           → Adminer (ver BD)            ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```
