# Trabajo Final: API REST con Spring Boot + Docker

**Curso:** Spring Boot + Hibernate — Prof. Juan Marcelo Gutierrez Miranda (@TodoEconometria)

---

## Objetivo

Construir **desde cero** una aplicacion web con API REST usando Spring Boot, Hibernate
y Docker. A partir de uno de los 16 blueprints (o un dominio propio), disenar
entidades con relaciones, implementar CRUD completo, y desplegarlo en contenedores.

**Lo que se evalua:** No solo el codigo, sino tu **proceso de aprendizaje**.
Pueden usar herramientas de IA pero deben documentar como las usaron y que aprendieron.

---

## Dominio del Proyecto

Cada alumno elige **uno** de los 16 blueprints en [`blueprints/`](../blueprints/README.md),
o propone un dominio propio (con aprobacion del profesor).

**Importante:** Cada alumno elige un blueprint diferente.

---

## Estructura: 4 Bloques

### Bloque A: Infraestructura Docker (30%)

Escribir un `docker-compose.yml` que levante:

| Servicio | Requisito |
|----------|-----------|
| PostgreSQL 16 | Base de datos para la app |
| Adminer | Interfaz web para ver la BD (puerto 8081) |
| App Spring Boot | Contenedor con la app (Dockerfile, puerto 8080) |

**Entregables:** `docker-compose.yml` + `Dockerfile` + `02_INFRAESTRUCTURA.md`

---

### Bloque B: API REST Spring Boot (25%)

| Requisito | Detalle |
|-----------|---------|
| Entidades | Minimo 3 entidades JPA con Lombok |
| Relaciones | Al menos una `@ManyToOne` y una `@ManyToMany` |
| Repositorios | `JpaRepository` con al menos 2 derived queries |
| Servicios | `@Service` con logica de negocio |
| Controladores | `@RestController` con CRUD completo |

**Entregables:** codigo fuente en `src/` + `pom.xml`

---

### Bloque C: Funcionalidad Avanzada (25%)

Elegir UNA opcion:

| Opcion | Que hacer |
|--------|-----------|
| **Validacion** | Bean Validation + `@ControllerAdvice` para errores globales |
| **Queries avanzadas** | `@Query` JPQL + paginacion con `Pageable` + filtros |
| **Testing** | JUnit 5 + Mockito para servicios + tests de integracion |

**Entregables:** codigo + capturas Postman/tests + `03_RESULTADOS.md`

---

### Bloque D: Reflexion IA (20%)

Para cada bloque (A, B, C), documentar 3 momentos:

1. **Momento Arranque** — Que fue lo primero que buscaron o le pidieron a la IA
2. **Momento Error** — Que fallo y como lo resolvieron
3. **Momento Aprendizaje** — Que aprendieron que NO sabian antes

**Entregable:** `04_REFLEXION_IA.md`

---

## Estructura de Entrega

```
entregas/trabajo_final/apellido_nombre/
├── PROMPTS.md                 # Lo MAS importante
├── 01_README.md               # Dominio + entidades + relaciones
├── 02_INFRAESTRUCTURA.md      # Explicacion Docker
├── 03_RESULTADOS.md           # Capturas Postman + explicacion
├── 04_REFLEXION_IA.md         # 3 Momentos x 3 bloques
├── 05_RESPUESTAS.md           # 4 preguntas de comprension
├── docker-compose.yml
├── Dockerfile
├── src/                       # Codigo fuente
├── pom.xml
└── .gitignore
```

La plantilla esta en [`plantilla/`](plantilla/).

---

## Preguntas de Comprension (05_RESPUESTAS.md)

1. **Infraestructura:** Si tu app necesita conectarse a PostgreSQL pero el contenedor de la BD tarda 30 segundos en arrancar, que pasa? Como lo solucionarias?
2. **JPA:** Por que `findAll()` puede ser lento con 100,000 registros? Que alternativa usarias?
3. **API REST:** Diferencia entre error 400 y error 500. Un ejemplo de cada uno en tu proyecto.
4. **Escalabilidad:** Si tu app pasa de 100 a 10,000 usuarios, que cambiarias en Docker?

---

## Criterios de Evaluacion

| Bloque | Peso | Que se evalua |
|--------|------|---------------|
| A. Docker | 30% | docker-compose.yml funcional, Dockerfile, explicacion |
| B. API REST | 25% | Entidades con relaciones, CRUD, codigo limpio |
| C. Avanzado | 25% | Implementacion correcta de la opcion elegida |
| D. Reflexion | 20% | Honestidad, profundidad, aprendizaje real |

---

## Calendario

| Dia | Que hacer |
|-----|-----------|
| 13 | Elegir blueprint, disenar entidades, empezar Bloque B |
| 14 | Completar API REST |
| 15-16 | Docker (Bloque A) |
| 17 | CI/CD + Bloque C |
| 18 | Integrar todo |
| 19 | Preparar presentacion |
| 20 | Demo Day |
