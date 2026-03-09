# Spring Boot + Hibernate — De Cero a Produccion‌‌‌​‌​‌‌​﻿‍‍‌‍‌​​﻿‌‌‌‍​﻿‌‍​﻿‌‍​‍‌‍​‍‌‍‌‌​﻿‍‌​﻿‌﻿​﻿​‍​﻿‍​‌‍​‍‌‍‌‍​﻿‍‌​﻿‌‍‌‍​‍

<div align="center">

<img src="https://raw.githubusercontent.com/TodoEconometria/ejercicios-bigdata/main/docs/assets/todoeconometria_logo.png" alt="TodoEconometria" width="350">

*"Sin experiencia no hay conocimiento"*

## CURSO COMPLETO DE SPRING BOOT + HIBERNATE

[![Web del Curso](https://img.shields.io/badge/Web-del%20Curso-green?style=for-the-badge&logo=github-pages&logoColor=white)](https://todoeconometria.github.io/curso-spring-hibernate/)
[![GitHub Stars](https://img.shields.io/github/stars/TodoEconometria/curso-spring-hibernate?style=for-the-badge&logo=github)](https://github.com/TodoEconometria/curso-spring-hibernate/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/TodoEconometria/curso-spring-hibernate?style=for-the-badge&logo=github)](https://github.com/TodoEconometria/curso-spring-hibernate/network/members)
[![License: CC BY-NC-SA 4.0](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-lightgrey.svg?style=for-the-badge)](https://creativecommons.org/licenses/by-nc-sa/4.0/)
[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

### [**Ver Sitio Web del Curso**](https://todoeconometria.github.io/curso-spring-hibernate/)

</div>

---

## El Curso en Numeros

| 75 Horas | 15 Dias | 16 Blueprints | 15 Manuales | 6+ Tecnologias |
|:--------:|:-------:|:-------------:|:-----------:|:--------------:|
| de contenido | de clase | proyectos individuales | paso a paso | profesionales |

---

## Stack Tecnologico

### Lenguaje y Build

| Tecnologia | Version | Para que |
|------------|---------|----------|
| **Java** | 21 (LTS) | Lenguaje principal |
| **Maven** | 3.9+ | Build, dependencias, lifecycle |
| **JUnit 5** | 5.10+ | Testing unitario |

### Persistencia

| Tecnologia | Para que |
|------------|----------|
| **Hibernate 7** | ORM — mapeo objeto-relacional |
| **JPA (Jakarta)** | API estandar de persistencia |
| **Spring Data JPA** | Repositorios automaticos |
| **H2** | Base de datos en memoria (desarrollo) |
| **PostgreSQL 16** | Base de datos en produccion |

### Framework Web

| Tecnologia | Para que |
|------------|----------|
| **Spring Boot 4** | Framework principal, autoconfig |
| **Spring Web MVC** | Controladores REST |
| **Jackson** | Serializacion JSON automatica |
| **Lombok** | Reducir boilerplate (getters, setters) |

### Infraestructura

| Tecnologia | Para que |
|------------|----------|
| **Docker** | Contenedores |
| **Docker Compose** | Orquestacion multi-contenedor |
| **GitHub Actions** | CI/CD |

### Herramientas

| Herramienta | Para que |
|-------------|----------|
| **IntelliJ IDEA** | IDE principal |
| **DBeaver** | Cliente de base de datos |
| **Postman** | Pruebas de API REST |
| **Git + GitHub** | Control de versiones |

---

## Modulos del Curso

| Dia | Tema | Proyecto |
|:---:|------|----------|
| 6 | Maven + Streams | Pizzeria con Maven y Gson |
| 7 | Maven + Pizzeria | Pizzeria v4 (Maven + dependencias) |
| 8 | Maven avanzado | mi-biblioteca (scopes, lifecycle, JUnit 5) |
| 9 | Hibernate intro | curso-hibernate (ORM, EntityManager, CRUD) |

> Los dias 10-20 se publicaran a medida que avance el curso.

---

## Inicio Rapido

### 1. Haz fork de este repositorio

Click en el boton **Fork** (arriba a la derecha en GitHub).

### 2. Clona tu fork

```bash
git clone https://github.com/TU_USUARIO/curso-spring-hibernate.git
cd curso-spring-hibernate
```

### 3. Configura el upstream

```bash
git remote add upstream https://github.com/TodoEconometria/curso-spring-hibernate.git
```

### 4. Abre los manuales

Los manuales estan en la carpeta `manuales/`. Empieza por `00_INDICE_CURSO.md`.

**Guia detallada:** [docs/git-github/fork-clone.md](docs/git-github/fork-clone.md)

---

## Estructura del Repositorio

```
curso-spring-hibernate/
├── manuales/                   # 15 manuales paso a paso
│   ├── 00_INDICE_CURSO.md
│   ├── DIA_06_MANUAL_MAVEN_STREAMS.md
│   ├── ...
│   └── DIA_20_MANUAL_DEMO_DAY.md
│
├── pizzeria/                   # Codigo esqueleto de la Pizzeria (v3.1)
│   └── src/com/pizzeria/...
│
├── blueprints/                 # 16 proyectos individuales para elegir
│   ├── README.md
│   ├── 01_CineEstrella.md
│   ├── ...
│   └── 16_BarajaEspanola.md
│
├── entregas/                   # Zona de entregas del alumno
│   ├── ejercicios/
│   └── trabajo_final/
│
├── trabajo_final/              # Enunciado + plantilla del trabajo final
│   ├── README.md
│   └── plantilla/
│
└── docs/git-github/            # Guias de Git y GitHub
```

---

## Proyecto Pizzeria — El Hilo Conductor

La Pizzeria es un proyecto que **evoluciona** a lo largo del curso:

| Version | Dia | Tecnologia |
|:-------:|:---:|------------|
| v1-v3 | 1-5 | Java puro (compilacion manual) |
| v4 | 6-7 | Maven + Gson |
| v5 | 9-10 | Hibernate + H2 |
| v6 | 11-12 | Spring Boot REST API |
| v7 | 15-18 | Docker + PostgreSQL + CI/CD |

Cada version construye sobre la anterior. El codigo esqueleto (v3.1) esta en la carpeta `pizzeria/`.

---

## Blueprints — Proyectos Individuales

En los dias 13-14, cada alumno elige **uno** de los 16 blueprints disponibles para construir su propio proyecto con Spring Boot + Hibernate.

Ver la lista completa en [`blueprints/README.md`](blueprints/README.md).

---

## Trabajo Final

El trabajo final integra TODO lo aprendido: Spring Boot + Hibernate + Docker + CI/CD.

Ver enunciado completo en [`trabajo_final/README.md`](trabajo_final/README.md).

---

## Instructor

**Juan Marcelo Gutierrez Miranda** — [@TodoEconometria](https://www.linkedin.com/in/juangutierrezconsultor/)

10+ anos en desarrollo de software y formacion. Instructor en toda Latinoamerica y Espana.

**Contacto:**
- Email: cursos@todoeconometria.com
- LinkedIn: [Juan Gutierrez](https://www.linkedin.com/in/juangutierrezconsultor/)
- Web: [todoeconometria.com](https://www.todoeconometria.com)

---

<div align="center">

**© 2026 Juan Marcelo Gutierrez Miranda** — Material Educativo — CC BY-NC-SA 4.0

**Hash ID:** 7a3f1b9c4d8e2f5a6b0c9d8e7f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a

</div>
