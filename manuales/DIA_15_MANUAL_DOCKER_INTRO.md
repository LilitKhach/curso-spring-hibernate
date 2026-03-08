# Día 15: Docker — Su Aplicación en un Contenedor

Hoy su aplicación deja de depender de "lo que hay instalado en mi máquina". Van a empaquetarla con todo lo que necesita para funcionar en cualquier lugar.

Prof. Juan Marcelo Gutierrez Miranda

**Curso IFCD0014 — Semana 4, Día 15 (Lunes)**
**Objetivo:** Entender qué es Docker, instalar Docker Desktop, aprender los comandos básicos, crear un Dockerfile para una aplicación Spring Boot y ejecutarla en un contenedor.

> Este manual es de consulta. Sigan los pasos con Docker Desktop abierto y la terminal lista.

---

# PARTE I — EL PROBLEMA Y LA SOLUCIÓN

# 1. El Problema: "En mi máquina funciona"

Todos hemos escuchado (o dicho) esta frase. Un compañero les pasa su proyecto y no compila. ¿Por qué?

- Ustedes tienen Java 17, él tiene Java 21
- Ustedes están en Windows, el servidor es Linux
- Les falta una variable de entorno que nadie documentó
- La versión de PostgreSQL del servidor es diferente
- Hay una librería del sistema operativo que nadie recuerda haber instalado

El resultado siempre es el mismo: funciona en un lugar y no en otro.

```
╔══════════════════════════════════════════════════════════════╗
║              EL PROBLEMA CLÁSICO DEL DESPLIEGUE              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Desarrollador:  "Funciona en mi máquina"                    ║
║  Operaciones:    "Pues aquí no funciona"                     ║
║  Desarrollador:  "Es tu problema"                            ║
║  Operaciones:    "Es TU código"                              ║
║                                                              ║
║  RESULTADO: horas perdidas revisando configuraciones         ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  CON DOCKER:                                                 ║
║  Desarrollador:  "Aquí está mi contenedor"                   ║
║  Operaciones:    "Funciona. Listo."                          ║
║                                                              ║
║  RESULTADO: la app lleva TODO lo que necesita consigo        ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

**Docker resuelve esto:** empaqueta la aplicación + Java + dependencias + configuración en una "caja" (contenedor) que funciona igual en cualquier máquina.

---

# 2. ¿Qué es Docker?

## Contenedor vs Máquina Virtual

Antes de Docker, la solución era usar máquinas virtuales (VMs). Pero una VM incluye un sistema operativo completo, lo cual es pesado y lento.

```
╔══════════════════════════════════════════════════════════════╗
║           MÁQUINA VIRTUAL vs CONTENEDOR DOCKER               ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  MÁQUINA VIRTUAL:                  CONTENEDOR DOCKER:        ║
║                                                              ║
║  ┌──────────────────┐              ┌────────┐ ┌────────┐     ║
║  │   Tu App         │              │ App A  │ │ App B  │     ║
║  │   Java 17        │              │ Java17 │ │ Java21 │     ║
║  │   Ubuntu 22.04   │              │ libs   │ │ libs   │     ║
║  │   (SO completo)  │              └────────┘ └────────┘     ║
║  │   Kernel Linux   │              ─────────────────────     ║
║  └──────────────────┘              Docker Engine             ║
║  ─────────────────────             ─────────────────────     ║
║  Hypervisor (VMware)               Sistema Operativo Host    ║
║  ─────────────────────             ─────────────────────     ║
║  Hardware                          Hardware                  ║
║                                                              ║
║  Peso: 2-10 GB por VM              Peso: 50-400 MB por cont ║
║  Arranque: 1-3 minutos             Arranque: 1-3 segundos   ║
║  RAM: reserva fija                 RAM: solo lo que usa      ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

## Conceptos clave: Imagen y Contenedor

Si vienen de Java, esta analogía les va a resultar natural:

```
╔══════════════════════════════════════════════════════════════╗
║              IMAGEN vs CONTENEDOR (analogía Java)            ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  IMAGEN = la CLASE                                           ║
║  ─────────────────                                           ║
║  - Es una plantilla (blueprint)                              ║
║  - Es de solo lectura (inmutable)                            ║
║  - Se define una vez                                         ║
║  - Ejemplo: eclipse-temurin:17-jre-alpine                    ║
║                                                              ║
║  CONTENEDOR = el OBJETO (instancia)                          ║
║  ─────────────────────────────────                           ║
║  - Es una ejecución viva de la imagen                        ║
║  - Puede haber muchos contenedores de la misma imagen        ║
║  - Se crea, se ejecuta, se detiene, se elimina               ║
║  - Ejemplo: docker run eclipse-temurin:17-jre-alpine         ║
║                                                              ║
║  Igual que en Java:                                          ║
║  class Pizza { ... }        →  Imagen                        ║
║  Pizza p1 = new Pizza();    →  Contenedor                    ║
║  Pizza p2 = new Pizza();    →  Otro contenedor               ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

## Docker Engine y Docker Hub

- **Docker Engine:** el motor que ejecuta contenedores en su máquina. Ustedes instalan Docker Desktop, que incluye el Engine.
- **Docker Hub:** el "Maven Central" de Docker. Un repositorio público con miles de imágenes listas para usar: `postgres`, `nginx`, `maven`, `eclipse-temurin`, etc. Dirección: https://hub.docker.com

---

# PARTE II — INSTALACIÓN Y PRIMEROS PASOS

# 3. Instalar Docker Desktop

## Paso 1: Descargar

Ir a https://www.docker.com/products/docker-desktop/ y descargar Docker Desktop para Windows.

## Paso 2: Instalar

Ejecutar el instalador. Aceptar los valores por defecto. Reiniciar Windows cuando lo pida.

## Paso 3: Verificar la instalación

Abrir una terminal (Git Bash, PowerShell o CMD) y ejecutar:

```bash
docker --version
```

Deben ver algo como:

```
Docker version 27.x.x, build xxxxxxx
```

## Paso 4: El primer contenedor

```bash
docker run hello-world
```

Si ven el mensaje "Hello from Docker!" — Docker funciona correctamente.

### ¿Qué acaba de pasar?

```
╔══════════════════════════════════════════════════════════════╗
║          ¿QUÉ HIZO docker run hello-world?                   ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  1. Docker buscó la imagen "hello-world" en su máquina       ║
║     → No la encontró                                         ║
║                                                              ║
║  2. La descargó de Docker Hub (docker pull hello-world)      ║
║     → "Unable to find image... Pulling from library..."      ║
║                                                              ║
║  3. Creó un contenedor a partir de la imagen                 ║
║     → Como hacer new HelloWorld()                            ║
║                                                              ║
║  4. Ejecutó el contenedor                                    ║
║     → Imprimió el mensaje                                    ║
║                                                              ║
║  5. El contenedor terminó (exit 0)                           ║
║     → El contenedor ya no está corriendo                     ║
║     → Pero sigue existiendo (parado)                         ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# 4. Comandos Básicos de Docker

## Los 8 comandos que van a usar siempre

| Comando | ¿Qué hace? | Equivalencia |
|---------|-------------|-------------|
| `docker run` | Crea y ejecuta un contenedor | `new Objeto()` + ejecutar |
| `docker ps` | Lista contenedores en ejecución | Ver procesos activos |
| `docker ps -a` | Lista TODOS los contenedores | Incluye los detenidos |
| `docker stop` | Detiene un contenedor | Parar el proceso |
| `docker rm` | Elimina un contenedor | Borrar el objeto |
| `docker images` | Lista imágenes locales | Ver clases disponibles |
| `docker pull` | Descarga una imagen de Docker Hub | Como `mvn dependency:resolve` |
| `docker build` | Construye una imagen desde Dockerfile | Como `mvn package` |

## Práctica 1: Un servidor web en 10 segundos

```bash
docker run -d -p 80:80 nginx
```

Abran el navegador en http://localhost — tienen un servidor web Nginx funcionando.

¿Qué significan las opciones?

- `-d` = detached (en segundo plano, no bloquea la terminal)
- `-p 80:80` = conectar el puerto 80 de su máquina al puerto 80 del contenedor

```bash
# Ver que el contenedor esta corriendo
docker ps

# Detener el contenedor (usen el CONTAINER ID que les dio docker ps)
docker stop <container_id>

# Verificar que ya no esta corriendo
docker ps

# Pero sigue existiendo (detenido)
docker ps -a

# Eliminar el contenedor
docker rm <container_id>
```

## Práctica 2: Una base de datos PostgreSQL instantánea

```bash
docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=secret postgres:16-alpine
```

Acaban de levantar un servidor PostgreSQL. Sin instaladores, sin configuración, sin "siguiente, siguiente, siguiente".

- `-e POSTGRES_PASSWORD=secret` = variable de entorno (la contraseña del admin)
- `postgres:16-alpine` = imagen de PostgreSQL versión 16, basada en Alpine Linux (ligera)

Para detenerlo:

```bash
docker ps
docker stop <container_id>
docker rm <container_id>
```

## Tabla de referencia rápida

```
╔═══════════════════════════════════════════════════════════════╗
║                 COMANDOS DOCKER — REFERENCIA                  ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  CONTENEDORES:                                                ║
║  docker run -d -p 8080:8080 imagen     → crear y ejecutar     ║
║  docker ps                              → ver activos         ║
║  docker ps -a                           → ver todos           ║
║  docker stop <id>                       → detener             ║
║  docker rm <id>                         → eliminar            ║
║  docker logs <id>                       → ver logs            ║
║  docker logs -f <id>                    → seguir logs en vivo ║
║                                                               ║
║  IMÁGENES:                                                    ║
║  docker images                          → listar locales      ║
║  docker pull imagen:tag                 → descargar           ║
║  docker build -t nombre:tag .           → construir           ║
║  docker rmi imagen                      → eliminar imagen     ║
║                                                               ║
║  LIMPIEZA:                                                    ║
║  docker system prune                    → limpiar todo        ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```

---

# PARTE III — DOCKERIZAR UNA APLICACIÓN SPRING BOOT

# 5. El Dockerfile: Receta para Empaquetar su App

## ¿Qué es un Dockerfile?

Un Dockerfile es un archivo de texto con instrucciones para construir una imagen. Es como una receta de cocina: paso a paso, sin ambigüedad.

Se llama exactamente `Dockerfile` (sin extensión), y se coloca en la raíz del proyecto.

## El Dockerfile para Spring Boot (multi-stage)

Creen un archivo llamado `Dockerfile` en la raíz de su proyecto (Pizzería o proyecto personal):

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

## Explicación línea por línea

### Etapa 1: Compilación

| Línea | ¿Qué hace? |
|-------|-------------|
| `FROM maven:3.9-eclipse-temurin-17 AS builder` | Usa una imagen que tiene Maven + JDK 17. La llama "builder". |
| `WORKDIR /app` | Crea y entra a la carpeta `/app` dentro del contenedor. |
| `COPY pom.xml .` | Copia el `pom.xml` de su máquina al contenedor. |
| `COPY src ./src` | Copia la carpeta `src/` de su máquina al contenedor. |
| `RUN mvn clean package -DskipTests` | Ejecuta Maven para compilar y empaquetar. Genera el `.jar` en `target/`. |

### Etapa 2: Ejecución

| Línea | ¿Qué hace? |
|-------|-------------|
| `FROM eclipse-temurin:17-jre-alpine` | Imagen nueva, solo con JRE (no JDK). Alpine = Linux ultraligero. |
| `WORKDIR /app` | Carpeta de trabajo. |
| `COPY --from=builder /app/target/*.jar app.jar` | Copia SOLO el `.jar` desde la etapa 1. Todo lo demás se descarta. |
| `EXPOSE 8080` | Documenta que la app usa el puerto 8080 (informativo). |
| `ENTRYPOINT ["java", "-jar", "app.jar"]` | Comando que se ejecuta al iniciar el contenedor. |

## ¿Por qué multi-stage?

```
╔══════════════════════════════════════════════════════════════╗
║              ¿POR QUÉ BUILD MULTI-STAGE?                     ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  SIN multi-stage (una sola etapa):                           ║
║  Imagen final tiene: Maven + JDK + codigo fuente + .jar      ║
║  Tamanio: ~1.2 GB                                            ║
║  Problema: en produccion NO necesitan Maven ni el fuente     ║
║                                                              ║
║  CON multi-stage (dos etapas):                               ║
║  Etapa 1: compila con Maven + JDK (se descarta despues)      ║
║  Etapa 2: solo JRE + el .jar                                 ║
║  Tamanio: ~200-400 MB                                        ║
║                                                              ║
║  Es como cocinar: usan ollas, cuchillos, tabla de cortar...  ║
║  pero al cliente le sirven solo el PLATO, no la cocina.      ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# 6. Construir y Ejecutar la Imagen

## Paso 1: Construir la imagen

Desde la raíz del proyecto (donde está el `Dockerfile`):

```bash
docker build -t mi-pizzeria:v1 .
```

- `-t mi-pizzeria:v1` = nombre y versión (tag) de la imagen
- `.` = contexto de build (la carpeta actual)

La primera vez tarda unos minutos porque descarga las imágenes base y todas las dependencias Maven. Las siguientes veces será mucho más rápido gracias al caché de capas.

### ¿Qué pasa durante el build?

```
╔══════════════════════════════════════════════════════════════╗
║              PROCESO DE BUILD (capas)                         ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Paso 1: FROM maven:3.9...          → descarga imagen Maven  ║
║  Paso 2: WORKDIR /app               → crea carpeta (capa)    ║
║  Paso 3: COPY pom.xml               → copia pom (capa)       ║
║  Paso 4: COPY src ./src             → copia codigo (capa)    ║
║  Paso 5: RUN mvn clean package      → compila (capa)         ║
║  Paso 6: FROM eclipse-temurin:17... → imagen nueva           ║
║  Paso 7: COPY --from=builder ...    → copia solo el .jar     ║
║                                                              ║
║  Cada instruccion crea una CAPA.                              ║
║  Docker cachea las capas que no cambiaron.                    ║
║  Si solo cambio codigo (src/), repite desde el paso 4.       ║
║  El pom.xml (paso 3) queda cacheado → no re-descarga deps.  ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

## Paso 2: Verificar que la imagen existe

```bash
docker images
```

Deben ver `mi-pizzeria` con tag `v1` en la lista.

## Paso 3: Ejecutar el contenedor

```bash
docker run -d -p 8080:8080 --name pizzeria mi-pizzeria:v1
```

- `-d` = en segundo plano
- `-p 8080:8080` = puerto de su máquina:puerto del contenedor
- `--name pizzeria` = nombre amigable para el contenedor

## Paso 4: Verificar que funciona

Abran el navegador o usen Postman:

```
http://localhost:8080/api/pizzas
```

Si ven la respuesta JSON con las pizzas — su aplicación está corriendo en un contenedor Docker.

## Paso 5: Ver los logs

```bash
docker logs pizzeria
```

Van a ver los logs de arranque de Spring Boot, igual que cuando ejecutan desde IntelliJ. Para seguir los logs en vivo:

```bash
docker logs -f pizzeria
```

Presionen `Ctrl + C` para salir del seguimiento de logs (el contenedor sigue corriendo).

## Paso 6: Detener y eliminar

```bash
docker stop pizzeria
docker rm pizzeria
```

---

# PARTE IV — PRÁCTICA

# 7. Ejercicio Práctico: Dockerizar la Pizzería

Sigan estos pasos:

**a) Crear el Dockerfile**

En la raíz del proyecto, crear un archivo llamado `Dockerfile` (sin extensión) con el contenido de la sección 5.

**b) Construir la imagen**

```bash
docker build -t mi-pizzeria:v1 .
```

Si el build falla, lean el error. Los más comunes:

| Error | Causa | Solución |
|-------|-------|----------|
| `pom.xml not found` | No están en la carpeta correcta | `cd` a la raíz del proyecto |
| `BUILD FAILURE` en Maven | El código no compila | Arreglen el error de compilación primero |
| `Connection refused` al descargar deps | Sin internet o detrás de proxy | Verificar conexión |

**c) Ejecutar el contenedor**

```bash
docker run -d -p 8080:8080 --name pizzeria mi-pizzeria:v1
```

**d) Probar los endpoints**

Abran Postman o el navegador y prueben sus endpoints. Por ejemplo:

```
GET  http://localhost:8080/api/pizzas
GET  http://localhost:8080/api/pizzas/1
POST http://localhost:8080/api/pedidos
```

**e) Ver los logs**

```bash
docker logs pizzeria
```

**f) Detener y limpiar**

```bash
docker stop pizzeria
docker rm pizzeria
```

---

# PARTE V — CONCEPTOS Y RESUMEN

# 8. Resumen de Conceptos Docker

```
╔══════════════════════════════════════════════════════════════╗
║              FLUJO COMPLETO DE DOCKER                         ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Dockerfile                                                  ║
║      │                                                       ║
║      ▼  docker build                                         ║
║  Imagen (inmutable, como una clase Java)                     ║
║      │                                                       ║
║      ▼  docker run                                           ║
║  Contenedor (efímero, como un objeto Java)                   ║
║      │                                                       ║
║      ▼  docker stop + docker rm                              ║
║  Contenedor eliminado                                        ║
║                                                              ║
║  La IMAGEN sigue existiendo.                                 ║
║  Pueden crear otro contenedor cuando quieran.                ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  REGLAS IMPORTANTES:                                         ║
║                                                              ║
║  - Las imágenes son INMUTABLES (no se modifican)             ║
║  - Los contenedores son EFÍMEROS (se crean y se destruyen)   ║
║  - Si necesitan cambiar algo → nuevo build → nueva imagen    ║
║  - Los datos dentro de un contenedor se PIERDEN al borrarlo  ║
║    (mañana vemos cómo resolver esto con volúmenes)           ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# 9. GPS Arquitectónico

```
╔══════════════════════════════════════════════════════════════╗
║              GPS: ¿DÓNDE ESTAMOS?                            ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  ANTES DE DOCKER:                                            ║
║  ─────────────────                                           ║
║  Su app corre en su portátil, con SU Java,                   ║
║  SU configuración, SUS variables de entorno.                 ║
║  Si la pasan a otro portátil... "en mi máquina funciona".    ║
║                                                              ║
║  DESPUÉS DE DOCKER:                                          ║
║  ──────────────────                                          ║
║  Su app corre en un contenedor con SU PROPIO Java,           ║
║  SU PROPIA configuración. Da igual dónde se ejecute:         ║
║  su portátil, el de un compañero, un servidor Linux,         ║
║  la nube de AWS... funciona IGUAL.                           ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Flujo actual del proyecto:                                  ║
║                                                              ║
║  Código Java                                                 ║
║    → Maven (compila + empaqueta .jar)                        ║
║      → Docker (empaqueta .jar + JRE en imagen)               ║
║        → Contenedor (ejecuta la imagen)                      ║
║                                                              ║
║  MAÑANA: Docker Compose — múltiples contenedores             ║
║  hablando entre sí (app + base de datos + admin).            ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# 10. Resumen: Tabla de Comandos del Día

```
╔═══════════════════════════════════════════════════════════════╗
║               COMANDOS DEL DÍA 15 — DOCKER                   ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  VERIFICAR INSTALACIÓN:                                       ║
║  docker --version                                             ║
║  docker run hello-world                                       ║
║                                                               ║
║  CONSTRUIR IMAGEN:                                            ║
║  docker build -t nombre:tag .                                 ║
║                                                               ║
║  EJECUTAR CONTENEDOR:                                         ║
║  docker run -d -p 8080:8080 --name mi-app nombre:tag          ║
║                                                               ║
║  VER CONTENEDORES:                                            ║
║  docker ps          (activos)                                 ║
║  docker ps -a       (todos)                                   ║
║                                                               ║
║  VER LOGS:                                                    ║
║  docker logs <id>                                             ║
║  docker logs -f <id>   (en vivo)                              ║
║                                                               ║
║  DETENER Y ELIMINAR:                                          ║
║  docker stop <id>                                             ║
║  docker rm <id>                                               ║
║                                                               ║
║  VER IMÁGENES:                                                ║
║  docker images                                                ║
║                                                               ║
║  LIMPIAR TODO:                                                ║
║  docker system prune                                          ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```
