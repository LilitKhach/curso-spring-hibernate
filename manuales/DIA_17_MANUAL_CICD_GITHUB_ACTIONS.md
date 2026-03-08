# Día 17: CI/CD — Automatización con GitHub Actions

Hoy cierran el círculo: cada vez que hagan push a GitHub, su proyecto se compila, se testea y se empaqueta automáticamente. Sin intervención manual.

Prof. Juan Marcelo Gutierrez Miranda

**Curso IFCD0014 — Semana 4, Día 17 (Miércoles)**
**Objetivo:** Entender CI/CD, crear un pipeline con GitHub Actions que compile, teste y empaquete su aplicación Spring Boot, y conocer Jenkins como alternativa empresarial.

> Este manual es de consulta. Sigan los pasos con su repositorio de GitHub abierto.

---

# PARTE I — CONCEPTOS DE CI/CD

# 1. ¿Qué es CI/CD?

## CI — Integración Continua (Continuous Integration)

Cada vez que alguien hace `git push`, un servidor automáticamente:

1. Descarga el código
2. Compila el proyecto
3. Ejecuta los tests
4. Reporta si algo falló

El objetivo: detectar errores **en minutos**, no en días.

## CD — Entrega/Despliegue Continuo (Continuous Delivery/Deployment)

Si la compilación y los tests pasan, automáticamente:

- **Continuous Delivery:** prepara el artefacto listo para desplegar (alguien aprieta un botón)
- **Continuous Deployment:** despliega directamente a producción (sin intervención humana)

## Sin CI/CD vs Con CI/CD

```
╔══════════════════════════════════════════════════════════════╗
║              SIN CI/CD vs CON CI/CD                           ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  SIN CI/CD (manual):                                         ║
║  ─────────────────────                                       ║
║  1. Desarrollador hace cambios                               ║
║  2. "Ya lo probé en mi máquina, funciona"                    ║
║  3. Sube a GitHub                                            ║
║  4. Alguien más descarga y... no compila                     ║
║  5. Horas investigando quién rompió qué                      ║
║  6. Deploy manual al servidor: copiar JAR por FTP            ║
║                                                              ║
║  CON CI/CD (automatizado):                                   ║
║  ─────────────────────────                                   ║
║  1. Desarrollador hace push                                  ║
║  2. GitHub Actions automáticamente compila                   ║
║  3. Ejecuta los tests                                        ║
║  4. Si algo falla → notificación inmediata                   ║
║  5. Si todo pasa → artefacto listo                           ║
║  6. Opcionalmente: deploy automático                         ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

## El flujo completo

```
╔══════════════════════════════════════════════════════════════╗
║              FLUJO CI/CD                                      ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Desarrollador                                               ║
║      │                                                       ║
║      ▼  git push                                             ║
║  GitHub (repositorio)                                        ║
║      │                                                       ║
║      ▼  dispara workflow                                     ║
║  GitHub Actions (servidor CI)                                ║
║      │                                                       ║
║      ├──→ Checkout código                                    ║
║      ├──→ Instalar Java 17                                   ║
║      ├──→ Compilar (mvn compile)                             ║
║      ├──→ Tests (mvn test)                                   ║
║      ├──→ Empaquetar (mvn package)                           ║
║      │                                                       ║
║      ▼                                                       ║
║  Resultado: check verde o X roja                             ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# PARTE II — GITHUB ACTIONS

# 2. GitHub Actions: CI/CD Gratis para Repositorios GitHub

GitHub Actions es una plataforma de automatización integrada directamente en GitHub. No necesitan instalar nada, no necesitan un servidor propio.

```
╔══════════════════════════════════════════════════════════════╗
║              CONCEPTOS DE GITHUB ACTIONS                      ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Workflow    = el pipeline completo (archivo YAML)           ║
║  Job         = un grupo de pasos (ej: "build")               ║
║  Step        = una acción individual (ej: "compilar")        ║
║  Action      = acción reutilizable (ej: actions/checkout)    ║
║  Runner      = la máquina donde se ejecuta (Ubuntu en nube)  ║
║                                                              ║
║  Workflow contiene → Jobs                                    ║
║  Job contiene      → Steps                                   ║
║  Step usa          → Actions o comandos shell                ║
║                                                              ║
║  COSTO:                                                      ║
║  - Repositorios públicos: GRATIS (ilimitado)                 ║
║  - Repositorios privados: 2000 min/mes gratis                ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# 3. Su Primer Workflow

## Paso 1: Crear la estructura de carpetas

GitHub Actions busca los workflows en `.github/workflows/`. Creen esa estructura dentro de su proyecto:

```bash
mkdir -p .github/workflows
```

## Paso 2: Crear el archivo del workflow

Creen el archivo `.github/workflows/build.yml`:

```yaml
# ============================================================
# Pipeline CI: Compilar y testear en cada push
# ============================================================
name: Build and Test

# Cuando se ejecuta este workflow
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

# Los trabajos que se van a ejecutar
jobs:
  build:
    # En que maquina se ejecuta (Ubuntu en los servidores de GitHub)
    runs-on: ubuntu-latest

    steps:
      # Paso 1: Descargar el codigo del repositorio
      - name: Checkout codigo
        uses: actions/checkout@v4

      # Paso 2: Instalar Java 17
      - name: Configurar Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      # Paso 3: Cache de dependencias Maven (acelera builds siguientes)
      - name: Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os }}-maven-

      # Paso 4: Compilar
      - name: Compilar
        run: mvn compile

      # Paso 5: Ejecutar tests
      - name: Ejecutar tests
        run: mvn test

      # Paso 6: Empaquetar (generar el .jar)
      - name: Empaquetar
        run: mvn package -DskipTests
```

## Explicación de cada parte

### `name:` — Nombre del workflow

```yaml
name: Build and Test
```

El nombre que aparece en la pestaña Actions de GitHub. Pongan algo descriptivo.

### `on:` — Cuándo se ejecuta

```yaml
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
```

Se ejecuta cuando:
- Alguien hace `push` a la rama `main`
- Alguien abre un Pull Request hacia `main`

### `jobs:` — Los trabajos

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
```

- `build` es el nombre del job (pueden llamarlo como quieran)
- `runs-on: ubuntu-latest` = ejecutar en la última versión de Ubuntu (en la nube de GitHub, no en su máquina)

### `steps:` — Los pasos dentro del job

Cada `step` tiene:
- `name:` — descripción legible
- `uses:` — una Action predefinida (de GitHub o la comunidad)
- `run:` — un comando de terminal

| Step | ¿Qué hace? | Tipo |
|------|-------------|------|
| `actions/checkout@v4` | Descarga el código del repo | Action |
| `actions/setup-java@v4` | Instala Java 17 (Temurin) | Action |
| `actions/cache@v4` | Cachea `~/.m2` para no descargar deps cada vez | Action |
| `mvn compile` | Compila el código | Comando |
| `mvn test` | Ejecuta tests unitarios | Comando |
| `mvn package -DskipTests` | Genera el `.jar` | Comando |

---

# PARTE III — PROBAR EL PIPELINE

# 4. Probar el Pipeline

## Paso 1: Hacer commit y push

```bash
git add .github/workflows/build.yml
git commit -m "Add CI pipeline with GitHub Actions"
git push
```

## Paso 2: Ver el pipeline en GitHub

1. Abran su repositorio en GitHub
2. Vayan a la pestaña **Actions**
3. Van a ver el workflow "Build and Test" ejecutándose

Cada paso se muestra con un indicador:

- Círculo amarillo girando = en ejecución
- Check verde = pasó
- X roja = falló

## Paso 3: Esperar el resultado

El pipeline completo tarda entre 1 y 3 minutos (la primera vez puede tardar más por las dependencias Maven).

```
╔══════════════════════════════════════════════════════════════╗
║              RESULTADO DEL PIPELINE                           ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  TODO VERDE:                                                 ║
║  ✓ Checkout codigo                                           ║
║  ✓ Configurar Java 17                                        ║
║  ✓ Cache Maven                                               ║
║  ✓ Compilar                                                  ║
║  ✓ Ejecutar tests                                            ║
║  ✓ Empaquetar                                                ║
║                                                              ║
║  → Su proyecto compila, los tests pasan, el .jar se genera.  ║
║    El código está en buen estado.                            ║
║                                                              ║
║  SI ALGO FALLA (X roja):                                     ║
║  → Hagan clic en el paso que falló                           ║
║  → Lean el log de error                                      ║
║  → Corrijan, commit, push → el pipeline se ejecuta de nuevo  ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# 5. Cuando el Pipeline Falla

No se asusten. Es normal que falle, especialmente la primera vez. Así aprenden a leer los logs.

## Errores comunes y soluciones

| Error | Causa probable | Solución |
|-------|---------------|----------|
| `Compilation failure` | Error de sintaxis o import faltante | Arreglen el código, commit, push |
| `Tests run: X, Failures: Y` | Un test no pasó | Arreglen el test o el código que testea |
| `Cannot resolve dependencies` | Dependencia mal declarada en pom.xml | Verifiquen groupId, artifactId, version |
| `java.lang.UnsupportedClassVersionError` | Java version del workflow no coincide | Verifiquen `java-version` en el YAML |
| `Error: Process completed with exit code 1` | Error genérico | Lean las líneas anteriores del log |

## Cómo leer los logs

1. En GitHub → Actions → clic en el workflow fallido
2. Clic en el job "build"
3. Clic en el paso que tiene la X roja
4. Lean el log expandido — el error está ahí

> Cada vez que hacen push, el pipeline se ejecuta de nuevo automáticamente. Corrijan y vuelvan a hacer push hasta que el check esté verde.

---

# PARTE IV — MEJORAS AL PIPELINE

# 6. Mejoras al Pipeline

## Agregar build de la imagen Docker

Si quieren que el pipeline también construya la imagen Docker, agreguen este paso al final del workflow:

```yaml
      # Paso 7: Construir imagen Docker
      - name: Build Docker image
        run: docker build -t mi-app:${{ github.sha }} .
```

`${{ github.sha }}` es el hash del commit — cada imagen queda etiquetada con el commit exacto que la generó.

El workflow completo con Docker quedaría así:

```yaml
name: Build, Test and Docker

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

      - name: Build Docker image
        run: docker build -t mi-app:${{ github.sha }} .
```

## Agregar badge al README

Un badge es un indicador visual que muestra si el pipeline está pasando. Agregen esta línea al inicio de su `README.md`:

```markdown
![Build](https://github.com/USERNAME/REPO/actions/workflows/build.yml/badge.svg)
```

Reemplacen `USERNAME` con su usuario de GitHub y `REPO` con el nombre de su repositorio. Por ejemplo:

```markdown
![Build](https://github.com/juan-perez/pizzeria-api/actions/workflows/build.yml/badge.svg)
```

El badge se actualiza automáticamente. Si el pipeline pasa, se ve verde. Si falla, se ve rojo.

---

# PARTE V — JENKINS (REFERENCIA)

# 7. Jenkins: La Alternativa Enterprise

Jenkins es la herramienta de CI/CD más veterana. Es open-source, autohospedada y usada por miles de empresas grandes.

## ¿Qué es Jenkins?

- Un servidor de CI/CD que corre en su propia máquina (o VM)
- Necesita Java para funcionar
- Se configura con un `Jenkinsfile` (escrito en Groovy)
- Tiene cientos de plugins para integrarse con todo
- Lo usan empresas que no quieren depender de servicios en la nube

## Comparación: GitHub Actions vs Jenkins

| Aspecto | GitHub Actions | Jenkins |
|---------|---------------|---------|
| **Instalación** | Cero (viene con GitHub) | Instalar servidor + Java + plugins |
| **Costo** | Gratis para repos públicos | Software gratis, pero pagan el servidor |
| **Configuración** | `.github/workflows/*.yml` | `Jenkinsfile` (Groovy) |
| **Dónde se ejecuta** | Nube de GitHub | Su propio servidor |
| **Mantenimiento** | GitHub lo mantiene | Ustedes lo mantienen |
| **Escalabilidad** | Automática | Manual (agregar agentes) |
| **Mejor para** | Equipos modernos, open source | Enterprise, on-premises, regulaciones |

## Ejemplo básico de Jenkinsfile

Para que vean la equivalencia, así se vería un pipeline similar en Jenkins:

```groovy
pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    stages {
        stage('Compilar') {
            steps {
                sh 'mvn compile'
            }
        }
        stage('Tests') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Empaquetar') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
    }
}
```

La estructura es similar: etapas (stages) con pasos (steps). La diferencia es que Jenkins necesita un servidor propio corriendo y configurado.

> Jenkins está en el programa oficial del curso IFCD0014. Deben saber que existe, para qué sirve y cómo se compara con GitHub Actions. Pero para un proyecto nuevo en 2026, GitHub Actions es la opción práctica.

---

# PARTE VI — PRÁCTICA

# 8. Ejercicio: Agregar GitHub Actions a su Proyecto Personal

Sigan estos pasos:

**Paso 1: Crear la carpeta de workflows**

```bash
mkdir -p .github/workflows
```

**Paso 2: Crear el archivo build.yml**

Copien el workflow de la sección 3. Si tienen Dockerfile, usen la versión con Docker de la sección 6.

**Paso 3: Commit y push**

```bash
git add .github/workflows/build.yml
git commit -m "Add CI pipeline with GitHub Actions"
git push
```

**Paso 4: Verificar en GitHub**

1. Vayan a su repositorio en GitHub
2. Pestaña **Actions**
3. Esperen a que el workflow termine
4. Verificar check verde

**Paso 5: Agregar badge al README**

Editen su `README.md` y agreguen al inicio:

```markdown
![Build](https://github.com/SU-USUARIO/SU-REPO/actions/workflows/build.yml/badge.svg)
```

Commit y push:

```bash
git add README.md
git commit -m "Add CI badge to README"
git push
```

**Verificación final:**

- [ ] Workflow aparece en la pestaña Actions
- [ ] El check está verde
- [ ] El badge se ve en el README

---

# PARTE VII — GPS Y RESUMEN

# 9. GPS Arquitectónico

```
╔══════════════════════════════════════════════════════════════╗
║              GPS: EL FLUJO PROFESIONAL COMPLETO               ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Desarrollador                                               ║
║      │                                                       ║
║      ▼  git push                                             ║
║  GitHub                                                      ║
║      │                                                       ║
║      ▼  dispara GitHub Actions                               ║
║  CI Pipeline                                                 ║
║      ├──→ Compilar (mvn compile)                             ║
║      ├──→ Testear (mvn test)                                 ║
║      ├──→ Empaquetar (mvn package)                           ║
║      ├──→ Construir imagen Docker                            ║
║      │                                                       ║
║      ▼                                                       ║
║  Imagen Docker lista para desplegar                          ║
║      │                                                       ║
║      ▼  (futuro: CD)                                         ║
║  Servidor de producción                                      ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Su código ahora tiene gestión profesional:                  ║
║                                                              ║
║  ✓ Control de versiones (Git + GitHub)                       ║
║  ✓ Compilación y tests automáticos (GitHub Actions)          ║
║  ✓ Empaquetado reproducible (Docker)                         ║
║  ✓ Orquestación de servicios (Docker Compose)                ║
║                                                              ║
║  Esto es lo que hacen los equipos profesionales.             ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# 10. Resumen

```
╔═══════════════════════════════════════════════════════════════╗
║           RESUMEN DEL DÍA 17 — CI/CD + GITHUB ACTIONS        ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  CONCEPTOS:                                                   ║
║  CI = Integración Continua (build + test en cada push)        ║
║  CD = Entrega Continua (deploy automático después de CI)      ║
║                                                               ║
║  GITHUB ACTIONS:                                              ║
║  - Archivo: .github/workflows/build.yml                       ║
║  - Se ejecuta en servidores de GitHub (Ubuntu)                ║
║  - Gratis para repos públicos                                 ║
║                                                               ║
║  ESTRUCTURA DEL WORKFLOW:                                     ║
║  name → nombre del pipeline                                   ║
║  on   → cuándo se ejecuta (push, pull_request)                ║
║  jobs → trabajos (build)                                      ║
║    steps → pasos (checkout, java, compile, test, package)     ║
║                                                               ║
║  JENKINS (referencia):                                        ║
║  - CI/CD autohospedado (necesita servidor propio)             ║
║  - Configuración: Jenkinsfile (Groovy)                        ║
║  - Usado en empresas grandes con infraestructura propia       ║
║                                                               ║
║  ARCHIVOS NUEVOS HOY:                                         ║
║  .github/workflows/build.yml → pipeline CI                    ║
║  README.md (badge)           → indicador visual del build     ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```
