# La Pizzería se Convierte en Proyecto Maven

Hoy ponemos en práctica todo lo que aprendieron ayer. La Pizzería deja de ser un proyecto "artesanal" y se convierte en un proyecto Maven profesional. Además, damos el primer paso hacia Hibernate.

Prof. Juan Marcelo Gutierrez Miranda

**Curso IFCD0014 — Semana 2, **
**Objetivo:** Convertir la Pizzería a Maven, agregar la primera dependencia externa (Gson), completar TODOs con Streams, y entender el puente hacia Hibernate.

> Este manual es de consulta. Sigan los pasos con el proyecto abierto en IntelliJ.

---

# PASO 0 — PONER AL DÍA LA PIZZERÍA (v3.1)

Antes de arrancar con Maven, necesitamos que **todos** tengan el mismo punto de partida.

La Pizzería v3 tenía 8 TODOs pendientes. Si alguno no los completó, la aplicación crashea antes de hacer nada. Aquí está el mapa de lo que faltaba:

```
╔══════════════════════════════════════════════════════════════════════════════════╗
║               LOS 8 TODOs DE LA V3 — ¿POR QUÉ IMPORTAN?                        ║
╠══════════════════╦═══════════════════════════╦═══════════════════════════════════╣
║ TODO             ║ Archivo                   ║ ¿Qué pasa si no lo hicieron?     ║
╠══════════════════╬═══════════════════════════╬═══════════════════════════════════╣
║ Constructor      ║ Cliente.java              ║ CRASH al crear cualquier cliente  ║
║ toString()       ║ Cliente.java              ║ Imprime datos incompletos         ║
║ buscarTodos()    ║ ClienteRepositoryMemoria  ║ CRASH al listar clientes          ║
║ buscarPorNombre()║ ClienteRepositoryMemoria  ║ CRASH al buscar por nombre        ║
║ buscarPorTipo()  ║ ClienteRepositoryMemoria  ║ CRASH al filtrar empresas/personas║
║ eliminar()       ║ ClienteRepositoryMemoria  ║ No se pueden borrar clientes      ║
║ actualizarCateg. ║ ClienteService            ║ CRASH en programa de fidelidad    ║
║ generarInforme() ║ ClienteService            ║ CRASH al pedir informe            ║
╠══════════════════╩═══════════════════════════╩═══════════════════════════════════╣
║                                                                                  ║
║  De los 8, solo 2 necesitaban Streams. Los otros 6 son Java básico.              ║
║  Lean las soluciones y entiéndanlas — no es magia, es lo mismo que ya saben.     ║
║                                                                                  ║
║  IMPORTANTE: El manual del día 6 tiene toda la teoría de Streams.                ║
║  Streams van a aparecer TODO el curso. No necesitan dominarlos hoy,              ║
║  pero sí necesitan seguir practicando por su cuenta con ese manual.              ║
║                                                                                  ║
╚══════════════════════════════════════════════════════════════════════════════════╝
```

## Actualizar el proyecto

El profesor les va a dar la versión **v3.1** con todos los TODOs resueltos. Hagan esto:

**1. Reemplazar los 4 archivos modificados:**

Copien estos archivos de la carpeta `v3.1` a su proyecto, reemplazando los existentes:

```
src/com/pizzeria/modelo/Cliente.java
src/com/pizzeria/repositorio/ClienteRepository.java
src/com/pizzeria/repositorio/ClienteRepositoryMemoria.java
src/com/pizzeria/servicio/ClienteService.java
```

**2. Compilar y verificar:**

```bash
javac -d out -encoding UTF-8 src/com/pizzeria/modelo/*.java src/com/pizzeria/excepcion/*.java src/com/pizzeria/repositorio/*.java src/com/pizzeria/servicio/*.java src/com/pizzeria/PizzeriaApp.java
```

```bash
java -cp out -Dfile.encoding=UTF-8 com.pizzeria.PizzeriaApp
```

Deben ver TODA la salida sin ningún `UnsupportedOperationException`.

**3. Hacer commit:**

```bash
git add -A
git commit -m "v3.1: TODOs de Cliente completados"
```

Una vez hecho esto, todos estamos en el mismo punto y arrancamos con Maven.

---

# PARTE I — CONVERSIÓN A MAVEN

# 1. Convertir la Pizzería a Proyecto Maven

La conversión tiene 3 pasos. No se pierde ningún código — solo se reorganizan las carpetas.

## Paso 1: Mover archivos a la estructura Maven

Maven espera que el código esté en `src/main/java/`. Nosotros tenemos el código en `src/`. Solo hay que mover una carpeta.

Desde la raíz de `Pizzeria_Alumno/`:

En Windows (PowerShell):

```powershell
New-Item -ItemType Directory -Force -Path "src\main\java"
Move-Item -Path "src\com" -Destination "src\main\java\"
New-Item -ItemType Directory -Force -Path "src\main\resources"
```

## Paso 2: Verificar la estructura

```
Pizzeria_Alumno/
├── src/
│   └── main/
│       ├── java/                            ← código Java (ANTES estaba en src/)
│       │   └── com/pizzeria/
│       │       ├── PizzeriaApp.java
│       │       ├── modelo/
│       │       ├── excepcion/
│       │       ├── repositorio/
│       │       └── servicio/
│       └── resources/                       ← NUEVO: para configuraciones
├── pom.xml                                  ← NUEVO: lo creamos ahora
├── .gitignore
└── out/                                     ← VIEJO: ya no lo necesitamos
```

## Paso 3: Crear el pom.xml

Crear el archivo `pom.xml` en la raíz de `Pizzeria_Alumno/`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.pizzeria</groupId>
    <artifactId>pizzeria</artifactId>
    <version>3.1-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Por ahora vacío — agregaremos Gson después -->
    </dependencies>

</project>
```

## Paso 4: Abrir en IntelliJ como proyecto Maven

```
1. File → Open → seleccionen la carpeta Pizzeria_Alumno
2. IntelliJ detecta el pom.xml → "Open as Project" → Sí
3. Si no lo detecta: clic derecho en pom.xml → "Add as Maven Project"
```

## Paso 5: Borrar la carpeta vieja y compilar

```bash
rm -rf out/
mvn compile
```

Si dice `BUILD SUCCESS`, la conversión funcionó. Ahora pueden compilar con `mvn compile` en vez del `javac` monstruoso.

## Paso 6: Actualizar .gitignore

Agregar al `.gitignore`:

```
target/
```

---

## Recordatorio: ¿Qué cambia y qué NO?

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

---

# PARTE II — PRIMERA DEPENDENCIA REAL

# 2. Agregar Gson: Exportar Pedidos a JSON

Gson es una librería de Google para convertir objetos Java a JSON. Esto es exactamente lo que harían en una API REST.

## Paso 1: Dependencia en el pom.xml

Dentro de `<dependencies>` en el `pom.xml`:

```xml
    <dependencies>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.11.0</version>
        </dependency>
    </dependencies>
```

**Recargar Maven:** icono del MAVEN o `Ctrl + Shift + O`.

Verificar que en `External Libraries` aparece `com.google.code.gson:gson:2.11.0`.

## Paso 2: Usar Gson en PedidoService

**📁** `src/main/java/com/pizzeria/servicio/PedidoService.java`

Agregar imports:

```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
```

Agregar método al final de la clase:

```java
    /**
     * Exporta todos los pedidos a formato JSON.
     * Usa Gson (dependencia Maven) + Streams.
     *
     * En Spring: esto no se hace asi. Spring convierte a JSON
     * automaticamente con Jackson cuando devolvemos un objeto
     * desde un @RestController. Pero la idea es la misma.
     */
    public String exportarJSON() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        List<Map<String, Object>> datos = repositorio.buscarTodos().stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("numero", p.getNumero());
                    m.put("cliente", p.getNombreCliente());
                    m.put("items", p.getCantidadItems());
                    m.put("total", p.calcularTotal());
                    return m;
                })
                .collect(Collectors.toList());

        return gson.toJson(datos);
    }
```

## Paso 3: Probarlo en el main

**📁** `src/main/java/com/pizzeria/PizzeriaApp.java`

```java
        // ==========================================================
        // EXPORTAR A JSON (usa Gson — dependencia Maven)
        // ==========================================================

        System.out.println("\n--- EXPORTAR A JSON ---\n");
        System.out.println(pedidoService.exportarJSON());
```

Compilar con `mvn compile` y ejecutar desde IntelliJ.

---

# PARTE III — EL PUENTE HACIA HIBERNATE

# 3. De HashMap a Base de Datos

## Nuestro repositorio hoy

```java
public class PedidoRepositoryMemoria implements PedidoRepository {
    private final Map<Integer, Pedido> datos = new HashMap<>();

    @Override
    public Pedido guardar(Pedido pedido) {
        datos.put(pedido.getNumero(), pedido);
        return pedido;
    }
    // ...
}
```

Cuando cierran la aplicación, los datos desaparecen. En el mundo real, los datos van a una base de datos.

¿Qué tendríamos que cambiar para guardar en PostgreSQL? **SOLO la implementación.** La interfaz `PedidoRepository` NO cambia. El servicio NO cambia. El main NO cambia.

## Lo que hace Hibernate

```
╔══════════════════════════════════════════════════════════════╗
║                    ¿QUÉ HACE HIBERNATE?                      ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  NUESTRO CÓDIGO (hoy):              CON HIBERNATE:           ║
║                                                              ║
║  class PedidoRepositoryMemoria {     @Repository             ║
║      Map<Integer,Pedido> datos;      interface PedidoRepo    ║
║      void guardar(Pedido p) {            extends JpaRepo {   ║
║          datos.put(p.getNum(), p);   }                       ║
║      }                              // ¡Hibernate genera     ║
║  }                                  //  guardar, buscar,     ║
║                                     //  eliminar y TODO!     ║
║  23 LÍNEAS de código                3 LÍNEAS de código       ║
║  Datos en RAM (se pierden)          Datos en PostgreSQL      ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║  PARA HACER ESTA TRANSICIÓN NECESITAN:                       ║
║                                                              ║
║  1. Maven (ya lo tienen ✓)         → para declarar Hibernate ║
║  2. Un pom.xml con las deps        → lo haremos pronto       ║
║  3. Anotar las clases modelo       → @Entity, @Id, @Table    ║
║  4. Un archivo de configuración    → persistence.xml         ║
║                                                              ║
║  La LÓGICA de PedidoService no cambia.                       ║
║  El MODELO (Pizza, Pedido, etc.) casi no cambia.             ║
║  Solo desaparece PedidoRepositoryMemoria.                    ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

## Preview: las anotaciones JPA

```java
// NUESTRO Pedido.java (hoy):          CON HIBERNATE:

public class Pedido {                   @Entity
    private int numero;                 @Table(name = "pedidos")
    private Cliente cliente;            public class Pedido {
    private List<Vendible> items;           @Id @GeneratedValue
                                            private int numero;
    // constructor, getters...              @ManyToOne
                                            private Cliente cliente;
}                                           // getters...
                                        }

// NUESTRO repositorio (hoy):          CON HIBERNATE:

public class PedidoRepoMemoria          @Repository
    implements PedidoRepository {        public interface PedidoRepository
    Map<Integer,Pedido> datos;               extends JpaRepository<Pedido,Integer> {
    // 50 líneas de código...              List<Pedido> findByClienteId(int id);
}                                        }
                                         // 3 líneas. Hibernate hace el resto.
```

# ═══════════════════════════════════════════════════════
# PARTE IV — EJERCICIO INTEGRADOR
# ═══════════════════════════════════════════════════════

---

# EJERCICIO INTEGRADOR — Ranking de Clientes por Gasto

```
╔══════════════════════════════════════════════════════════════╗
║  EJERCICIO: "El jefe quiere buscar los mejores clientes"     ║
║                                                              ║
║  Requisito: Devolver los clientes ordenados por gasto total, ║
║  de mayor a menor. Solo los que tienen al menos 1 pedido.    ║
║                                                              ║
║  Piensen:                                                    ║
║  1. ¿En qué CAPA empieza? ¿Qué archivos tocan?              ║
║  2. ¿Usan for loop o Streams?                                ║
║  3. ¿Necesitan un nuevo tipo de dato o sirve uno existente?  ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

**GPS Arquitectónico — piensen antes de mirar:**

```
GPS: "Buscar mejores clientes por gasto total"

  → ¿Es una entidad nueva?        NO
  → ¿Es una consulta?             Parcialmente — necesita CRUZAR datos
  → ¿Cruza datos de 2 entidades?  SÍ (clientes + pedidos)
  → ¿Dónde se cruzan datos?       SERVICIO (tiene acceso a ambos repos)
  → ¿Qué servicio?                ClienteService (el resultado es sobre clientes)

  ARCHIVO: src/main/java/com/pizzeria/servicio/ClienteService.java
  MÉTODO NUEVO: obtenerRankingClientes()
```

El profesor les dará tiempo para intentarlo. Después lo resuelven juntos en clase.

---

# REFERENCIAS

# 4.1 Streams y Arquitectura: Dónde Usar Streams

```
╔════════════════════╦═══════════════════════════════════════════════╗
║  CAPA              ║  CÓMO SE USAN STREAMS AHÍ                     ║
╠════════════════════╬═══════════════════════════════════════════════╣
║                    ║                                               ║
║  MODELO            ║  Raro. Solo dentro de métodos como            ║
║                    ║  getItemsParaCocina() que filtra una lista    ║
║                    ║  interna del propio objeto.                   ║
║                    ║                                               ║
║  REPOSITORIO       ║  Común. Para filtrar, buscar y ordenar datos  ║
║  (Implementación)  ║  del HashMap. Ej: buscarPorCliente(),         ║
║                    ║  buscarPedidosCaros().                        ║
║                    ║                                               ║
║  SERVICIO          ║  Muy común. Para calcular estadísticas,       ║
║                    ║  generar reportes, cruzar datos de varios     ║
║                    ║  repositorios. Ej: generarResumen(),          ║
║                    ║  obtenerRankingClientes().                    ║
║                    ║                                               ║
║  MAIN              ║  Solo para demostrar. En producción           ║
║                    ║  (Spring), los Streams están en las capas     ║
║                    ║  internas, no en el controller.               ║
║                    ║                                               ║
╚════════════════════╩═══════════════════════════════════════════════╝
```

---

# 4.2 Patrones que Usaron Esta Semana (y Verán en Spring)

```
╔══════════════════════════════════════════════════════════════╗
║         PATRONES QUE VIERON ESTA SEMANA (Y EN SPRING)        ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  1. REPOSITORY PATTERN                                       ║
║     → Interfaz + implementación para acceso a datos          ║
║     → Ustedes: PedidoRepository + PedidoRepositoryMemoria    ║
║     → Spring: @Repository + JpaRepository (auto-generado)    ║
║                                                              ║
║  2. SERVICE LAYER                                            ║
║     → Clase con lógica de negocio                            ║
║     → Ustedes: PedidoService, ClienteService                 ║
║     → Spring: @Service (exactamente igual)                   ║
║                                                              ║
║  3. DEPENDENCY INJECTION                                     ║
║     → El servicio RECIBE sus dependencias por constructor     ║
║     → Ustedes: new PedidoService(repo) en el main            ║
║     → Spring: @Autowired (Spring lo hace automáticamente)    ║
║                                                              ║
║  4. DTO / SERIALIZACIÓN                                      ║
║     → Convertir objetos a JSON para enviar por red           ║
║     → Ustedes: exportarJSON() con Gson                       ║
║     → Spring: Jackson convierte automáticamente              ║
║                                                              ║
║  5. STREAMS                                                  ║
║     → Procesar colecciones sin bucles                        ║
║     → Ustedes: filter/map/collect en repositorios y servicios║
║     → Spring: igual. Streams es Java, no de Spring           ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# 4.3 Conexión con lo que Viene

```
╔══════════════════════════════════════════════════════════════╗
║           MAPA: DÓNDE ESTAMOS Y HACIA DÓNDE VAMOS           ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  SEMANA 1 (días 3-5):                                        ║
║  ✓ OOP: interfaces, herencia, polimorfismo                  ║
║  ✓ Arquitectura: modelo/repo/servicio/excepción             ║
║  ✓ Git: commits, tags, historial                            ║
║                                                              ║
║  AYER (día 6):                                               ║
║  ✓ Maven: pom.xml, dependencias, compile                    ║
║  ✓ Streams: filter/map/collect, lambdas                     ║
║  ✓ GPS: saber DÓNDE va cada cambio                          ║
║                                                              ║
║  HOY (día 7):                                                ║
║  ✓ Convertir Pizzería a Maven                                ║
║  ✓ Primera dependencia real (Gson → JSON)                    ║
║  ✓ Puente a Hibernate                                        ║
║                                                              ║
║  PRÓXIMOS DÍAS:                                              ║
║  → Hibernate: @Entity, @Repository, base de datos            ║
║  → Spring: @Service, @RestController, @Autowired             ║
║  → Todo lo que construyeron SIGUE. Solo cambia la            ║
║    infraestructura.                                          ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# ═══════════════════════════════════════════════════════
# PARTE V — CIERRE DEL DÍA
# ═══════════════════════════════════════════════════════

## Resumen: lo que lograron hoy

```
╔══════════════════════════════════════════════════════════════════╗
║                HERRAMIENTAS DE LOS DÍAS 6 Y 7                    ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  DÍA 6: TEORÍA                      DÍA 7: PRÁCTICA             ║
║  ──────────────                      ──────────────              ║
║  Maven: qué es, pom.xml             Convertir Pizzería a Maven  ║
║  Streams: filter/map/collect         Agregar Gson (1ra dependencia)║
║  GPS Arquitectónico                  Puente a Hibernate          ║
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

## Commit del día

Guarden su trabajo antes de irse:

```bash
git add pom.xml
git add .gitignore
git add src/
git commit -m "v4.0: proyecto Maven, Gson JSON export (dia 7)"
```

Verificar:

```bash
git log --oneline -3
```




