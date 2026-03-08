# Día 19 — Pulir proyecto y preparar presentación

## Objetivo del día

Hoy es el penúltimo día. El objetivo es dejar su proyecto personal **terminado** y preparar una presentación de 5-7 minutos para el Demo Day de mañana.

---

## 1. Checklist técnico del proyecto

Antes de preparar la presentación, verifiquen que su proyecto cumple con TODO lo siguiente.

### Código y arquitectura

- [ ] Proyecto Maven con `pom.xml` limpio
- [ ] Al menos 3 entidades JPA con relaciones (`@ManyToOne`, `@OneToMany`, `@ManyToMany`)
- [ ] Repositorios que extienden `JpaRepository`
- [ ] Servicios con `@Service` y lógica de negocio
- [ ] Controladores REST con `@RestController`
- [ ] Al menos 5 endpoints funcionales (CRUD completo de una entidad)
- [ ] Validaciones con `@Valid`, `@NotNull`, `@Size`
- [ ] Manejo de errores (`@ControllerAdvice`)

### Docker

- [ ] `Dockerfile` funcional (multi-stage build)
- [ ] `docker-compose.yml` con app + base de datos
- [ ] Verificar: `docker-compose up` levanta todo sin errores
- [ ] La app se conecta a la BD dentro de Docker

### CI/CD

- [ ] Repositorio en GitHub (público o privado)
- [ ] `.github/workflows/build.yml` funcional
- [ ] El pipeline pasa (check verde en GitHub)

### Documentación

- [ ] `README.md` completo (ver template en sección 2)

---

## 2. Template para README.md

Copien este template y adáptenlo a su proyecto:

```markdown
# [Nombre del Proyecto]

## Descripción

[1-2 párrafos describiendo qué hace la aplicación y por qué la eligieron]

## Tecnologías

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- H2 / PostgreSQL
- Docker & Docker Compose
- GitHub Actions (CI/CD)
- Maven
- Lombok

## Cómo ejecutar

### Opción 1: Sin Docker

​```bash
mvn spring-boot:run
​```

La app estará disponible en: http://localhost:8080

### Opción 2: Con Docker

​```bash
docker-compose up
​```

## Endpoints principales

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/[entidad] | Listar todos |
| GET | /api/[entidad]/{id} | Obtener por ID |
| POST | /api/[entidad] | Crear nuevo |
| PUT | /api/[entidad]/{id} | Actualizar |
| DELETE | /api/[entidad]/{id} | Eliminar |

## Arquitectura

​```
HTTP Request → Controller → Service → Repository → Database
(Postman)     (@RestController) (@Service) (JpaRepository) (PostgreSQL)
​```

## Autor

[Tu nombre]
```

> **Nota:** Reemplacen `[entidad]` por sus entidades reales y añadan los endpoints custom que tengan.

---

## 3. Preparar la presentación (5-7 minutos)

### Estructura recomendada

**Minuto 1 — Introducción:**
- ¿Qué problema resuelve su aplicación?
- ¿Por qué eligieron ese dominio?

**Minutos 2-3 — Demo en vivo:**
- Levantar la app (Docker o local)
- Mostrar 2-3 endpoints en Postman
- Crear un registro, leerlo, actualizarlo

**Minuto 4 — Arquitectura:**
- Mostrar estructura de carpetas en IntelliJ
- Explicar las capas: Controller → Service → Repository
- Mostrar una entidad con sus anotaciones JPA

**Minuto 5 — DevOps:**
- Mostrar el `Dockerfile`
- Mostrar `docker-compose.yml`
- Mostrar el pipeline en GitHub Actions (el check verde)

**Minutos 6-7 — Aprendizajes y preguntas:**
- ¿Qué fue lo más difícil?
- ¿Qué aprendieron que no sabían antes?
- Preguntas del público

### Tips para la presentación

- **No lean de una diapositiva.** Muestren código y la app funcionando.
- Si algo falla en la demo, tengan capturas de pantalla de respaldo.
- Hablen del "por qué" de las decisiones, no solo del "qué".
- Si trabajaron en parejas, ambos deben hablar.
- Practiquen al menos una vez: cronómetren los 7 minutos.

---

## 4. Errores comunes de último minuto

### Docker Compose no levanta

```bash
# Verificar que no hay contenedores ocupando puertos
docker ps
docker stop [container_id]

# Si todo falla, limpiar y reintentar
docker-compose down -v
docker-compose up --build
```

### La app no conecta a la BD en Docker

Revisen `application.properties`. Dentro de Docker, el host de la BD **NO** es `localhost`, es el nombre del servicio:

```properties
# MAL (dentro de Docker)
spring.datasource.url=jdbc:postgresql://localhost:5432/miproyecto

# BIEN (dentro de Docker)
spring.datasource.url=jdbc:postgresql://db:5432/miproyecto
```

### El pipeline de GitHub falla

Revisen el log en GitHub → Actions → click en el workflow fallido.

Errores más comunes:
- **Tests que fallan:** arreglen el test o el código
- **Java version incorrecta:** verifiquen que el workflow usa la misma versión que su `pom.xml`
- **Out of memory:** añadan `-Xmx512m` en las opciones de Maven

### JSON con recursión infinita

Si al hacer GET de una entidad con relaciones el servidor se cuelga o devuelve un error de stack overflow:

```java
// En el lado "hijo" de la relacion, añadan:
@ManyToOne
@JsonBackReference
private Cliente cliente;

// En el lado "padre":
@OneToMany(mappedBy = "cliente")
@JsonManagedReference
private List<Pedido> pedidos;
```

---

## 5. Horario del día

| Hora | Actividad |
|------|-----------|
| 9:00 - 10:30 | Completar checklist técnico (arreglar lo que falte) |
| 10:30 - 11:00 | Escribir README.md |
| 11:00 - 11:30 | Descanso |
| 11:30 - 12:30 | Preparar estructura de la presentación |
| 12:30 - 13:30 | Ensayar en parejas (uno presenta, el otro da feedback) |
| 13:30 - 14:00 | Commit final + push + verificar pipeline verde |

---

## 6. Antes de irse hoy

Verifiquen estos 3 puntos antes de salir:

1. **`docker-compose up` funciona** — su app levanta con la BD
2. **GitHub tiene el código actualizado** — último push hecho
3. **Saben qué van a decir mañana** — tienen la estructura clara

> **Mañana es el Demo Day. Vengan preparados, con todo funcionando, y disfruten mostrando lo que han construido en estas 4 semanas.**
