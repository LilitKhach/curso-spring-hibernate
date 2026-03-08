# Día 20 — Demo Day: Presentaciones y cierre del curso

## El día de hoy

Cada uno (o cada pareja) va a presentar su proyecto personal. Es el cierre del curso IFCD0014.

---

## 1. Formato de las presentaciones

- **Tiempo por persona/equipo:** 5-7 minutos de presentación + 3 minutos de preguntas
- **Orden:** se sortea al inicio de la clase
- **Qué mostrar:**
  1. La aplicación funcionando (demo en vivo o capturas)
  2. El código: al menos una entidad, un servicio y un controlador
  3. Docker: el `docker-compose.yml` y la app corriendo en contenedor
  4. CI/CD: el pipeline en GitHub Actions
  5. Aprendizajes personales

---

## 2. Ficha de evaluación entre pares

Mientras cada compañero presenta, evalúen su trabajo:

| Criterio | 1 | 2 | 3 | 4 | 5 |
|----------|---|---|---|---|---|
| **Funcionalidad:** ¿La app funciona? ¿Los endpoints responden? | | | | | |
| **Arquitectura:** ¿Tiene capas bien separadas (Controller/Service/Repository)? | | | | | |
| **Código limpio:** ¿Se entiende el código? ¿Usa Lombok, validaciones? | | | | | |
| **Docker:** ¿Corre en contenedor? ¿Tiene Compose? | | | | | |
| **Presentación:** ¿Explicó bien? ¿Mostró el "por qué"? | | | | | |

**Nombre del presentador:** _______________

**Lo mejor del proyecto:** _______________

**Un consejo constructivo:** _______________

---

## 3. Horario del día

| Hora | Actividad |
|------|-----------|
| 9:00 - 9:15 | Sorteo del orden + últimos preparativos |
| 9:15 - 11:00 | Ronda 1 de presentaciones (~7 alumnos) |
| 11:00 - 11:30 | Descanso |
| 11:30 - 13:00 | Ronda 2 de presentaciones (~8 alumnos) |
| 13:00 - 13:30 | Votación "Mejor proyecto" + feedback general |
| 13:30 - 14:00 | Cierre del curso |

---

## 4. Después del curso: ¿Qué sigue?

### Lo que ya saben hacer

Al terminar hoy, ustedes son capaces de:

- Crear proyectos Java con **Maven** (dependencias, plugins, perfiles, tests)
- Mapear objetos a base de datos con **Hibernate/JPA** (entidades, relaciones, CRUD)
- Construir **APIs REST** con **Spring Boot** (controladores, servicios, repositorios)
- Validar datos y manejar errores de forma profesional
- Empaquetar aplicaciones con **Docker** y orquestar con **Docker Compose**
- Automatizar builds y tests con **GitHub Actions** (CI/CD)
- Trabajar con **Git** en un proyecto real

Eso no es poco. Es la base de lo que hace un backend developer en una empresa.

### Próximos pasos recomendados

**Si les gustó el backend:**
- Spring Security + JWT (autenticación y autorización)
- Microservicios con Spring Cloud
- Mensajería asíncrona con RabbitMQ o Kafka
- Caché con Redis

**Si les gustó DevOps:**
- Kubernetes (orquestación de contenedores a escala)
- Terraform (infraestructura como código)
- AWS / Azure / GCP (proveedores cloud)
- Monitorización con Prometheus + Grafana

**Si quieren hacer fullstack:**
- Frontend con React, Angular o Vue.js
- Conectar el frontend a su API REST
- Desplegar todo junto con Docker Compose

**Para todos:**
- Hacer su proyecto personal más grande (añadir autenticación, frontend, más entidades)
- Contribuir a un proyecto open source en GitHub
- Preparar el proyecto para entrevistas técnicas (es su mejor carta de presentación)

### Recursos gratuitos

| Recurso | URL | Para qué |
|---------|-----|----------|
| Spring Guides | spring.io/guides | Tutoriales oficiales paso a paso |
| Baeldung | baeldung.com | Spring y Java en profundidad |
| Docker Docs | docs.docker.com/get-started | Docker desde cero |
| GitHub Actions | docs.github.com/en/actions | Workflows y pipelines |
| Java Brains (YouTube) | youtube.com/@Java.Brains | Videos de Spring Boot |

---

## 5. Resumen del viaje

| Fase | Días | Qué aprendimos | Proyecto |
|------|------|-----------------|----------|
| Intro Java | 1-5 | Java, POO, packages, compilación manual | Pizzería v1-v3 |
| Maven | 6-8 | Maven, Streams, dependencias, plugins, tests | Pizzería v4 |
| Hibernate | 9-10 | ORM, JPA, entidades, relaciones, H2 | Pizzería v5 |
| Spring Boot | 11-12 | IoC, DI, Spring Data, REST API, Lombok | Pizzería v6 |
| Proyecto personal | 13-14 | Aplicar todo en un dominio propio | Proyecto propio |
| Docker | 15-16 | Contenedores, Dockerfile, Compose, PostgreSQL | Proyecto dockerizado |
| CI/CD | 17 | GitHub Actions, pipelines, automatización | Pipeline verde |
| Integración | 18 | Docker + CI/CD en el proyecto personal | Todo integrado |
| Cierre | 19-20 | Pulir, presentar, compartir | Demo Day |

---

## 6. Una última cosa

```
Día 1:  javac PizzeriaApp.java
Día 20: docker-compose up + GitHub Actions + API REST + PostgreSQL
```

Empezaron compilando con `javac`. Terminan desplegando APIs REST en contenedores Docker con CI/CD automático.

**El proyecto que construyeron es suyo.** Está en su GitHub. Pónganlo en su CV, úsenlo en entrevistas, sigan construyendo sobre él. Es la mejor carta de presentación que pueden tener.

---

> *Curso IFCD0014 — Desarrollo de aplicaciones con Spring e Hibernate*
