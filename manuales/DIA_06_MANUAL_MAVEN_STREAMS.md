# Maven y Streams: Las Herramientas que les Faltan

La diferencia entre un programador junior y un programador profesional no es cuánto código sabe escribir — es cuántas herramientas sabe usar, y cuándo usar cada una. Hoy van a adquirir dos que van a usar TODOS los días de su carrera.

Prof. Juan Marcelo Gutierrez Miranda

**Curso IFCD0014 — Semana 2, Día 6 (Lunes)**
**Objetivo:** Dominar Maven para gestionar proyectos y Streams para procesar datos. Aprender a decidir DÓNDE va cada cambio en la arquitectura.

> Este manual es de consulta. Léanlo con el proyecto abierto al lado. Cada ejemplo usa código real de nuestra Pizzería.

---

# PARTE I — MAVEN

# 1. El Problema: ¿Por Qué Existe Maven?

Hasta ahora compilamos nuestro proyecto con este comando:

```bash
javac -d out src/com/pizzeria/modelo/*.java src/com/pizzeria/excepcion/*.java
      src/com/pizzeria/repositorio/*.java src/com/pizzeria/servicio/*.java
      src/com/pizzeria/PizzeriaApp.java
```

Eso funciona. Pero tiene 3 problemas graves:

## Problema 1: Las dependencias

Imaginen que el jefe dice "quiero exportar los pedidos a formato JSON". Para eso necesitan una librería externa: **Gson** (de Google).

```
Sin Maven:
  1. Buscar Gson en internet
  2. Descargar el archivo gson-2.11.0.jar
  3. Ponerlo en alguna carpeta del proyecto
  4. Agregarlo al classpath manualmente
  5. ¿Y si Gson necesita OTRA librería? Repetir 1-4 para esa también
  6. ¿Y en la máquina de un compañero? Repetir TODO
```

Ahora multipliquen eso por 20 librerías (que es lo normal en un proyecto real con Spring, Hibernate, Jackson, Lombok, etc.). Eso se llama el **infierno de las dependencias**.

```
EL INFIERNO DE LAS DEPENDENCIAS (sin Maven):

  Mi proyecto necesita → Gson 2.10
  Gson 2.10 necesita → annotations-api 1.3
  annotations-api 1.3 necesita → javax.inject 1.0
  javax.inject 1.0 necesita → ???

  ¿Quién descarga todo eso? ¿Quién verifica versiones? ¿Quién lo hace
  en la máquina de los otros 4 programadores del equipo?

  NADIE quiere hacer eso a mano. Por eso existe Maven.
```

## Problema 2: La estructura

Cada programador organiza su proyecto diferente. Unos ponen el código en `src/`, otros en `code/`, otros en `app/`. Cuando llegas a un equipo nuevo, no sabes dónde está nada.

## Problema 3: La compilación

El comando `javac` de arriba solo funciona para NUESTRO proyecto. Cada proyecto tiene su propio comando. No hay un estándar.

---

# 2. La Solución: Maven en 5 Minutos

Maven resuelve los 3 problemas con un solo archivo: el `pom.xml`.

```
╔════════════════════════════════════════════════════════════╗
║                    MAVEN RESUELVE:                         ║
╠════════════════════════════════════════════════════════════╣
║                                                            ║
║  Problema 1 → DEPENDENCIAS                                ║
║  Las declaro en el pom.xml.                                ║
║  Maven las descarga automáticamente.                       ║
║  Y descarga las dependencias DE las dependencias.          ║
║                                                            ║
║  Problema 2 → ESTRUCTURA ESTÁNDAR                          ║
║  Todos los proyectos Maven tienen la misma estructura.     ║
║  Si sabes uno, sabes todos.                                ║
║                                                            ║
║  Problema 3 → COMANDOS UNIVERSALES                         ║
║  mvn compile → compila cualquier proyecto Maven            ║
║  mvn test    → ejecuta los tests                           ║
║  mvn package → genera un .jar distribuible                 ║
║                                                            ║
╚════════════════════════════════════════════════════════════╝
```

## La estructura Maven vs lo que teníamos

```
NUESTRO PROYECTO (sin Maven):        PROYECTO MAVEN:

Pizzeria_Alumno/                      Pizzeria_Alumno/
├── src/                              ├── src/
│   └── com/pizzeria/                 │   └── main/
│       ├── modelo/                   │       ├── java/           ← código
│       ├── repositorio/              │       │   └── com/pizzeria/
│       ├── servicio/                 │       │       ├── modelo/
│       ├── excepcion/                │       │       ├── repositorio/
│       └── PizzeriaApp.java         │       │       ├── servicio/
├── out/        ← compilados          │       │       ├── excepcion/
└── .gitignore                        │       │       └── PizzeriaApp.java
                                      │       └── resources/     ← configuraciones
                                      ├── pom.xml                ← EL CORAZÓN
                                      ├── target/  ← compilados (auto)
                                      └── .gitignore

Diferencias:
  src/           → src/main/java/     (el código baja un nivel)
  out/           → target/            (Maven compila aquí)
  (no existía)   → pom.xml            (el archivo que define TODO)
  (no existía)   → resources/         (para configuraciones futuras)
```

La diferencia principal: **el código se mueve de `src/` a `src/main/java/`**. Todo lo demás sigue igual. Los paquetes no cambian, los imports no cambian, la lógica no cambia.

---

# 3. El pom.xml: El DNI del Proyecto

POM significa **Project Object Model**. Es un archivo XML que le dice a Maven todo sobre el proyecto.

```
┌─────────────────────────────────────────────────────────┐
│                     pom.xml                             │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │  CABECERA XML + NAMESPACE                       │    │
│  │  (<?xml ...> y <project ...>)                   │    │
│  │  → Boilerplate. Siempre igual. Copiar y pegar.  │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │  COORDENADAS (la identidad del proyecto)        │    │
│  │  → groupId + artifactId + version               │    │
│  │  → Como el DNI: unicas en el mundo              │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │  PROPERTIES (configuracion)                     │    │
│  │  → Version de Java, encoding, variables         │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │  DEPENDENCIES (la lista de la compra)           │    │
│  │  → Librerias externas que el proyecto necesita  │    │
│  │  → Maven las descarga automaticamente           │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │  BUILD (opcional, avanzado)                     │    │
│  │  → Plugins, configuracion de compilacion        │    │
│  │  → No lo tocamos hoy                            │    │
│  └─────────────────────────────────────────────────┘    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

Veamos cada sección del pom.xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>

    <modelVersion>4.0.0</modelVersion>

    <!-- SECCIÓN 1: ¿QUIÉN SOY? -->
    <groupId>com.pizzeria</groupId>         <!-- El "apellido" -->
    <artifactId>pizzeria</artifactId>       <!-- El "nombre" -->
    <version>3.1-SNAPSHOT</version>         <!-- La versión actual -->

    <!-- SECCIÓN 2: ¿CON QUÉ COMPILO? -->
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <!-- SECCIÓN 3: ¿QUÉ LIBRERÍAS NECESITO? -->
    

</project>
```

## Las 3 coordenadas

```
LAS 3 COORDENADAS MAVEN (obligatorias):

  groupId    = "com.curso"         → La organizacion (como el apellido)
                                     Convencion: dominio invertido
                                     Google usa "com.google", Apache usa "org.apache"

  artifactId = "hola-maven"       → El nombre del proyecto (como el nombre de pila)
                                     En minusculas, separado por guiones

  version    = "1.0-SNAPSHOT"     → La version actual
                                     SNAPSHOT = "en desarrollo, no es final"
                                     Sin SNAPSHOT (1.0, 2.1.3) = version liberada

  Las 3 juntas forman una "direccion unica":
  com.curso:hola-maven:1.0-SNAPSHOT

  Esto es lo que escriben en <dependency> cuando OTRO proyecto
  quiere usar el de ustedes.
```



Cada librería Java del mundo se identifica con 3 valores, como las coordenadas GPS de un lugar:

```
╔══════════════════════════════════════════════════════════════╗
║  groupId     = com.google.code.gson     (quién la creó)     ║
║  artifactId  = gson                     (cómo se llama)     ║
║  version     = 2.11.0                   (qué versión)       ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Con estas 3 coordenadas, Maven sabe EXACTAMENTE qué        ║
║  descargar de Maven Central (el "app store" de Java).        ║
║                                                              ║
║  Es como una dirección:                                      ║
║  País: com.google.code.gson                                  ║
║  Ciudad: gson                                                ║
║  Número: 2.11.0                                              ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

```
PROPERTIES ESENCIALES:

  maven.compiler.source  = 17   → "Compila con sintaxis de Java 17"
  maven.compiler.target  = 17   → "Genera bytecode para Java 17"
  project.build.sourceEncoding = UTF-8  → "Los archivos estan en UTF-8"
                                          (sin esto, el € de la Pizzeria se rompe)

  Regla simple: source y target = la version de Java que tengan instalada.
  Si tienen Java 21, pongan 21. Si tienen 17, pongan 17.
```

```
Documentacion oficial de Maven:

  Referencia completa del POM:
  https://maven.apache.org/pom.html

  Introduccion al POM (mas didactico):
  https://maven.apache.org/guides/introduction/introduction-to-the-pom.html

  Maven en 5 minutos:
  https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html
```



## El repositorio local: ~/.m2

Cuando Maven descarga una librería, la guarda en una carpeta oculta de su sistema:

```
~/.m2/repository/
├── com/
│   └── google/
│       └── code/
│           └── gson/
│               └── gson/
│                   └── 2.11.0/
│                       ├── gson-2.11.0.jar       ← la librería
│                       ├── gson-2.11.0.pom       ← sus propias dependencias
│                       └── gson-2.11.0.jar.sha1  ← verificación
```

La primera vez descarga de internet. La siguiente vez usa la copia local. **Todos los proyectos Maven de su máquina comparten este almacén.** Si 5 proyectos usan Gson 2.11.0, solo hay una copia.

---

# 4. Los Comandos Maven

Solo necesitan 4 comandos. Los demás son para casos avanzados.

```
╔═══════════════╦══════════════════════════════════════════════╗
║  Comando       ║  Qué hace                                    ║
╠═══════════════╬══════════════════════════════════════════════╣
║  mvn compile   ║  Compila el código. Resultado en target/     ║
║                ║  Equivale a nuestro javac -d out ...          ║
╠═══════════════╬══════════════════════════════════════════════╣
║  mvn test      ║  Ejecuta los tests automáticos               ║
║                ║  (los que están en src/test/java/)            ║
╠═══════════════╬══════════════════════════════════════════════╣
║  mvn package   ║  Crea un .jar en target/                     ║
║                ║  Es el archivo que se despliega en producción ║
╠═══════════════╬══════════════════════════════════════════════╣
║  mvn clean     ║  Borra la carpeta target/ entera             ║
║                ║  Útil cuando algo no compila y no sabes por qué║
╚═══════════════╩══════════════════════════════════════════════╝
```

**Consejo:** Si algo no compila y no entienden por qué, ejecuten `mvn clean compile`. El `clean` borra todo lo compilado y empieza de cero. Resuelve el 80% de los problemas raros.

---

# 5. Maven en IntelliJ IDEA

IntelliJ tiene integración directa con Maven. Cuando abren un proyecto con `pom.xml`, IntelliJ lo reconoce automáticamente.

### Crear un proyecto Maven desde IntelliJ

```
En la ventana "New Project" (File → New → Project...):

  Name:           HolaMaven
  Location:       (dejen el default o elijan su escritorio)
  Language:       Java
  Build system:   Maven        ← ESTO ES LO IMPORTANTE
  JDK:            17
  GroupId:        com.curso
  ArtifactId:     hola-maven

  [Create]
```

### El panel Maven

```
PANEL MAVEN (barra lateral derecha):

  📁 pizzeria
  ├── Lifecycle
  │   ├── clean
  │   ├── validate
  │   ├── compile      ← doble clic = mvn compile
  │   ├── test
  │   ├── package      ← doble clic = mvn package
  │   ├── verify
  │   ├── install
  │   └── deploy
  ├── Plugins
  └── Dependencies
      └── com.google.code.gson:gson:2.11.0    ← aquí ven las librerías
```

### El icono del elefante 🐘

Cada vez que modifican el `pom.xml` (agregar una dependencia, cambiar una versión), deben **recargar Maven**. IntelliJ muestra un icono de un elefante pequeño arriba a la derecha, o una notificación "Load Maven Changes".

**Si no recargan, IntelliJ no sabe que algo cambió.** Este es el error más común con Maven en IntelliJ.

Atajo: `Ctrl + Shift + O` (en versiones recientes de IntelliJ).

---

# 6. De javac a Maven: Lo que Cambia y lo que NO

```
╔══════════════════════════════════════════════════════════════╗
║              ¿QUÉ CAMBIA CON MAVEN?                          ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  CAMBIA:                                                     ║
║  ✓ La estructura de carpetas (src/ → src/main/java/)         ║
║  ✓ El comando de compilación (javac → mvn compile)           ║
║  ✓ Dónde van los .class (out/ → target/)                     ║
║  ✓ Cómo se agregan librerías (JAR manual → pom.xml)          ║
║                                                              ║
║  NO CAMBIA:                                                  ║
║  ✗ Los paquetes (com.pizzeria.modelo sigue igual)            ║
║  ✗ Los imports (import com.pizzeria.modelo.* sigue igual)    ║
║  ✗ La lógica de negocio (PedidoService sigue igual)          ║
║  ✗ La arquitectura por capas (modelo/repo/servicio)          ║
║  ✗ Las interfaces y clases                                   ║
║  ✗ Los Streams y lambdas                                     ║
║  ✗ NADA del código Java que escribieron                      ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

Maven es una herramienta de **gestión**, no de programación. No cambia CÓMO programan — cambia cómo ORGANIZAN y COMPILAN el proyecto.

---

# PARTE II — STREAMS

# 7. El Problema: Los Bucles `for` No Escalan

Miren este método que ya tienen en su proyecto:

**📁** `PedidoService.java` — método `generarResumen()`:

```java
public String generarResumen() {
    // 1. Obtener todos los pedidos del repositorio (HashMap en memoria)
    List<Pedido> todos = repositorio.buscarTodos();

    // 2. Guard clause: si no hay pedidos, salir rapido
    if (todos.isEmpty()) {
        return "No hay pedidos registrados.";
    }

    // 3. Variables manuales: una para acumular, otra para comparar
    double totalVentas = 0;                // acumulador manual
    Pedido pedidoMayor = todos.get(0);     // asume que el primero es el mayor

    // 4. Bucle for-each: recorre UNO POR UNO haciendo TODO dentro
    for (Pedido p : todos) {
        double totalPedido = p.calcularTotal();  // calcula el total de este pedido
        totalVentas += totalPedido;              // suma al acumulador

        // compara para encontrar el mayor
        if (totalPedido > pedidoMayor.calcularTotal()) {
            pedidoMayor = p;
        }
    }

    // 5. Construir el resultado con String.format
    return String.format(
        "=== RESUMEN DE VENTAS ===\n" +
        "Total pedidos: %d\n" +
        "Total ventas: %.2f EUR\n" +
        "Pedido mayor: #%d (%.2f EUR)",
        todos.size(),
        totalVentas,
        pedidoMayor.getNumero(),
        pedidoMayor.calcularTotal()
    );
}
```

Este código funciona. Pero miren todo lo que hace ese `for` solito:

```
┌─────────────────────────────────────────────────────────────┐
│  ¿QUÉ HACE EL FOR DE generarResumen()?                     │
│                                                             │
│  Línea 1:  double totalPedido = p.calcularTotal()  → CALCULA│
│  Línea 2:  totalVentas += totalPedido              → SUMA   │
│  Línea 3:  if (totalPedido > pedidoMayor...)       → COMPARA│
│  Línea 4:  pedidoMayor = p                         → GUARDA │
│                                                             │
│  Son 4 responsabilidades en UN solo bucle.                  │
│  Funciona... pero ¿qué pasa si el jefe pide más?           │
└─────────────────────────────────────────────────────────────┘

  REQUISITO NUEVO                    │  QUÉ HAY QUE AGREGAR AL FOR
  ───────────────────────────────────┼──────────────────────────────
  "Dame el promedio"                 │  + 1 variable + 1 division
  "Agrupa por cliente"               │  + 1 Map + if/else dentro
  "Ordena del mas caro al barato"    │  + Collections.sort() aparte
  "Filtra los de mas de 3 items"     │  + 1 if mas dentro del for

  Cada requisito nuevo = mas variables, mas if, mas logica
  dentro del MISMO bucle.

  Resultado: UNA MARAÑA que nadie quiere leer ni mantener.
```

**Streams es otra forma de pensar.**

---

# 8. ¿Qué es un Stream?

```
¿QUÉ ES UN STREAM?

  Un Stream es una SECUENCIA DE DATOS que pasa por una cadena
  de operaciones. No es una coleccion, no guarda datos — solo
  los PROCESA.

  Piensen en una cadena de montaje de una fabrica:

    Materia prima → Operacion 1 → Operacion 2 → Resultado final
    (la lista)      (filtrar)     (transformar)   (lo que quiero)

  Ustedes NO tocan los datos uno por uno.
  Ustedes DESCRIBEN las operaciones y Java las ejecuta.
```

---

# 9. La Idea: Describir en Vez de Hacer

La diferencia fundamental entre un `for` y un Stream:

```
DOS FORMAS DE PENSAR:

  IMPERATIVO (con for):
    "Recorre la lista, mira cada elemento, si cumple la condicion
     guardalo en otra lista, despues ordena esa lista, despues
     saca el primero..."
    → Ustedes dicen CÓMO hacerlo, paso a paso.

  DECLARATIVO (con streams):
    "De la lista, filtra los que cumplan, ordena, dame el primero."
    → Ustedes dicen QUÉ quieren. Java decide el cómo.

  Es como pedir comida:
    Imperativo = ir al super, comprar ingredientes, cocinar, servir
    Declarativo = "Quiero una pizza margarita, por favor"
```

```
╔══════════════════════════════════════════════════════════════╗
║                                                              ║
║  FOR LOOP = Ustedes entran al almacén y hacen todo a mano    ║
║                                                              ║
║    Pedido 1 → lo miro → ¿mayor? → sumo → pongo a un lado   ║
║    Pedido 2 → lo miro → ¿mayor? → sumo → pongo a un lado   ║
║    Pedido 3 → lo miro → ...                                  ║
║                                                              ║
║    Ustedes HACEN el trabajo paso a paso.                      ║
║    Ustedes controlan el índice, las variables, el flujo.     ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  STREAM = Una cadena de montaje donde dan instrucciones      ║
║                                                              ║
║    "De todos los pedidos..."                   .stream()     ║
║    "...quédate con los que superan 20€..."     .filter()     ║
║    "...saca el total de cada uno..."           .mapToDouble() ║
║    "...y dime la suma."                        .sum()        ║
║                                                              ║
║    Ustedes DESCRIBEN lo que quieren.                          ║
║    Java se encarga de cómo hacerlo.                          ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# 10. ¿Cuándo usar Streams y cuándo un `for`?

```
┌─────────────────────────────────────────────────────────────┐
│  ¿CUÁNDO USAR STREAMS?                                     │
│                                                             │
│  USA STREAM cuando:                                         │
│  ✓ Filtrar datos     → "dame solo los pedidos de hoy"       │
│  ✓ Transformar datos → "de cada pedido, saca solo el total" │
│  ✓ Agrupar datos     → "agrupa pedidos por cliente"         │
│  ✓ Buscar datos      → "encuentra el pedido mas caro"       │
│  ✓ Resumir datos     → "suma todos los totales"             │
│  ✓ Combinar varias de las anteriores en una sola operacion  │
│                                                             │
│  USA FOR cuando:                                            │
│  ✓ Modificar los elementos originales (streams no modifica) │
│  ✓ Necesitas break o continue con logica compleja           │
│  ✓ El bucle es simple y corto (2-3 lineas, no vale la pena)│
│  ✓ Necesitas acceder al indice (i = 0, 1, 2...)            │
└─────────────────────────────────────────────────────────────┘
```

```
EJEMPLOS REALES DONDE SE USAN STREAMS:

  E-COMMERCE (como nuestra Pizzería):

  APLICACIONES BANCARIAS:

  REDES SOCIALES:

  APIs REST (lo que van a hacer con Spring):


  REGLA: Si tienen una LISTA y necesitan FILTRAR, TRANSFORMAR
  o RESUMIR los datos → Stream es la herramienta correcta.
```

---

# 11. La Analogía: La Cadena de Montaje

```
  MATERIA PRIMA          OPERACIONES              RESULTADO
  ┌──────────┐                                    ┌──────────┐
  │ Pedido 1 │──┐                                 │          │
  │ Pedido 2 │──┤    ┌─────────┐  ┌─────────┐    │  Lista   │
  │ Pedido 3 │──┼───→│ FILTRAR │─→│ ORDENAR │───→│   de 2   │
  │ Pedido 4 │──┤    │ (>20€)  │  │ (↓ $$$) │    │ pedidos  │
  │ Pedido 5 │──┘    └─────────┘  └─────────┘    │          │
  └──────────┘                                    └──────────┘
     .stream()        .filter()     .sorted()     .collect()

    FASE 1:           FASE 2:                     FASE 3:
    CREAR             OPERAR                      CERRAR
    (abrir grifo)     (transformar)               (recoger resultado)
```

Los datos fluyen por la cinta. En cada estación se aplica una operación. Al final sale el resultado.

---

# 12. Las Tres Fases de un Stream

```
╔══════════════╦════════════════════════╦═══════════════════════════╗
║  FASE        ║  QUÉ HACE              ║  EJEMPLOS                 ║
╠══════════════╬════════════════════════╬═══════════════════════════╣
║              ║                        ║                           ║
║  1. CREAR    ║  Poner los datos en    ║  lista.stream()           ║
║  (1 vez)     ║  la cinta              ║  Stream.of("a", "b")      ║
║              ║                        ║  mapa.values().stream()   ║
║              ║                        ║                           ║
╠══════════════╬════════════════════════╬═══════════════════════════╣
║              ║                        ║                           ║
║  2. OPERAR   ║  Transformar los       ║  .filter()  → descartar   ║
║  (0 o más)   ║  datos en la cinta     ║  .map()     → transformar ║
║              ║                        ║  .sorted()  → ordenar     ║
║              ║  ⚠ NO ejecuta nada     ║  .distinct()→ sin duplicados║
║              ║  hasta la fase 3       ║  .limit()   → primeros N  ║
║              ║                        ║                           ║
╠══════════════╬════════════════════════╬═══════════════════════════╣
║              ║                        ║                           ║
║  3. CERRAR   ║  Recoger el resultado  ║  .collect() → crear lista ║
║  (exacta-    ║                        ║  .count()   → contar      ║
║   mente 1)   ║  ⚠ AQUÍ se ejecuta    ║  .sum()     → sumar       ║
║              ║  todo el pipeline      ║  .forEach() → imprimir    ║
║              ║                        ║  .max()     → el mayor    ║
║              ║                        ║  .average() → promedio    ║
║              ║                        ║                           ║
╚══════════════╩════════════════════════╩═══════════════════════════╝
```

### La regla de oro

**Sin fase 3 (operación terminal), el Stream NO hace NADA.** Es como poner la cinta transportadora sin encender el motor al final.

```java
// ESTO no hace absolutamente nada:
pedidos.stream()
       .filter(p -> p.calcularTotal() > 20);
// Falta la operación terminal. El filtro se define pero nunca se ejecuta.

// ESTO sí funciona:
long cantidad = pedidos.stream()
       .filter(p -> p.calcularTotal() > 20)
       .count();  // ← operación terminal: ahora SÍ se ejecuta todo
```

---

# 13. Lambdas: La Pieza que Falta

## La idea

```java
// Lambda completa:
(Pedido p) -> p.calcularTotal()

// Se lee: "dado un Pedido p, devuelve su total"

// Java infiere el tipo, así que se puede acortar:
p -> p.calcularTotal()

// Y si solo llaman a un método del objeto, existe la forma aún más corta:
Pedido::calcularTotal

// Las tres formas son IDÉNTICAS. Usen la que les resulte más legible.
```

## Comparación visual

```java
// ANTES (clase anónima — lo que quizá conocen):
lista.sort(new Comparator<Pedido>() {
    @Override
    public int compare(Pedido a, Pedido b) {
        return Double.compare(a.calcularTotal(), b.calcularTotal());
    }
});

// DESPUÉS (lambda):
lista.sort((a, b) -> Double.compare(a.calcularTotal(), b.calcularTotal()));

// AÚN MÁS CORTO (method reference):
lista.sort(Comparator.comparingDouble(Pedido::calcularTotal));
```

---

# 14. Streams en Nuestra Pizzería: Casos Reales

Antes de reescribir código, siempre hay que preguntarse: ¿en qué archivo va este cambio?

```
╔═══════════════════════════════════════════════════════════╗
║  GPS ARQUITECTÓNICO: ¿Dónde va este cambio?               ║
╠═══════════════════════════════════════════════════════════╣
║                                                           ║
║  Pregunta: "Quiero reescribir la lógica de resumen"       ║
║                                                           ║
║  → ¿Es una entidad nueva? NO                              ║
║  → ¿Es una consulta nueva a los datos? NO                 ║
║  → ¿Es lógica de negocio que ya existe? SÍ                ║
║                                                           ║
║  RESPUESTA: SERVICIO → PedidoService.java                 ║
║  Método existente: generarResumen() (línea 150)           ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝
```

## Caso 1: Total de ventas

```java
// CON FOR:
double total = 0;
for (Pedido p : todos) {
    total += p.calcularTotal();
}

// CON STREAM:
double total = todos.stream()
        .mapToDouble(Pedido::calcularTotal)
        .sum();
```

## Caso 2: Promedio por pedido

```java
// CON FOR:
double suma = 0;
for (Pedido p : todos) {
    suma += p.calcularTotal();
}
double promedio = suma / todos.size();

// CON STREAM:
OptionalDouble promedio = todos.stream()
        .mapToDouble(Pedido::calcularTotal)
        .average();
```

## Caso 3: Pedido más caro

```java
// CON FOR:
Pedido mayor = todos.get(0);
for (Pedido p : todos) {
    if (p.calcularTotal() > mayor.calcularTotal()) {
        mayor = p;
    }
}

// CON STREAM:
Pedido mayor = todos.stream()
        .max(Comparator.comparingDouble(Pedido::calcularTotal))
        .orElseThrow();
```

## Caso 4: Filtrar pedidos caros, ordenados

```
GPS: "Buscar pedidos que superan un monto"

  → ¿Es una entidad nueva?               NO
  → ¿Es una consulta nueva a los datos?   SÍ → empieza en REPOSITORIO
  → ¿Necesita validación de negocio?       SÍ (monto no negativo) → SERVICIO también

  PASO 1: PedidoRepository.java         (interfaz — declarar el método)
  PASO 2: PedidoRepositoryMemoria.java   (implementación — escribir el stream)
  PASO 3: PedidoService.java            (validar + delegar)
  PASO 4: PizzeriaApp.java              (probarlo)
```

```java
// CON FOR:
List<Pedido> caros = new ArrayList<>();
for (Pedido p : todos) {
    if (p.calcularTotal() > 20) {
        caros.add(p);
    }
}
caros.sort((a, b) -> Double.compare(b.calcularTotal(), a.calcularTotal()));

// CON STREAM:
List<Pedido> caros = todos.stream()
        .filter(p -> p.calcularTotal() > 20)
        .sorted(Comparator.comparingDouble(Pedido::calcularTotal).reversed())
        .collect(Collectors.toList());
```

## Caso 5: Agrupar pedidos por cliente

```java
// CON FOR:
Map<String, List<Pedido>> porCliente = new HashMap<>();
for (Pedido p : todos) {
    String nombre = p.getNombreCliente();
    if (!porCliente.containsKey(nombre)) {
        porCliente.put(nombre, new ArrayList<>());
    }
    porCliente.get(nombre).add(p);
}

// CON STREAM:
Map<String, List<Pedido>> porCliente = todos.stream()
        .collect(Collectors.groupingBy(Pedido::getNombreCliente));
```

## Caso 6: Todo a la vez con DoubleSummaryStatistics

```java
DoubleSummaryStatistics stats = todos.stream()
        .mapToDouble(Pedido::calcularTotal)
        .summaryStatistics();

System.out.println("Pedidos: " + stats.getCount());
System.out.println("Total:   " + stats.getSum() + "€");
System.out.println("Promedio:" + stats.getAverage() + "€");
System.out.println("Mínimo:  " + stats.getMin() + "€");
System.out.println("Máximo:  " + stats.getMax() + "€");
```

## Comparación: ANTES vs AHORA

Cuando reescriben `generarResumen()` con Streams, el cambio se ve así:

```
ANTES (for loop):                      AHORA (Streams):

double totalVentas = 0;               DoubleSummaryStatistics stats =
Pedido pedidoMayor = todos.get(0);         todos.stream()
for (Pedido p : todos) {                       .mapToDouble(Pedido::calcularTotal)
    double t = p.calcularTotal();              .summaryStatistics();
    totalVentas += t;
    if (t > pedidoMayor...) {          Pedido pedidoMayor = todos.stream()
        pedidoMayor = p;                   .max(...)
    }                                      .orElseThrow();
}

→ 3 variables, 1 loop, 2 operaciones  → 2 streams, cada uno hace UNA cosa
→ Si agrego "promedio", toco el loop   → stats.getAverage() ya lo tiene
→ Difícil de leer con 5+ operaciones  → Cada stream se lee como una frase
```

---

# 15. Diccionario Rápido de Operaciones

## Operaciones intermedias (fase 2 — se pueden encadenar)

```
╔══════════════════╦═════════════════════════════════════════════╗
║  Operación        ║  Qué hace                                   ║
╠══════════════════╬═════════════════════════════════════════════╣
║  .filter(cond)    ║  Quédate solo con los que cumplen cond      ║
║  .map(func)       ║  Transforma cada elemento con func          ║
║  .mapToDouble(f)  ║  Transforma a double (permite sum/avg)      ║
║  .sorted()        ║  Ordena (natural o con Comparator)          ║
║  .distinct()      ║  Elimina duplicados                         ║
║  .limit(n)        ║  Quédate con los primeros n                 ║
║  .skip(n)         ║  Salta los primeros n                       ║
║  .peek(accion)    ║  Ejecuta acción sin modificar (para debug)  ║
╚══════════════════╩═════════════════════════════════════════════╝
```

## Operaciones terminales (fase 3 — exactamente una, al final)

```
╔══════════════════════════╦═════════════════════════════════════╗
║  Operación                ║  Qué devuelve                       ║
╠══════════════════════════╬═════════════════════════════════════╣
║  .collect(Collectors...)  ║  List, Set, Map, String...          ║
║  .toList()                ║  List (Java 16+)                    ║
║  .count()                 ║  long (cantidad de elementos)       ║
║  .sum()                   ║  double/int (solo en mapToDouble)   ║
║  .average()               ║  OptionalDouble                     ║
║  .min() / .max()          ║  Optional<T>                        ║
║  .forEach(accion)         ║  void (ejecuta acción por elemento) ║
║  .findFirst()             ║  Optional<T> (primer elemento)      ║
║  .anyMatch(cond)          ║  boolean (¿alguno cumple?)          ║
║  .allMatch(cond)          ║  boolean (¿todos cumplen?)          ║
║  .noneMatch(cond)         ║  boolean (¿ninguno cumple?)         ║
║  .summaryStatistics()     ║  count+sum+min+max+avg de una vez   ║
╚══════════════════════════╩═════════════════════════════════════╝
```

---

# 16. `for` vs Stream: ¿Cuándo Usar Cada Uno?

```
╔══════════════════════════════╦═══════════════════════════════════╗
║  USAR FOR CUANDO...           ║  USAR STREAM CUANDO...             ║
╠══════════════════════════════╬═══════════════════════════════════╣
║  Necesitan break/continue     ║  Filtran + transforman + recolectan║
║  Modifican la lista original  ║  Calculan estadísticas (sum, avg)  ║
║  La lógica es muy simple      ║  Agrupan datos (groupingBy)        ║
║  (ej: imprimir 3 cosas)      ║  Encadenan varias operaciones      ║
║  Necesitan el índice (i)      ║  La operación es una "frase"       ║
╚══════════════════════════════╩═══════════════════════════════════╝
```

---

# 17. El Error Más Común: Reusar un Stream

```java
// ⛔ ESTO DA ERROR:
Stream<Pedido> miStream = todos.stream().filter(p -> p.getCantidadItems() > 0);

long cantidad = miStream.count();          // ✓ funciona
double total = miStream.mapToDouble(       // ✗ IllegalStateException!
        Pedido::calcularTotal).sum();      //   "stream has already been operated upon"
```

**Un Stream solo se puede usar UNA VEZ.** Después de la operación terminal (`.count()`), el Stream se cierra. Si necesitan hacer dos cosas diferentes, creen dos Streams:

```java
// ✓ CORRECTO: dos Streams separados
long cantidad = todos.stream()
        .filter(p -> p.getCantidadItems() > 0)
        .count();

double total = todos.stream()
        .filter(p -> p.getCantidadItems() > 0)
        .mapToDouble(Pedido::calcularTotal)
        .sum();
```

O usen `summaryStatistics()` que calcula todo de una vez.

---

# PARTE III — GPS ARQUITECTÓNICO

# 18. ¿Dónde Va Cada Cosa? El Árbol de Decisiones

```
╔══════════════════════════════════════════════════════════════════╗
║           GPS ARQUITECTÓNICO — ¿DÓNDE VA ESTE CAMBIO?            ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  ¿Qué me están pidiendo?                                         ║
║  │                                                               ║
║  ├── ¿Es una COSA nueva que existe en el negocio?                ║
║  │   (producto, entidad, categoría, tipo)                        ║
║  │                                                               ║
║  │   → MODELO: crear clase en modelo/                            ║
║  │                                                               ║
║  │   📁 src/com/pizzeria/modelo/NuevaClase.java                  ║
║  │   Pregúntense: ¿implementa Vendible? ¿Preparable? ¿Ninguna?   ║
║  │   Ejemplo: Combo.java, Cliente.java, TipoCliente.java         ║
║  │                                                               ║
║  ├── ¿Es un ERROR de negocio nuevo?                              ║
║  │   (algo que puede salir mal y tiene nombre propio)            ║
║  │                                                               ║
║  │   → EXCEPCIÓN: crear clase en excepcion/                      ║
║  │                                                               ║
║  │   📁 src/com/pizzeria/excepcion/NuevaExcepcion.java           ║
║  │   Siempre extiende RuntimeException                           ║
║  │   Ejemplo: ClienteNoEncontradoException.java                  ║
║  │                                                               ║
║  ├── ¿Es una forma nueva de BUSCAR o GUARDAR datos?              ║
║  │   (consulta, filtro, búsqueda)                                ║
║  │                                                               ║
║  │   → REPOSITORIO: tocar 2 archivos SIEMPRE                     ║
║  │                                                               ║
║  │   📁 repositorio/PedidoRepository.java       (declarar)       ║
║  │   📁 repositorio/PedidoRepositoryMemoria.java (implementar)   ║
║  │   El servicio NO cambia. El modelo NO cambia.                 ║
║  │   Ejemplo: buscarPedidosCaros(), buscarPorRangoPrecio()       ║
║  │                                                               ║
║  ├── ¿Es una REGLA o LÓGICA que necesita datos?                  ║
║  │   (calcular, validar, generar reportes, cruzar datos)         ║
║  │                                                               ║
║  │   → SERVICIO: agregar método                                  ║
║  │                                                               ║
║  │   📁 servicio/PedidoService.java    (si es de pedidos)        ║
║  │   📁 servicio/ClienteService.java   (si es de clientes)       ║
║  │   ¿Necesita dos repositorios? → Va en el servicio que         ║
║  │   "posee" el resultado (ej: ranking de clientes → ClienteService) ║
║  │   Ejemplo: generarResumen(), aplicarDescuento(), ranking      ║
║  │                                                               ║
║  └── ¿Es solo DEMOSTRAR o USAR lo anterior?                      ║
║                                                                  ║
║      → MAIN: PizzeriaApp.java                                    ║
║                                                                  ║
║      📁 src/com/pizzeria/PizzeriaApp.java                        ║
║      Aquí solo se LLAMAN servicios. NUNCA lógica directa.        ║
║      En Spring, esto se convierte en @RestController.            ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝
```

## La regla de las dependencias

```
  PizzeriaApp (main)
       │
       ▼ usa
  PedidoService / ClienteService (servicio)
       │
       ▼ usa
  PedidoRepository (interfaz)
       │
       ▼ implementada por
  PedidoRepositoryMemoria (implementación)
       │
       ▼ almacena
  Pedido, Pizza, Cliente... (modelo)


  ⚠ NUNCA al revés:
  - El main NUNCA toca el repositorio directamente
  - El servicio NUNCA crea objetos de modelo sin pasar por el repo
  - El repositorio NUNCA valida reglas de negocio
  - El modelo NUNCA sabe de repositorios ni servicios
```

## Ejemplos prácticos — ¿en qué capa va?

**"El jefe quiere guardar facturas"**
→ ¿Qué tipo de cambio es? ¿En qué carpeta iría?

**"Quiero buscar pedidos de los últimos 7 días"**
→ ¿Qué tipo de cambio es? ¿Cuántos archivos hay que tocar?

**"Si un pedido supera 100€, se aplica 5% automáticamente"**
→ ¿Dónde va esta lógica? ¿Por qué no en el modelo?

**"Quiero un error cuando un cliente pida más de 10 items"**
→ ¿Qué dos cosas hay que crear? ¿En qué capas?

**"Quiero ver las estadísticas en pantalla"**
→ ¿Dónde va el código que muestra? ¿Y el que calcula?


---

# Resumen Visual del Día 6

```
╔══════════════════════════════════════════════════════════════════╗
║                HERRAMIENTAS DEL DÍA 6                            ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  MAVEN                           STREAMS                        ║
║  ─────                           ───────                        ║
║  Gestión del proyecto            Procesamiento de datos          ║
║                                                                  ║
║  pom.xml → dependencias          .stream() → abrir              ║
║  mvn compile → compilar          .filter() → descartar           ║
║  mvn package → empaquetar        .map()    → transformar         ║
║  ~/.m2/ → almacén local          .collect()→ recoger             ║
║                                                                  ║
║  ¿Cuándo?                        ¿Cuándo?                       ║
║  SIEMPRE, en TODO proyecto       Cuando procesan colecciones     ║
║  profesional Java                (listas, mapas, sets)           ║
║                                                                  ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  GPS ARQUITECTÓNICO                                              ║
║  ──────────────────                                              ║
║  COSA nueva      → modelo/                                      ║
║  ERROR nuevo     → excepcion/                                    ║
║  BÚSQUEDA nueva  → repositorio/ (interfaz + implementación)     ║
║  LÓGICA nueva    → servicio/                                     ║
║  DEMOSTRAR       → main                                         ║
║                                                                  ║
║  ⚠ Nunca se salten una capa                                     ║
║  ⚠ El main nunca toca el repositorio directamente                ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝
```

---

# Siguiente Día

Mañana (día 7) vamos a:
- Convertir la Pizzería a un proyecto Maven
- Agregar la primera dependencia externa (Gson → JSON)
- Completar los TODOs pendientes con Streams
- Ver el puente hacia Hibernate y Spring

Consulten el manual **DIA_07_MANUAL_MAVEN_PIZZERIA.md** para los pasos del día 7.
