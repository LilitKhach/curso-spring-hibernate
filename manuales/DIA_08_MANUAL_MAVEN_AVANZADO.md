# Día 8 — Maven avanzado: Dependencias, plugins y perfiles

Hoy profundizamos en las áreas clave de Maven que van a usar a diario en el trabajo: scopes, ciclo de vida, plugins, tests automáticos con JUnit 5 y perfiles por entorno.

Prof. Juan Marcelo Gutierrez Miranda

**Curso IFCD0014 — Semana 2**
**Requisito:** Haber completado los días 6 y 7 (Maven básico + Pizzería mavenizada)
**Objetivo:** Dominar scopes, ciclo de vida, plugins, tests automáticos con JUnit 5, perfiles por entorno, y empaquetar la Pizzería como JAR ejecutable.

> Este manual es de consulta. Sigan los pasos con el proyecto abierto en IntelliJ.

---

# PARTE I — SCOPES, CICLO DE VIDA Y PLUGINS

## 1.1 Repaso rápido: ¿Dónde estamos?

Después de los días 6 y 7, ustedes ya saben:

- Qué es Maven y por qué existe
- Crear un proyecto Maven desde IntelliJ
- El `pom.xml` es el "DNI" del proyecto
- `mvn compile` reemplaza al `javac` monstruoso
- `mvn exec:java` o Run desde IntelliJ para ejecutar
- Agregar una dependencia externa (Gson)

La Pizzería ya es un proyecto Maven (v4) con esta estructura:

```
Pizzeria_Alumno/
├── pom.xml
├── src/main/java/com/pizzeria/
│   ├── PizzeriaApp.java
│   ├── modelo/
│   ├── servicio/
│   └── repositorio/
└── target/                 ← generado por Maven
```

Hoy vamos a profundizar en las **3 áreas clave de Maven** que van a usar a diario en el trabajo:

```
╔══════════════════════════════════════════════════════╗
║            DÍA 8 — MAVEN AVANZADO                    ║
╠══════════════════════════════════════════════════════╣
║                                                      ║
║  1. Scopes: controlar cuándo se usa cada JAR         ║
║  2. Ciclo de vida: qué hace Maven internamente       ║
║  3. Plugins: extender las capacidades de Maven       ║
║  4. Tests: JUnit 5 con Maven                         ║
║  5. Perfiles: configuración por entorno              ║
║  6. JAR ejecutable: empaquetar la app                ║
║                                                      ║
╚══════════════════════════════════════════════════════╝
```

---

## 1.2 Scopes de dependencias: ¿Cuándo se necesita cada JAR?

### ¿Qué es un scope?

Un **scope** (ámbito) es una etiqueta que le dice a Maven **en qué momento del ciclo** necesita una dependencia. No es un archivo aparte ni una configuración separada — es una línea dentro de la etiqueta `<dependency>` del `pom.xml`:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>       ← ESTA LÍNEA es el scope
</dependency>
```

Si no ponen `<scope>`, Maven asume `compile` por defecto — la dependencia se incluye en todo.

### ¿Por qué existen los scopes?

En un proyecto real, no todas las dependencias se necesitan siempre:

- **Gson** lo necesitan para compilar, para probar y para ejecutar → va en todo
- **JUnit** solo lo necesitan para ejecutar tests → no tiene sentido meterlo en el JAR que van a producción
- **El driver de PostgreSQL** solo se usa cuando la app se conecta a la base de datos → no se necesita para compilar

Sin scopes, Maven metería TODO en el JAR final. En proyectos grandes con 50-100 dependencias, eso significa un archivo de 200 MB con librerías que nunca se usan en producción. Con scopes correctos, ese JAR baja a 30-50 MB.

**En cualquier empresa, los scopes se configuran desde el primer día.** Es una de las primeras cosas que revisan en un code review de un `pom.xml`.

### Tabla de scopes

| Scope | ¿Compilar? | ¿Tests? | ¿JAR final? | Ejemplo |
|-------|-----------|---------|-------------|---------|
| `compile` | Sí | Sí | Sí | Gson, Spring |
| `test` | No | Sí | No | JUnit, Mockito |
| `provided` | Sí | Sí | No | Servlet API (el servidor ya la tiene) |
| `runtime` | No | Sí | Sí | Driver PostgreSQL (solo se usa al ejecutar) |

### ¿Cómo se declara?

Estos son **4 ejemplos independientes**, uno por cada scope. No van todos juntos en un mismo `pom.xml` — cada uno muestra cómo se ve la etiqueta `<scope>` en la práctica:

```xml
<!-- compile (default — si no ponen scope, es compile) -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.11.0</version>
</dependency>

<!-- test — solo para tests, NO va en el JAR final -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>

<!-- provided — existe al compilar pero el servidor ya la tiene -->
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>6.0.0</version>
    <scope>provided</scope>
</dependency>

<!-- runtime — no se necesita para compilar, solo para ejecutar -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version>
    <scope>runtime</scope>
</dependency>
```

> **¿Cuáles van en nuestra Pizzería hoy?** Solo dos: Gson (`compile`, ya lo tienen del día 7) y JUnit (`test`, lo agregan hoy). Los otros dos son ejemplos para que conozcan los scopes — PostgreSQL lo usarán más adelante con perfiles, y Servlet API es un ejemplo teórico que no necesita la Pizzería.

### ¿Qué es un Servlet? (concepto clave para entrevistas)

Un **Servlet** es una clase Java que vive dentro de un servidor web (como Tomcat, Jetty o WildFly) y se encarga de **recibir peticiones HTTP y devolver respuestas**. Es el componente más básico de cualquier aplicación web Java.

```
¿QUÉ HACE UN SERVLET?

  Navegador                    Servidor (Tomcat)
  ─────────                    ──────────────────
      │                              │
      │  GET /pizzas                 │
      │ ──────────────────────────►  │
      │                              │  MiServlet.doGet()
      │                              │  → busca pizzas
      │                              │  → genera HTML/JSON
      │  200 OK + datos              │
      │ ◄──────────────────────────  │
      │                              │
```

Antes de que existiera Spring, **todo** se hacía con Servlets directamente. Se creaba una clase que extendía `HttpServlet` y se sobreescribían los métodos `doGet()`, `doPost()`, etc:

```java
// ASÍ SE PROGRAMABA ANTES (2000-2010):
public class PizzaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write("{\"pizza\": \"Margherita\"}");
    }
}
```

**¿Por qué importa saber esto?**

- **Spring Boot usa Servlets internamente.** Cuando escriben `@GetMapping("/pizzas")`, Spring traduce eso a un Servlet por debajo. No lo ven, pero está ahí.
- **Tomcat es un contenedor de Servlets.** Cuando Spring Boot arranca, levanta un Tomcat embebido que ejecuta los Servlets.
- **Preguntan en TODAS las entrevistas.** "¿Qué es un Servlet?", "¿Cuál es el ciclo de vida de un Servlet?", "¿Qué relación tiene Spring MVC con los Servlets?" son preguntas clásicas.

**¿Y por qué scope `provided`?** Porque el Servlet API es como los platos del restaurante — cuando su aplicación se despliega en un Tomcat, Tomcat **ya tiene** la librería `jakarta.servlet-api`. Si la incluyen en el JAR, habría dos copias y Tomcat se confunde. Por eso `provided` dice: "la necesito para compilar mi código, pero NO la metas en el JAR porque el servidor ya la tiene."

> Cuando usemos Spring Boot (día 11), todo esto pasa automáticamente. Spring Boot trae Tomcat embebido y gestiona los Servlets por ustedes. Pero entender qué hay debajo les va a ayudar a resolver problemas y a responder entrevistas.

### ¿Por qué importan los scopes?

Imaginen que su aplicación usa 50 dependencias. Si todas van con scope `compile`, el JAR final pesa 200 MB. Con scopes correctos, quizá pesa 30 MB.

**Regla práctica:**
- Si la usan en su código (`import ...`): `compile`
- Si solo la usan en tests: `test`
- Si el servidor ya la tiene (Tomcat, WildFly): `provided`
- Si solo se carga en ejecución (drivers de BD): `runtime`

---

## 1.3 El ciclo de vida de Maven

Cuando ejecutan `mvn package`, Maven no solo empaqueta. Ejecuta una **secuencia ordenada de fases**:

```
CICLO DE VIDA DEFAULT DE MAVEN:

  validate  →  compile  →  test  →  package  →  verify  →  install  →  deploy
     │            │          │         │           │           │          │
     │            │          │         │           │           │          │
  Verifica     Compila    Ejecuta   Crea el    Verifica    Copia el   Sube a
  que el       .java a    tests     .jar o     calidad     JAR al     un repo
  pom.xml      .class     JUnit     .war                   repo       remoto
  es válido                                                local
                                                           (~/.m2)
```

**Lo importante:** cada fase ejecuta TODAS las anteriores.

| Comando | ¿Qué ejecuta? |
|---------|---------------|
| `mvn compile` | validate → compile |
| `mvn test` | validate → compile → test |
| `mvn package` | validate → compile → test → package |
| `mvn install` | validate → compile → test → package → verify → install |

### Ejercicio: observar cada fase

Ejecuten estos comandos uno por uno y observen qué aparece en `target/`:

```bash
# 1. Limpiar todo
mvn clean
# Resultado: se borra la carpeta target/

# 2. Solo compilar
mvn compile
# Resultado: target/classes/ con los .class

# 3. Ejecutar tests
mvn test
# Resultado: target/test-classes/ + target/surefire-reports/

# 4. Empaquetar
mvn package
# Resultado: target/pizzeria-1.0-SNAPSHOT.jar

# 5. Instalar en repo local
mvn install
# Resultado: el JAR se copia a ~/.m2/repository/com/pizzeria/...
```

> **OJO:** `mvn package` crea un JAR en `target/`, pero ese JAR solo contiene el código compilado de la Pizzería, NO incluye las dependencias (Gson no está dentro). No es un JAR listo para desplegar en producción — más adelante verán cómo Spring Boot resuelve esto automáticamente.

### El ciclo `clean`

`clean` es un ciclo de vida **separado**. Borra la carpeta `target/`.

Por eso el comando más común es:

```bash
mvn clean package
```

Esto significa: "borra todo lo anterior y vuelve a construir desde cero."

---

## 1.4 Plugins: extender Maven

### ¿Qué es un plugin?

Un **plugin** es un programa externo que Maven descarga y ejecuta en una fase específica del ciclo de vida. Maven por sí solo es solo un **orquestador** — no sabe compilar Java, ejecutar tests ni crear JARs. TODO lo hacen los plugins.

¿Por qué está diseñado así? Porque Maven es **modular**. Si mañana sale Java 25 con una forma nueva de compilar, no hay que cambiar Maven — solo se actualiza el `maven-compiler-plugin`. Si quieren generar reportes de cobertura de código, agregan el plugin `jacoco-maven-plugin`. Si quieren crear un Docker image, agregan `jib-maven-plugin`. La idea es que Maven sea extensible sin límite.

### ¿Dónde se configuran?

Los plugins van dentro de `<build><plugins>` en el `pom.xml` — al mismo nivel que `<dependencies>` pero dentro de `<build>`:

```xml
<project>
    <dependencies>...</dependencies>    ← dependencias van aquí
    <build>
        <plugins>                       ← plugins van aquí DENTRO de build
            <plugin>...</plugin>
        </plugins>
    </build>
</project>
```

Muchos plugins vienen preconfigurados (Maven ya sabe que `maven-compiler-plugin` se ejecuta en la fase `compile`). Solo necesitan configurarlos explícitamente cuando quieren cambiar el comportamiento por defecto (por ejemplo, decirle que use Java 17 en vez de Java 5).

### Plugins que ya están usando (sin saberlo)

| Plugin | Fase | Qué hace |
|--------|------|----------|
| `maven-compiler-plugin` | compile | Compila `.java` a `.class` |
| `maven-surefire-plugin` | test | Ejecuta tests JUnit |
| `maven-jar-plugin` | package | Crea el archivo `.jar` |
| `maven-resources-plugin` | process-resources | Copia archivos de `resources/` |
| `maven-install-plugin` | install | Copia JAR al repositorio local |

### Configurar el compilador

Añadan esto al `pom.xml` de la Pizzería dentro de `<build>`:

*📁 Archivo a modificar: `pom.xml`*

```xml
<build>
    <plugins>
        <!-- Compilador: usar Java 17 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <encoding>UTF-8</encoding>
            </configuration>
        </plugin>
    </plugins>
</build>
```

> **¿No teníamos ya `<maven.compiler.source>17</maven.compiler.source>` en properties?**
> Sí. Eso es un atajo que funciona igual. La sección `<plugins>` permite **más control** (encoding, parámetros extra, etc.).

### Plugin para crear JAR ejecutable

El JAR que genera Maven por defecto **no es ejecutable**: no sabe cuál es la clase `main`. Para hacerlo ejecutable:

*📁 Archivo a modificar: `pom.xml` (dentro de `<plugins>`)*

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.4.1</version>
    <configuration>
        <archive>
            <manifest>
                <mainClass>com.pizzeria.PizzeriaApp</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

Después de agregar esto:

```bash
mvn clean package
java -jar target/pizzeria-1.0-SNAPSHOT.jar
```

> Si ven un error de `ClassNotFoundException` para Gson, es porque el JAR ejecutable no incluye las dependencias. Eso se resuelve con el `maven-shade-plugin` o el `maven-assembly-plugin`. Lo veremos cuando usemos Spring Boot, que lo hace automáticamente.

---

# PARTE II — TESTS CON JUNIT 5

# 2. Tests con JUnit 5

### ¿Qué es un test automático?

Un **test automático** es un programa que verifica que otro programa funciona correctamente. En vez de abrir la app, hacer clic, escribir datos y mirar la pantalla para ver si todo está bien (eso es un **test manual**), escriben código que hace esas verificaciones automáticamente.

```
TEST MANUAL (lo que hacen hoy):              TEST AUTOMÁTICO (lo que van a hacer):

1. Ejecutar la app                           1. Ejecutar: mvn test
2. Escribir datos                            2. Maven ejecuta TODOS los tests
3. Mirar la pantalla                         3. Si algo falla, Maven lo dice
4. "Parece que funciona..."                  4. "Tests run: 15, Failures: 0"
5. Repetir para cada caso                    5. Se ejecuta en 2 segundos
6. Toma 20 minutos                           6. Se puede repetir 100 veces al día
```

### ¿Por qué importan los tests?

- **Detectan errores antes de que lleguen al usuario.** Si cambian `PizzaService` y sin querer rompen el cálculo de precios, el test lo detecta inmediatamente.
- **Son documentación viva.** Un test llamado `testPreciosPositivos()` dice más sobre las reglas de negocio que un comentario en el código.
- **Dan confianza para hacer cambios.** Sin tests, cada refactoring es un riesgo. Con tests, pueden cambiar el código y verificar en 2 segundos que todo sigue funcionando.
- **En la industria son obligatorios.** En cualquier empresa seria, no se acepta un pull request sin tests. En muchas, el pipeline de CI/CD rechaza automáticamente código sin tests.

### ¿Qué es JUnit?

**JUnit** es el framework de testing más usado en Java. Fue creado en 1997 por Kent Beck y Erich Gamma (sí, el mismo del libro "Design Patterns"). La versión actual es **JUnit 5** (también llamada JUnit Jupiter), lanzada en 2017.

JUnit no es parte de Java — es una **librería externa** que se agrega como dependencia de Maven con scope `test` (porque los tests no van a producción).

### ¿Dónde van los tests?

Maven tiene una convención estricta:

```
src/main/java/      ← código de la aplicación
src/test/java/      ← tests (misma estructura de paquetes)
```

Para cada clase que quieran testear, crean una clase de test con el mismo nombre + `Test`:

```
src/main/java/com/pizzeria/servicio/PizzaService.java
src/test/java/com/pizzeria/servicio/PizzaServiceTest.java   ← test de esa clase
```

### El patrón Arrange-Act-Assert

Todo test sigue el mismo patrón de 3 pasos:

```java
@Test
void testPrecioPositivo() {
    // ARRANGE (preparar): crear los objetos necesarios
    Pizza pizza = new PizzaClasica("Margherita", 8.50,
            Pizza.Tamano.MEDIANA, List.of("tomate", "mozzarella"));

    // ACT (actuar): ejecutar lo que quieren probar
    double precio = pizza.getPrecio();

    // ASSERT (verificar): comprobar que el resultado es correcto
    assertTrue(precio > 0, "El precio debe ser positivo");
}
```

**Arrange** = preparar los datos. **Act** = ejecutar la acción. **Assert** = verificar el resultado.

### Tabla de asserts de JUnit 5

| Assert | Verifica que... |
|--------|----------------|
| `assertTrue(condicion)` | La condición sea true |
| `assertFalse(condicion)` | La condición sea false |
| `assertEquals(esperado, real)` | Dos valores sean iguales |
| `assertNotNull(objeto)` | El objeto no sea null |
| `assertThrows(Exception, ...)` | Se lance una excepción |

### Paso 1: Agregar JUnit 5

En el `pom.xml`, dentro de `<dependencies>`:

*📁 Archivo a modificar: `pom.xml`*

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>
```

### Paso 2: Crear la carpeta de tests

```
Pizzeria_Alumno/
├── src/
│   ├── main/java/com/pizzeria/       ← código de la app
│   └── test/java/com/pizzeria/       ← tests (CREAR ESTA CARPETA)
```

### Paso 3: Primer test

*📁 Archivo a crear: `src/test/java/com/pizzeria/servicio/PedidoServiceTest.java`*

```java
package com.pizzeria.servicio;

import com.pizzeria.modelo.*;
import com.pizzeria.repositorio.PedidoRepositoryMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceTest {

    private PedidoService pedidoService;
    private Cliente clientePrueba;

    @BeforeEach
    void setUp() {
        PedidoRepositoryMemoria pedidoRepository = new PedidoRepositoryMemoria();
        pedidoService = new PedidoService(pedidoRepository);
        clientePrueba = new Cliente("Juan Test", TipoCliente.PERSONA);
    }

    @Test
    @DisplayName("Debe crear un pedido para un cliente")
    void testCrearPedido() {
        Pedido pedido = pedidoService.crearPedido(clientePrueba);
        assertNotNull(pedido, "El pedido no debe ser null");
        assertEquals("Juan Test", pedido.getNombreCliente());
    }

    @Test
    @DisplayName("Un pedido nuevo debe tener 0 items")
    void testPedidoNuevoVacio() {
        Pedido pedido = pedidoService.crearPedido(clientePrueba);
        assertEquals(0, pedido.getCantidadItems(),
                "Un pedido nuevo debe tener 0 items");
    }

    @Test
    @DisplayName("Debe poder agregar una pizza al pedido")
    void testAgregarPizza() {
        Pedido pedido = pedidoService.crearPedido(clientePrueba);
        Pizza pizza = new PizzaClasica("Margherita", 8.50,
                Pizza.Tamano.MEDIANA, List.of("tomate", "mozzarella"));
        pedidoService.agregarItem(pedido.getNumero(), pizza);
        assertEquals(1, pedido.getCantidadItems());
    }

    @Test
    @DisplayName("El total debe ser positivo despues de agregar items")
    void testTotalPositivo() {
        Pedido pedido = pedidoService.crearPedido(clientePrueba);
        Pizza pizza = new PizzaClasica("Margherita", 8.50,
                Pizza.Tamano.MEDIANA, List.of("tomate", "mozzarella"));
        pedidoService.agregarItem(pedido.getNumero(), pizza);
        assertTrue(pedido.calcularTotal() > 0,
                "El total debe ser positivo despues de agregar items");
    }
}
```

### Paso 4: Ejecutar los tests

```bash
mvn test
```

Resultado esperado:

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Si algún test falla, Maven muestra exactamente cuál y por qué. Los reportes quedan en `target/surefire-reports/`.

### Ejercicio: escribir 2 tests más

*📁 Archivo a crear: `src/test/java/com/pizzeria/servicio/ClienteServiceTest.java`*

Creen ese archivo con:

1. Un test que registre un cliente y verifique que se creó correctamente
2. Un test que verifique que el nombre del cliente no está vacío

> **Pista:** `ClienteService` necesita dos dependencias en el constructor: `ClienteRepositoryMemoria` y `PedidoRepositoryMemoria`. Para registrar un cliente usen `registrarCliente("nombre", TipoCliente.PERSONA)`.

> **Estructura sugerida:**
> 1. En `@BeforeEach`: crear las dos dependencias y pasarlas al constructor de `ClienteService`
> 2. En el primer `@Test`: llamar a `registrarCliente(...)`, luego `assertNotNull` y `assertEquals` sobre el nombre
> 3. En el segundo `@Test`: registrar un cliente y verificar con `assertFalse(nombre.isEmpty())`
>
> El patrón es el mismo que `PedidoServiceTest` — solo cambia el servicio y sus dependencias.

---

# PARTE III — PERFILES Y REPOSITORIO LOCAL

## 3.1 Perfiles: configuración por entorno

### ¿Qué es un perfil?

Un **perfil** (profile) es un bloque de configuración dentro del `pom.xml` que solo se activa bajo ciertas condiciones. Piensen en ello como un "modo" de compilación.

### ¿Por qué existen?

En el trabajo real, la misma aplicación se ejecuta en diferentes **entornos**:

| Entorno | ¿Para qué? | Base de datos | Configuración |
|---------|-----------|---------------|---------------|
| **Desarrollo** (dev) | El programador en su máquina | H2 (base de datos escrita en Java que corre en memoria — no necesita instalación) | Logs detallados, debug activado |
| **Testing** (test) | Tests automáticos en el servidor | H2 o PostgreSQL de prueba | Datos de prueba |
| **Staging** (pre-prod) | Pruebas finales antes de producción | PostgreSQL real (copia) | Configuración casi igual a prod |
| **Producción** (prod) | Los usuarios reales | PostgreSQL/MySQL real | Logs mínimos, optimizado |

**Cada entorno necesita dependencias y configuraciones diferentes.** En desarrollo usan H2 porque es rápida y no necesita instalación. En producción usan PostgreSQL porque aguanta millones de registros.

**¿Se usa siempre?** Sí. Cualquier proyecto profesional tiene al menos 2 perfiles (dev y prod). Los proyectos grandes pueden tener 5-6 perfiles. Es tan estándar que Spring Boot lo trae integrado con `application-dev.properties` y `application-prod.properties`.

### ¿Dónde van los perfiles?

Los perfiles van dentro de `<profiles>` en el `pom.xml`, al mismo nivel que `<dependencies>` y `<build>`:

```xml
<project>
    <dependencies>...</dependencies>
    <build>...</build>
    <profiles>                    ← al mismo nivel que dependencies y build
        <profile>
            <id>dev</id>
            ...
        </profile>
        <profile>
            <id>prod</id>
            ...
        </profile>
    </profiles>
</project>
```

### Ejemplo práctico: diferentes bases de datos

*📁 Archivo a modificar: `pom.xml` (agregar al mismo nivel que `<dependencies>` y `<build>`)*

```xml
<profiles>
    <!-- Perfil de desarrollo: base de datos en memoria -->
    <profile>
        <id>dev</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <dependencies>
            <dependency>
                <groupId>com.h2database</groupId>
                <artifactId>h2</artifactId>
                <version>2.2.224</version>
            </dependency>
        </dependencies>
    </profile>

    <!-- Perfil de produccion: PostgreSQL real -->
    <profile>
        <id>prod</id>
        <dependencies>
            <dependency>
                <groupId>org.postgresql</groupId>
                <artifactId>postgresql</artifactId>
                <version>42.7.3</version>
            </dependency>
        </dependencies>
    </profile>
</profiles>
```

### Activar un perfil

```bash
# Compilar con perfil dev (H2)
mvn clean package -Pdev

# Compilar con perfil prod (PostgreSQL)
mvn clean package -Pprod

# Ver qué perfil está activo
mvn help:active-profiles
```

> **Todavía no usamos base de datos** — eso viene mañana con Hibernate. Pero ya dejamos preparados los perfiles en el `pom.xml` para cuando llegue el momento.

---

## 3.2 El repositorio local: ~/.m2/repository

Cada vez que Maven descarga una dependencia, la guarda localmente para no volver a descargarla.

```
~/.m2/repository/
├── com/
│   └── google/
│       └── code/
│           └── gson/
│               └── gson/
│                   └── 2.11.0/
│                       ├── gson-2.11.0.jar        ← el JAR
│                       ├── gson-2.11.0.pom        ← su pom.xml
│                       └── _remote.repositories
├── org/
│   └── junit/
│       └── jupiter/
│           └── ...
```

### ¿Cómo funciona la descarga?

```
Tu pom.xml dice: "Necesito gson:2.11.0"
         │
         ▼
Maven busca en ~/.m2/repository/
         │
    ¿Lo tiene?
    │         │
   SÍ         NO
    │         │
  Usa el     Descarga de Maven Central
  local      (repo1.maven.org)
             y lo guarda en ~/.m2/
```

### Ejercicio: explorar el repositorio local

```bash
# Ver qué hay descargado
ls ~/.m2/repository/com/google/code/gson/gson/

# Ver el tamaño total del repositorio
du -sh ~/.m2/repository/
```

---

# ═══════════════════════════════════════════════════════
# PARTE IV — EJERCICIO INTEGRADOR
# ═══════════════════════════════════════════════════════

---

# EJERCICIO INTEGRADOR — mi-biblioteca

Creen un proyecto Maven nuevo llamado `mi-biblioteca` que tenga:

1. **groupId:** `com.cursojava` / **artifactId:** `mi-biblioteca`
2. **Dependencias:**
   - Gson (`compile`)
   - JUnit 5 (`test`)
   - H2 Database (`runtime`, base de datos en memoria para desarrollo/tests) — versión 2.2.224
3. **Plugins:**
   - `maven-compiler-plugin` configurado para Java 17
   - `maven-jar-plugin` con mainClass `com.cursojava.BibliotecaApp`
4. **Una clase `BibliotecaApp.java`** que:
   - Cree una lista de 3 libros (titulo, autor, precio)
   - Los exporte a JSON con Gson
   - Imprima el JSON por consola
5. **Un test `BibliotecaAppTest.java`** que:
   - Verifique que la lista tiene 3 libros
   - Verifique que ningún precio es negativo

**GPS Arquitectónico — piensen la estructura antes de escribir código:**

```
GPS: "Crear proyecto mi-biblioteca con Maven"

  → ¿Qué tipo de proyecto?         Maven (necesita pom.xml)
  → ¿Dónde va el código fuente?    src/main/java/com/cursojava/
  → ¿Dónde va el modelo (Libro)?   src/main/java/com/cursojava/modelo/
  → ¿Dónde van los tests?          src/test/java/com/cursojava/
  → ¿Qué scopes necesitan?         Gson=compile, JUnit=test, H2=runtime

  ESTRUCTURA:
  mi-biblioteca/
  ├── pom.xml
  ├── src/main/java/com/cursojava/
  │   ├── BibliotecaApp.java
  │   └── modelo/Libro.java
  └── src/test/java/com/cursojava/
      └── BibliotecaAppTest.java
```

Verificar con:

```bash
mvn clean test      # los tests pasan
mvn clean package   # se genera el JAR
java -jar target/mi-biblioteca-1.0-SNAPSHOT.jar   # imprime el JSON
```

El profesor les dará tiempo para intentarlo. Después lo resuelven juntos en clase.

---

# REFERENCIAS

# 4.1 El pom.xml completo de la Pizzería (actualizado)

Después de todo lo que hemos visto hoy, el `pom.xml` de la Pizzería debería verse así:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.pizzeria</groupId>
    <artifactId>pizzeria</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Gson: convertir objetos a JSON -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.11.0</version>
        </dependency>

        <!-- JUnit 5: tests automaticos -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Compilador Java 17 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>

            <!-- JAR ejecutable -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.4.1</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.pizzeria.PizzeriaApp</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <profiles>
        <profile>
            <id>dev</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <dependencies>
                <dependency>
                    <groupId>com.h2database</groupId>
                    <artifactId>h2</artifactId>
                    <version>2.2.224</version>
                </dependency>
            </dependencies>
        </profile>
        <profile>
            <id>prod</id>
            <dependencies>
                <dependency>
                    <groupId>org.postgresql</groupId>
                    <artifactId>postgresql</artifactId>
                    <version>42.7.3</version>
                </dependency>
            </dependencies>
        </profile>
    </profiles>
</project>
```

---

# 4.2 Cheat sheet de Maven

| Comando | Qué hace |
|---------|----------|
| `mvn clean` | Borra `target/` |
| `mvn compile` | Compila el código |
| `mvn test` | Compila + ejecuta tests |
| `mvn package` | Compila + tests + crea JAR |
| `mvn install` | Todo + copia JAR a `~/.m2` |
| `mvn clean package` | Limpia y reconstruye |
| `mvn clean package -Pprod` | Reconstruye con perfil prod |
| `mvn dependency:tree` | Muestra árbol de dependencias |
| `mvn help:active-profiles` | Muestra perfil activo |

| Concepto | Qué es |
|----------|--------|
| **Scope** | Cuándo se usa una dependencia (compile, test, provided, runtime) |
| **Plugin** | Extensión que ejecuta una tarea en una fase del ciclo |
| **Perfil** | Configuración alternativa para diferentes entornos |
| **Repositorio local** | `~/.m2/repository/` — caché de JARs descargados |
| **Ciclo de vida** | Secuencia de fases: compile → test → package → install |

---

# 4.3 Para profundizar (referencias)

Si quieren ir más allá de lo que vimos en clase:

**Maven:**
- [Maven — Guía oficial de inicio](https://maven.apache.org/guides/getting-started/) — La documentación oficial. Seca pero completa.
- [Maven in 5 Minutes](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html) — Tutorial rápido oficial.
- [Baeldung — Maven Tutorial](https://www.baeldung.com/maven) — Explicaciones claras con ejemplos prácticos.
- [Maven: The Definitive Guide (Sonatype)](https://books.sonatype.com/mvnref-book/reference/) — Libro gratuito online, muy completo.

**JUnit 5:**
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/) — Documentación oficial. La sección "Writing Tests" es la más útil.
- [Baeldung — JUnit 5 Guide](https://www.baeldung.com/junit-5) — Tutorial paso a paso con ejemplos.
- [AssertJ](https://assertj.github.io/doc/) — Librería alternativa de asserts, más legible. La verán en proyectos reales.

**Testing en general:**
- [Martin Fowler — Testing](https://martinfowler.com/testing/) — Artículos del creador de muchos patrones de testing.

---

# ═══════════════════════════════════════════════════════
# PARTE V — CIERRE DEL DÍA
# ═══════════════════════════════════════════════════════

## Resumen: lo que lograron hoy

```
╔══════════════════════════════════════════════════════════════════╗
║                    QUÉ LOGRARON HOY (DÍA 8)                      ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  SCOPES              Saben cuándo incluir cada dependencia       ║
║  ──────              compile / test / provided / runtime         ║
║                                                                  ║
║  CICLO DE VIDA       Entienden validate→compile→test→package     ║
║  ────────────        Cada fase ejecuta las anteriores            ║
║                                                                  ║
║  PLUGINS             Configuraron compiler + jar ejecutable      ║
║  ───────             maven-compiler-plugin, maven-jar-plugin     ║
║                                                                  ║
║  JUNIT 5             Escribieron tests automáticos               ║
║  ──────              @Test, @BeforeEach, asserts                 ║
║                                                                  ║
║  PERFILES            Configuración por entorno (dev/prod)        ║
║  ────────            -Pdev, -Pprod, activeByDefault              ║
║                                                                  ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  EVOLUCIÓN DE LA PIZZERÍA:                                       ║
║  v1-v3:  javac manual → ArrayList → se pierde al cerrar         ║
║  v4:     Maven + Gson → ArrayList → se pierde al cerrar         ║
║  v4+:    Maven + JUnit → Tests automáticos  ← ESTÁN AQUÍ       ║
║  v5:     Maven + Hibernate → BASE DE DATOS  ← MAÑANA           ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝
```

## Commit del día

Guarden su trabajo antes de irse:

```bash
cd Pizzeria_Alumno
git add pom.xml
git add src/test/java/com/pizzeria/servicio/PedidoServiceTest.java
git commit -m "v4.1: plugins, tests JUnit 5 y perfiles Maven (dia 8)"
```

Verificar:

```bash
git log --oneline -3
```

