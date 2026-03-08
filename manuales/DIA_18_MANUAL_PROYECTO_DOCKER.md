# Día 18: Proyecto Personal — Docker + CI/CD + Integración Final

Hoy su proyecto recibe el tratamiento profesional completo. Van a integrar todo lo que aprendieron esta semana: Docker, Docker Compose, PostgreSQL y GitHub Actions.

Prof. Juan Marcelo Gutierrez Miranda

**Curso IFCD0014 — Semana 4, Día 18 (Jueves)**
**Objetivo:** Dockerizar el proyecto personal con Compose (app + PostgreSQL + Adminer), configurar el pipeline CI/CD con GitHub Actions y dejar el README profesional.

> Este es un día de trabajo práctico. Usen los manuales de los días 15, 16 y 17 como referencia.

---

# 1. Objetivo del Día

Al terminar hoy, su proyecto va a tener:

- Un `Dockerfile` que empaqueta la app en una imagen Docker
- Un `docker-compose.yml` que levanta app + PostgreSQL + Adminer
- Un pipeline de GitHub Actions que compila y testea en cada push
- Un README profesional con badge, descripción y cómo ejecutar

Esto es exactamente lo que un equipo profesional tiene en sus proyectos.

---

# 2. Checklist de Inicio: ¿Dónde Está Cada Uno?

Antes de empezar, verifiquen que tienen lo esencial. Marquen cada punto:

```
╔══════════════════════════════════════════════════════════════╗
║              CHECKLIST DE INICIO                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  [ ] Spring Boot app funcional con 3+ entidades              ║
║  [ ] Endpoints REST probados con Postman                     ║
║  [ ] Validación (@Valid, @NotNull, etc.)                     ║
║  [ ] Manejo de errores (@ControllerAdvice)                   ║
║  [ ] Repositorio en GitHub (con push reciente)               ║
║  [ ] mvn clean package funciona sin errores                  ║
║                                                              ║
║  Si les falta algo: arréglenlo PRIMERO.                      ║
║  No tiene sentido dockerizar algo que no compila.            ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# PARTE I — DOCKERFILE

# 3. Tarea 1: Dockerfile

## Paso 1: Crear el Dockerfile

En la raíz del proyecto (al lado del `pom.xml`), creen un archivo llamado `Dockerfile`:

```dockerfile
# ============================================================
# Etapa 1: Compilar con Maven
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ============================================================
# Etapa 2: Ejecutar con JRE ligero
# ============================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> Si su proyecto usa un `artifactId` específico en el `pom.xml`, el `*.jar` en el COPY va a tomar el archivo correcto automáticamente. Si tienen más de un `.jar` en `target/`, usen el nombre exacto.

## Paso 2: Verificar que el build funciona

```bash
docker build -t mi-proyecto:v1 .
```

Si falla, verifiquen que `mvn clean package -DskipTests` funciona localmente primero.

## Paso 3: Probar el contenedor (solo la app, sin BD)

```bash
docker run -p 8080:8080 mi-proyecto:v1
```

> Si su app necesita base de datos para arrancar y falla, es normal. Pasen a la Tarea 2 para agregar PostgreSQL con Compose.

---

# PARTE II — DOCKER COMPOSE

# 4. Tarea 2: Docker Compose

## Paso 1: Agregar PostgreSQL al pom.xml

Dentro de `<dependencies>`:

```xml
        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
```

Pueden mantener H2 para tests:

```xml
        <!-- H2 para tests -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
```

## Paso 2: Actualizar application.properties

```properties
# ============================================================
# Base de datos PostgreSQL
# ============================================================
spring.datasource.url=jdbc:postgresql://db:5432/miproyecto
spring.datasource.username=postgres
spring.datasource.password=secret
spring.datasource.driver-class-name=org.postgresql.Driver

# ============================================================
# JPA / Hibernate
# ============================================================
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> Cambien `miproyecto` por el nombre que quieran darle a su base de datos.

## Paso 3: Crear docker-compose.yml

En la raíz del proyecto:

```yaml
version: '3.8'

services:
  # =====================================================
  # La aplicacion Spring Boot
  # =====================================================
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/miproyecto
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=secret
    depends_on:
      - db

  # =====================================================
  # Base de datos PostgreSQL
  # =====================================================
  db:
    image: postgres:16-alpine
    environment:
      - POSTGRES_DB=miproyecto
      - POSTGRES_PASSWORD=secret
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  # =====================================================
  # Adminer (panel web para ver la BD)
  # =====================================================
  adminer:
    image: adminer
    ports:
      - "9090:8080"

volumes:
  postgres_data:
```

> Cambien `miproyecto` por el nombre de su base de datos en los tres lugares donde aparece: `SPRING_DATASOURCE_URL`, `POSTGRES_DB` y `application.properties`.

## Paso 4: Levantar todo

```bash
docker-compose up --build -d
```

## Paso 5: Verificar

```bash
# Ver que los tres servicios estan corriendo
docker-compose ps

# Ver logs de la app
docker-compose logs -f app
```

Prueben:

| Qué | URL |
|-----|-----|
| Su API | http://localhost:8080 |
| Adminer | http://localhost:9090 |

En Adminer: Sistema = PostgreSQL, Servidor = db, Usuario = postgres, Contraseña = secret, Base de datos = miproyecto.

## Paso 6: Probar con Postman

Abran su colección de Postman y prueben los endpoints contra `http://localhost:8080`. Creen algunos datos con POST y verifiquen que aparecen en Adminer.

---

# PARTE III — GITHUB ACTIONS

# 5. Tarea 3: Pipeline CI/CD

## Paso 1: Crear la carpeta de workflows

```bash
mkdir -p .github/workflows
```

## Paso 2: Crear build.yml

Creen `.github/workflows/build.yml`:

```yaml
name: Build and Test

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout codigo
        uses: actions/checkout@v4

      - name: Configurar Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os }}-maven-

      - name: Compilar
        run: mvn compile

      - name: Ejecutar tests
        run: mvn test

      - name: Empaquetar
        run: mvn package -DskipTests
```

> Si sus tests necesitan base de datos, pueden fallar en el pipeline. Vean la sección de Troubleshooting más adelante.

## Paso 3: Commit y push

```bash
git add .github/workflows/build.yml
git add Dockerfile
git add docker-compose.yml
git commit -m "Add Docker + CI/CD pipeline"
git push
```

## Paso 4: Verificar en GitHub

1. Abrir repositorio en GitHub
2. Pestaña **Actions**
3. Esperar check verde

## Paso 5: Agregar badge al README

Al inicio de su `README.md`:

```markdown
![Build](https://github.com/SU-USUARIO/SU-REPO/actions/workflows/build.yml/badge.svg)
```

```bash
git add README.md
git commit -m "Add CI badge to README"
git push
```

---

# PARTE IV — README PROFESIONAL

# 6. Tarea 4: README Final

Su README debe permitir que cualquier persona entienda y ejecute el proyecto. Usen esta estructura como guía:

```markdown
# Nombre del Proyecto

![Build](https://github.com/USUARIO/REPO/actions/workflows/build.yml/badge.svg)

Descripcion breve del proyecto (1-2 oraciones).

## Tecnologias

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Docker + Docker Compose
- GitHub Actions (CI)

## Como ejecutar

### Con Docker Compose (recomendado)

```bash
docker-compose up --build -d
```

La aplicacion queda disponible en http://localhost:8080

Para ver la base de datos: http://localhost:9090 (Adminer)

### Sin Docker (desarrollo)

```bash
mvn spring-boot:run
```

Requiere Java 17 y PostgreSQL local (o cambiar a H2 en application.properties).

## Endpoints

| Metodo | URL | Descripcion |
|--------|-----|-------------|
| GET | /api/recurso | Listar todos |
| GET | /api/recurso/{id} | Obtener por ID |
| POST | /api/recurso | Crear nuevo |
| PUT | /api/recurso/{id} | Actualizar |
| DELETE | /api/recurso/{id} | Eliminar |

## Arquitectura

```
src/main/java/com/ejemplo/
├── modelo/        ← Entidades JPA
├── repositorio/   ← Interfaces Spring Data
├── servicio/      ← Logica de negocio
├── controlador/   ← REST Controllers
└── excepcion/     ← Manejo de errores
```
```

> Adapten la tabla de endpoints a los de su proyecto. Si tienen más de un recurso, agreguen secciones por cada uno.

---

# PARTE V — TROUBLESHOOTING

# 7. Problemas Comunes de Integración

| Problema | Causa | Solución |
|----------|-------|----------|
| "Connection refused" a la BD | El servicio `db` no arrancó o la app arrancó antes | Verifiquen `depends_on: db` en compose. Agreguen `restart: on-failure` al servicio app. |
| "Port already in use" | Otro contenedor o proceso usa el puerto | `docker-compose down` primero. O cambien el puerto en compose. |
| Tests fallan en GitHub Actions | Los tests necesitan base de datos | Usen H2 para tests (scope `test`) o configuren `application-test.properties` con H2. |
| Docker build falla | El código no compila | Ejecuten `mvn clean package -DskipTests` local primero. |
| Docker build es muy lento | Re-descarga dependencias Maven cada vez | Copien `pom.xml` antes que `src/` en el Dockerfile (ya lo hacen con el multi-stage). |
| Adminer no conecta | Servidor incorrecto | En Adminer, el servidor es `db` (nombre del servicio), no `localhost`. |
| "Dialect not set" | Falta la propiedad del dialecto | Agreguen `spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect`. |
| La app arranca pero no encuentra tablas | `ddl-auto` está en `none` o `validate` | Cambien a `spring.jpa.hibernate.ddl-auto=update`. |

### Tests y base de datos en GitHub Actions

Si sus tests usan `@DataJpaTest` o `@SpringBootTest` y necesitan base de datos, el pipeline va a fallar porque no hay PostgreSQL en el servidor de GitHub Actions.

Solución: crear un archivo `src/test/resources/application-test.properties`:

```properties
# Configuracion para tests (usa H2 en memoria)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

Y anotar las clases de test con `@ActiveProfiles("test")` o agregar al `application.properties` general:

```properties
spring.profiles.active=test
```

> Solo para los tests. En producción (Docker Compose), las variables de entorno sobreescriben todo esto con PostgreSQL.

---

# 8. Horario del Día

| Hora | Actividad |
|------|-----------|
| 9:00 - 10:00 | **Tarea 1:** Dockerfile + verificar build |
| 10:00 - 11:00 | **Tarea 2:** Docker Compose + PostgreSQL + Adminer |
| 11:00 - 11:30 | **Descanso** |
| 11:30 - 12:30 | **Tarea 3:** GitHub Actions + verificar pipeline verde |
| 12:30 - 13:30 | **Tarea 4:** README profesional + pulir detalles |
| 13:30 - 14:00 | **Commit final:** verificar que todo funciona junto |

---

# 9. Checklist de Fin de Día

```
╔══════════════════════════════════════════════════════════════╗
║              CHECKLIST FINAL — DÍA 18                         ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  DOCKER:                                                     ║
║  [ ] Dockerfile funcional (multi-stage)                      ║
║  [ ] docker build completa sin errores                       ║
║                                                              ║
║  DOCKER COMPOSE:                                             ║
║  [ ] docker-compose.yml con: app + db + adminer              ║
║  [ ] docker-compose up levanta todo sin errores              ║
║  [ ] Endpoints responden contra la app dockerizada           ║
║  [ ] Adminer muestra las tablas correctamente                ║
║  [ ] Los datos persisten después de docker-compose down/up   ║
║                                                              ║
║  CI/CD:                                                      ║
║  [ ] .github/workflows/build.yml existe                      ║
║  [ ] Pipeline verde en GitHub Actions                        ║
║  [ ] Badge visible en README                                 ║
║                                                              ║
║  PROYECTO:                                                   ║
║  [ ] README actualizado con descripción y cómo ejecutar      ║
║  [ ] Todo commiteado y pusheado a GitHub                     ║
║  [ ] El proyecto compila, los tests pasan, Docker funciona   ║
║                                                              ║
║  Si todo está marcado: tienen un proyecto profesional.       ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# Resumen: Lo que Lograron Esta Semana

```
╔══════════════════════════════════════════════════════════════╗
║              SEMANA 4 COMPLETA — DEVOPS BÁSICO                ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  DÍA 15: Docker                                              ║
║  ─────────────────                                           ║
║  Qué es Docker, comandos básicos, Dockerfile,                ║
║  multi-stage build, ejecutar Spring Boot en contenedor.      ║
║                                                              ║
║  DÍA 16: Docker Compose                                      ║
║  ──────────────────────                                      ║
║  Múltiples contenedores, PostgreSQL, Adminer,                ║
║  redes internas, volúmenes, persistencia.                    ║
║                                                              ║
║  DÍA 17: CI/CD + GitHub Actions                              ║
║  ────────────────────────────                                ║
║  Pipeline automatizado, build + test en cada push,           ║
║  badges, Jenkins como referencia.                            ║
║                                                              ║
║  DÍA 18: Integración (hoy)                                   ║
║  ──────────────────────────                                  ║
║  Todo junto en su proyecto personal.                         ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  FLUJO PROFESIONAL:                                          ║
║                                                              ║
║  Código → Git → GitHub → GitHub Actions → Docker → Deploy    ║
║                                                              ║
║  Esto es DevOps básico. Y ya lo saben hacer.                 ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```
