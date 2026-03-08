# Índice del Curso IFCD0014 — Spring e Hibernate

## Material del Alumno

Cada archivo es el manual de referencia para un día de clase. Síganlos en orden.

---

### Semana 2: Maven y Streams (Días 6-8)

| Día | Archivo | Contenido |
|-----|---------|-----------|
| 6 | `DIA_06_MANUAL_MAVEN_STREAMS.md` | Maven: instalación, `pom.xml`, primer proyecto. Streams: pipelines, `filter`, `map`, `collect`. 3 ejercicios con la Pizzería. GPS Arquitectónico |
| 7 | `DIA_07_MANUAL_MAVEN_PIZZERIA.md` | Convertir Pizzería a Maven. Dependencia Gson. Exportar pedidos a JSON. Puente hacia Hibernate |
| 8 | `DIA_08_MANUAL_MAVEN_AVANZADO.md` | Scopes de dependencias. Ciclo de vida Maven. Plugins (compiler, surefire, jar). Perfiles (dev/prod). Tests con JUnit 5 |

### Semana 2: Hibernate (Días 9-10)

| Día | Archivo | Contenido |
|-----|---------|-----------|
| 9 | `DIA_09_MANUAL_HIBERNATE_INTRO.md` | ORM: qué es y por qué existe. JPA vs Hibernate. `persistence.xml`. Primera `@Entity`. CRUD con `EntityManager`. JPQL |
| 10 | `DIA_10_MANUAL_PIZZERIA_HIBERNATE.md` | Hibernate en la Pizzería. Pizza/Cliente/Pedido como `@Entity`. Relaciones `@ManyToOne`, `@ManyToMany`, `@JoinTable`. Repositorios con `EntityManager` |

### Semana 3: Spring Boot (Días 11-13)

| Día | Archivo | Contenido |
|-----|---------|-----------|
| 11 | `DIA_11_MANUAL_SPRING_BOOT_INTRO.md` | IoC y DI. Spring Initializr. Lombok (`@Data`, `@Builder`). `application.properties`. `JpaRepository`. Primer `@RestController`. DevTools |
| 12 | `DIA_12_MANUAL_PIZZERIA_SPRING.md` | Pizzería como API REST con Spring Boot. 3 entidades con relaciones. Repositorios, servicios, controladores. `data.sql`. Postman |
| 13 | `DIA_13_MANUAL_PROYECTO_PERSONAL.md` | Elegir dominio propio. Diseñar entidades. Crear proyecto Spring Boot. Implementar primer CRUD |

### Semana 3-4: Proyecto Personal (Día 14)

| Día | Archivo | Contenido |
|-----|---------|-----------|
| 14 | `DIA_14_MANUAL_PROYECTO_COMPLETO.md` | Completar todos los servicios y controladores. Validación con `@Valid`. `GlobalExceptionHandler`. Colección Postman. Subir a GitHub |

### Semana 4: Docker y CI/CD (Días 15-18)

| Día | Archivo | Contenido |
|-----|---------|-----------|
| 15 | `DIA_15_MANUAL_DOCKER_INTRO.md` | Contenedores vs VMs. Docker Desktop. Imágenes y contenedores. `Dockerfile` multi-stage. `docker build` y `docker run` |
| 16 | `DIA_16_MANUAL_DOCKER_COMPOSE.md` | Docker Compose. Migrar de H2 a PostgreSQL. `docker-compose.yml` con app + BD + Adminer. Volúmenes y redes |
| 17 | `DIA_17_MANUAL_CICD_GITHUB_ACTIONS.md` | CI/CD conceptos. GitHub Actions: workflows, `build.yml`. Compilar, testear, empaquetar en cada push. Jenkins como referencia |
| 18 | `DIA_18_MANUAL_PROYECTO_DOCKER.md` | Dockerizar proyecto personal. Compose + PostgreSQL + CI/CD. README profesional con badge |

### Semana 5: Presentaciones (Días 19-20)

| Día | Archivo | Contenido |
|-----|---------|-----------|
| 19 | `DIA_19_MANUAL_PREPARAR_DEMO.md` | Checklist técnico. Template de README. Estructura de presentación (5-7 min). Troubleshooting de último minuto |
| 20 | `DIA_20_MANUAL_DEMO_DAY.md` | Formato de presentaciones. Evaluación entre pares. Cierre del curso. Próximos pasos y recursos |

---

## Evolución del Proyecto Pizzería

```
v1 (Día 3)  → Java puro, compilación con javac
v2 (Día 4)  → Paquetes, interfaces, capas
v3 (Día 5)  → Cliente, Combo, CategoríaCliente
v4 (Día 7)  → Maven + Gson (exportar JSON)
v5 (Día 10) → Hibernate + H2 (persistencia)
v6 (Día 12) → Spring Boot + REST API
```

## Patrón Arquitectónico

```
HTTP Request → Controller → Service → Repository → Database
(Postman)     (@RestController) (@Service) (JpaRepository) (PostgreSQL)
```

---

> *Curso IFCD0014 — Desarrollo de aplicaciones con Spring e Hibernate*
