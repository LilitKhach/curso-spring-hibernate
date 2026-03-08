# Día 11: Spring Boot — El Framework que lo Cambia Todo

Hoy dejamos de sufrir con `EntityManagerFactory`, `begin()`, `commit()`, `close()`... Spring Boot se encarga de todo eso. Ustedes solo escriben la lógica de negocio.

Prof. Juan Marcelo Gutiérrez Miranda

**Curso IFCD0014 — Semana 3, Día 11**
**Objetivo:** Entender qué es Spring Boot, crear el primer proyecto con Spring Initializr, y construir una API REST completa con entidad, repositorio, servicio y controlador.

> Este manual es de consulta. Sigan los pasos con el proyecto abierto en IntelliJ.

---

# PARTE I — EL PROBLEMA

## 1.1 El Problema del Día Anterior

Miren cuánto código necesitamos ayer solo para guardar UNA pizza en la base de datos:

```java
// 1. Crear la fabrica (lee persistence.xml)
EntityManagerFactory emf = Persistence.createEntityManagerFactory("pizzeria-pu");

// 2. Crear el EntityManager
EntityManager em = emf.createEntityManager();

// 3. Abrir transaccion
em.getTransaction().begin();

// 4. Persistir el objeto
em.persist(pizza);

// 5. Confirmar
em.getTransaction().commit();

// 6. Cerrar todo (si no, hay memory leaks)
em.close();
emf.close();
```

Y en cada método del repositorio, había que repetir el patrón `begin/commit/rollback`. Eso es **boilerplate**: código repetitivo que no aporta lógica de negocio.

Spring Boot elimina TODO eso.

---

# PARTE II — CONCEPTOS FUNDAMENTALES

## 2.1 ¿Qué es Spring?

### ¿Qué es?

Spring (framework creado en 2002 que revolucionó el desarrollo Java) es un marco de trabajo para construir aplicaciones Java empresariales. Spring Boot (versión simplificada de Spring, lanzada en 2014) hace que empezar un proyecto tome minutos en vez de días.

### ¿Por qué existe?

Sin Spring, cada proyecto Java requería decenas de archivos XML de configuración, un servidor externo como Tomcat (servidor web Java que ejecuta aplicaciones web), y cientos de líneas de infraestructura. Spring Boot elimina todo eso.

### ¿Dónde va?

Spring Boot es la BASE del proyecto. Todo gira alrededor de él: la configuración, las dependencias, el servidor, la inyección de objetos.

### ¿Se usa siempre?

En la industria Java actual, Spring Boot es EL estándar. El 70%+ de las aplicaciones Java empresariales lo usan.

```
╔═══════════════════════════════════════════════════════════════════╗
║  SPRING EN 30 SEGUNDOS                                            ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  Spring = Framework para construir aplicaciones Java              ║
║                                                                   ║
║  Framework = esqueleto + infraestructura para la app              ║
║    (como un edificio donde tu pones los muebles)                  ║
║                                                                   ║
║  Spring Boot = Spring hecho facil                                 ║
║    - Autoconfiguracion (detecta lo que necesitas)                 ║
║    - Starters (paquetes de dependencias pre-armados)              ║
║    - Servidor embebido (no necesitan instalar Tomcat)             ║
║    - Cero XML (todo con anotaciones y properties)                 ║
║                                                                   ║
║  Sin Spring Boot:                                                 ║
║    30 archivos XML + 50 lineas de configuracion + Tomcat externo  ║
║                                                                   ║
║  Con Spring Boot:                                                 ║
║    1 archivo application.properties + ejecutar con java -jar      ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

Spring Boot sigue el **Principio de Hollywood**: "No nos llamen, nosotros los llamamos." Ustedes definen los componentes; Spring los crea, los conecta y los ejecuta.

---

## 2.2 Inversión de Control (IoC) y Dependency Injection (DI)

Este es el concepto MÁS importante de Spring. Si lo entienden, entienden Spring.

### ¿Qué es?

**Inversión de Control (IoC)** significa que ustedes ya no crean los objetos — Spring los crea por ustedes. **Inyección de Dependencias (DI)** significa que Spring les pasa automáticamente los objetos que necesitan.

### ¿Por qué existe?

Sin IoC, cada vez que cambian una implementación (ej: de `PizzaRepositoryMemoria` a `PizzaRepositoryJPA`), tienen que modificar el `main()`. Con IoC, Spring detecta la implementación correcta y la inyecta sin que ustedes toquen nada.

### Sin Spring: ustedes crean todo

```java
// En el main, USTEDES crean cada dependencia manualmente:
PizzaRepository repo = new PizzaRepositoryJPA(em);    // ustedes lo crean
PizzaService service = new PizzaService(repo);         // ustedes lo conectan
// Si cambian la implementacion, tienen que tocar ESTE codigo
```

### Con Spring: el contenedor crea e inyecta

```java
// Ustedes solo DECLARAN lo que necesitan:

@Repository
public class PizzaRepository { ... }    // Spring lo crea

@Service
public class PizzaService {
    private final PizzaRepository repo;  // Spring lo inyecta

    // Spring ve que PizzaService necesita un PizzaRepository
    // y se lo pasa automaticamente
    public PizzaService(PizzaRepository repo) {
        this.repo = repo;
    }
}
```

**El "contenedor" de Spring** es como un almacén de objetos. Cuando la app arranca:

```
1. Spring escanea todas las clases con @Component, @Service, @Repository, @Controller
2. Crea UNA instancia de cada una (los "beans")
3. Mira los constructores: "PizzaService necesita un PizzaRepository"
4. Busca en su almacen: "Tengo un PizzaRepository, se lo paso"
5. Listo. Todo conectado sin que ustedes escriban una linea de "new"
```

```
╔═══════════════════════════════════════════════════════════════════╗
║  COMPARACION: SIN SPRING vs CON SPRING                            ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  SIN SPRING (nuestro main hasta ahora):                           ║
║                                                                   ║
║    EntityManagerFactory emf = Persistence.create...;              ║
║    EntityManager em = emf.createEntityManager();                  ║
║    PizzaRepositoryJPA repo = new PizzaRepositoryJPA(em);          ║
║    ClienteRepositoryJPA clienteRepo = new ClienteRepositoryJPA(); ║
║    PizzaService pizzaService = new PizzaService(repo);            ║
║    ClienteService clienteService = new ClienteService(...);       ║
║    // 6+ lineas solo para configurar                              ║
║                                                                   ║
║  CON SPRING:                                                      ║
║                                                                   ║
║    @SpringBootApplication                                         ║
║    public class PizzeriaApplication {                             ║
║        public static void main(String[] args) {                   ║
║            SpringApplication.run(PizzeriaApplication.class, args);║
║        }                                                          ║
║    }                                                              ║
║    // 3 lineas. Spring crea TODO lo demas.                        ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

---

# PARTE III — CREAR EL PRIMER PROYECTO

## 3.1 Spring Initializr

Abran el navegador y vayan a: **https://start.spring.io**

Configuren así:

```
╔═══════════════════════════════════════════╗
║  SPRING INITIALIZR                        ║
╠═══════════════════════════════════════════╣
║  Project:      Maven                      ║
║  Language:     Java                       ║
║  Spring Boot:  la ultima estable (4.x)    ║
║                                           ║
║  Group:        com.curso                  ║
║  Artifact:     demo-spring                ║
║  Name:         demo-spring                ║
║  Package name: com.curso.demospring       ║
║  Packaging:    Jar                        ║
║  Java:         17                         ║
║                                           ║
║  DEPENDENCIES (boton "Add"):              ║
║  * Spring Web                             ║
║  * Spring Data JPA                        ║
║  * H2 Database                            ║
║  * Lombok                                 ║
║  * Spring Boot DevTools                   ║
╚═══════════════════════════════════════════╝
```

Hagan clic en **"Generate"**. Se descarga un ZIP.

**En IntelliJ:**

```
1. Descomprimir el ZIP en una carpeta (ej: Escritorio/demo-spring)
2. File -> Open -> seleccionar la carpeta demo-spring
3. IntelliJ detecta Maven -> "Open as Project" -> Si
4. Esperar a que Maven descargue todas las dependencias (puede tardar 1-2 min)
```

### Estructura del proyecto generado

```
demo-spring/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/curso/demospring/
│   │   │       └── DemoSpringApplication.java    ← clase principal
│   │   └── resources/
│   │       ├── application.properties             ← configuracion
│   │       ├── static/                            ← archivos web (HTML, CSS, JS)
│   │       └── templates/                         ← plantillas (Thymeleaf)
│   └── test/
│       └── java/
│           └── com/curso/demospring/
│               └── DemoSpringApplicationTests.java ← tests
├── pom.xml                                         ← dependencias
└── mvnw / mvnw.cmd                                 ← Maven wrapper
```

Miren el `pom.xml`. Tiene un `<parent>`:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.3</version>
</parent>
```

Esto hereda TODA la configuración de Spring Boot. Las versiones de cada librería ya están predefinidas — no necesitan poner números de versión en las dependencias.

---

## 3.2 Lombok: Adiós Boilerplate

Antes de empezar con Spring, aprendamos Lombok (librería Java que genera getters, setters, constructores y más en tiempo de compilación — no necesitan escribirlos a mano).

```
╔═══════════════════════════════════════════════════════════════════╗
║  LOMBOK = menos codigo, misma funcionalidad                       ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  SIN LOMBOK (40 lineas):           CON LOMBOK (10 lineas):        ║
║                                                                   ║
║  public class Producto {           @Data                          ║
║      private Long id;              @NoArgsConstructor              ║
║      private String nombre;        @AllArgsConstructor             ║
║      private double precio;        public class Producto {         ║
║                                        private Long id;            ║
║      public Producto() {}              private String nombre;      ║
║      public Producto(Long id,          private double precio;      ║
║          String nombre,            }                               ║
║          double precio) {                                          ║
║          this.id = id;             // Lombok genera TODO:          ║
║          this.nombre = nombre;     // getId, setId,                ║
║          this.precio = precio;     // getNombre, setNombre,        ║
║      }                             // getPrecio, setPrecio,        ║
║                                    // toString, equals, hashCode,  ║
║      public Long getId() {...}     // constructor vacio,           ║
║      public void setId(...) {...}  // constructor con todos        ║
║      public String getNombre()...                                  ║
║      public void setNombre(...)..                                  ║
║      public double getPrecio()...                                  ║
║      public void setPrecio(...)..                                  ║
║      public String toString()...                                   ║
║      public boolean equals(...)..                                  ║
║      public int hashCode() {...}                                   ║
║  }                                                                ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

### Anotaciones Lombok que vamos a usar

| Anotación | Genera |
|-----------|--------|
| `@Data` | Getters + setters + `toString` + `equals` + `hashCode` |
| `@NoArgsConstructor` | Constructor sin argumentos (el que necesita Hibernate) |
| `@AllArgsConstructor` | Constructor con TODOS los campos |
| `@Builder` | Patrón Builder para crear objetos de forma fluida |
| `@Getter` / `@Setter` | Solo getters, solo setters (si no quieren todo `@Data`) |

### IntelliJ y Lombok

IntelliJ 2025+ ya trae soporte para Lombok integrado. Si les pide activar "annotation processing":

```
File -> Settings -> Build -> Compiler -> Annotation Processors -> Enable
```

---

## 3.3 application.properties

Este archivo reemplaza el `persistence.xml` que usamos ayer. Mucho más simple.

*📁 Archivo a modificar: `src/main/resources/application.properties`*

```properties
# --- Base de datos H2 en memoria ---
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# --- JPA / Hibernate ---
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# --- Puerto del servidor ---
server.port=8080
```

**Comparación con ayer:**

| Ayer (persistence.xml) | Hoy (application.properties) |
|------------------------|------------------------------|
| 25 líneas de XML | 7 líneas de properties |
| `<persistence-unit>` con `<class>` | Spring detecta `@Entity` automáticamente |
| `<property name="jakarta.persistence.jdbc.url">` | `spring.datasource.url=` |
| `hibernate.hbm2ddl.auto` | `spring.jpa.hibernate.ddl-auto` |

Además, la base de datos la podemos ver con DBeaver conectándonos a `jdbc:h2:mem:testdb` mientras la app está corriendo.

---

# PARTE IV — LAS 4 CAPAS DE SPRING BOOT

## 4.1 Entidad: Producto.java en 15 Líneas

*📁 Archivo a crear: `src/main/java/com/curso/demospring/modelo/Producto.java`*

```java
package com.curso.demospring.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Producto con Spring + Lombok.
 *
 * Comparen con la version de ayer:
 * - Ayer: 60+ lineas (getters, setters, constructores, toString)
 * - Hoy: 15 lineas. Lombok genera todo lo demas.
 */
@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private double precio;

    private String categoria;
}
```

Eso es todo. Quince líneas. Y tienen:
- Getters y setters para los 4 campos
- `toString()` con todos los campos
- `equals()` y `hashCode()`
- Constructor vacío (para Hibernate)
- Constructor con todos los campos

---

## 4.2 Repositorio: De 70 Líneas a 3

### ¿Qué es Spring Data JPA?

Spring Data JPA (módulo de Spring que genera repositorios automáticamente a partir de interfaces) es probablemente la parte más impactante de Spring Boot. Elimina TODO el código de acceso a datos que escribieron el viernes.

### ¿Cómo funciona?

Ustedes definen una **interfaz** (sin implementación). Spring, al arrancar la app, crea un **proxy dinámico** — un objeto invisible generado en memoria que implementa todos los métodos. Ese proxy usa `EntityManager` internamente (el mismo que usaron el viernes), pero ustedes ya no lo ven.

```
╔═══════════════════════════════════════════════════════════════════╗
║  QUE PASA CUANDO EXTIENDEN JpaRepository                         ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  Ustedes escriben:                                                ║
║     public interface ProductoRepository                           ║
║         extends JpaRepository<Producto, Long> { }                 ║
║                                                                   ║
║  Spring genera AUTOMATICAMENTE estos metodos:                     ║
║     save(Producto p)          --> INSERT o UPDATE                 ║
║     findById(Long id)         --> SELECT WHERE id = ?             ║
║     findAll()                 --> SELECT * FROM productos         ║
║     deleteById(Long id)       --> DELETE WHERE id = ?             ║
║     count()                   --> SELECT COUNT(*)                 ║
║     existsById(Long id)       --> SELECT COUNT(*) > 0             ║
║                                                                   ║
║  Y ademas, pueden agregar metodos CUSTOM por nombre:             ║
║     findByCategoria(String)   --> WHERE categoria = ?             ║
║     findByPrecioGreaterThan() --> WHERE precio > ?                ║
║     findByNombreContaining()  --> WHERE nombre LIKE %?%           ║
║                                                                   ║
║  Spring PARSEA el nombre del metodo y genera la consulta JPQL.   ║
║  Ustedes no escriben ni una linea de SQL.                         ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

### El repositorio

*📁 Archivo a crear: `src/main/java/com/curso/demospring/repositorio/ProductoRepository.java`*

```java
package com.curso.demospring.repositorio;

import com.curso.demospring.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de Producto.
 *
 * Eso es TODO. Una interfaz que extiende JpaRepository.
 * Spring genera automaticamente la implementacion con estos metodos:
 *
 *   findAll()          -> SELECT * FROM productos
 *   findById(Long id)  -> SELECT * FROM productos WHERE id = ?
 *   save(Producto p)   -> INSERT o UPDATE segun si tiene id o no
 *   deleteById(Long id)-> DELETE FROM productos WHERE id = ?
 *   count()            -> SELECT COUNT(*) FROM productos
 *   existsById(Long id)-> SELECT COUNT(*) > 0 WHERE id = ?
 *
 * Ademas, pueden agregar metodos custom por convencion de nombre:
 *   List<Producto> findByCategoria(String categoria);
 *   List<Producto> findByPrecioGreaterThan(double precio);
 *   Optional<Producto> findByNombre(String nombre);
 *
 * Spring lee el nombre del metodo y genera la consulta SQL.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

}
```

**Comparen:**

```
╔═══════════════════════════════════════════════════════════════════╗
║  AYER (Hibernate puro)           HOY (Spring Data JPA)            ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  public class PizzaRepositoryJPA   public interface ProductoRepo  ║
║      implements PizzaRepository {      extends JpaRepository      ║
║                                            <Producto, Long> {     ║
║      private final EntityManager em;                              ║
║                                        }                          ║
║      public Pizza guardar(Pizza p) {   // Eso es todo.            ║
║          try {                         // Spring genera:           ║
║              em.getTransaction()       //   save()                ║
║                .begin();               //   findAll()             ║
║              if (p.getId() == null) {  //   findById()            ║
║                  em.persist(p);        //   deleteById()          ║
║              } else {                  //   count()               ║
║                  em.merge(p);          //   existsById()          ║
║              }                         //   y muchos mas...       ║
║              em.getTransaction()                                  ║
║                .commit();                                         ║
║          } catch (Exception e) {                                  ║
║              if (em.getTransaction()                              ║
║                  .isActive()) {                                   ║
║                  em.getTransaction()                              ║
║                    .rollback();                                   ║
║              }                                                    ║
║              throw e;                                             ║
║          }                                                        ║
║      }                                                            ║
║      // ... 40 lineas mas ...                                     ║
║  }                                                                ║
║                                                                   ║
║  70+ lineas                        3 lineas                       ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

---

## 4.3 Service: Lógica de Negocio con @Service

### ¿Qué hace la capa de servicio?

El servicio es donde va la **lógica de negocio**: validaciones, cálculos, reglas. El controlador NO debe tener lógica — solo recibe la petición y la pasa al servicio. El repositorio NO debe tener lógica — solo habla con la base de datos.

```
╔═══════════════════════════════════════════════════════════════════╗
║  RESPONSABILIDADES DE CADA CAPA                                   ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  @RestController  -->  Recibir HTTP, devolver JSON               ║
║                        NO tiene logica de negocio                 ║
║                                                                   ║
║  @Service         -->  Logica de negocio                          ║
║                        Validaciones, calculos, reglas             ║
║                        Coordina entre repositorios                ║
║                                                                   ║
║  JpaRepository    -->  Acceso a datos                             ║
║                        Solo guarda, busca, elimina                ║
║                        NO sabe de reglas de negocio               ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

`@Service` le dice a Spring: "crea una instancia de esta clase y ponla disponible para inyección". Fíjense que el constructor recibe `ProductoRepository` — Spring ve que necesita uno y se lo inyecta automáticamente. Ustedes NO hacen `new`.

El servicio de hoy se ve casi igual que los que ya hicieron... pero sin crear dependencias manualmente.

*📁 Archivo a crear: `src/main/java/com/curso/demospring/servicio/ProductoService.java`*

```java
package com.curso.demospring.servicio;

import com.curso.demospring.modelo.Producto;
import com.curso.demospring.repositorio.ProductoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Producto.
 *
 * @Service le dice a Spring: "crea una instancia de esta clase y ponla
 * disponible para inyeccion". Cuando alguien necesite un ProductoService,
 * Spring se lo da automaticamente.
 *
 * Fijense: el constructor recibe ProductoRepository.
 * Spring ve que ProductoRepository es un bean (porque extiende JpaRepository)
 * y se lo inyecta automaticamente. Nosotros NO hacemos "new".
 */
@Service
public class ProductoService {

    private final ProductoRepository repositorio;

    // Spring inyecta el repositorio aqui automaticamente
    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Lista todos los productos.
     */
    public List<Producto> listarTodos() {
        return repositorio.findAll();
    }

    /**
     * Busca un producto por id.
     */
    public Optional<Producto> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    /**
     * Guarda un producto (nuevo o existente).
     * Si tiene id -> UPDATE. Si no tiene id -> INSERT.
     */
    public Producto guardar(Producto producto) {
        return repositorio.save(producto);
    }

    /**
     * Elimina un producto por id.
     */
    public void eliminar(Long id) {
        repositorio.deleteById(id);
    }
}
```

Comparen con `PedidoService` de nuestro proyecto: la estructura es IDÉNTICA. La única diferencia es que Spring crea e inyecta el repositorio. Ustedes ya sabían hacer servicios.

---

## 4.4 Controller: Exponer la API REST

### ¿Qué es una API REST?

Hasta ahora, nuestra Pizzería solo podía ejecutarse desde el `main()` con `System.out.println`. Para usar la app, alguien tenía que abrir IntelliJ y ejecutar el código. Eso no es una aplicación real.

En el mundo real, las aplicaciones se comunican por **HTTP** — el mismo protocolo que usa el navegador cuando entran a una web. Una **API REST** es una aplicación que RECIBE peticiones HTTP y DEVUELVE datos (normalmente en formato JSON).

```
╔═══════════════════════════════════════════════════════════════════╗
║  QUE ES UNA API REST                                              ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  API = Application Programming Interface                          ║
║    Una puerta de entrada para que OTROS programas                 ║
║    se comuniquen con tu aplicacion.                               ║
║                                                                   ║
║  REST = Representational State Transfer                            ║
║    Un estilo de organizacion de URLs.                             ║
║    Cada URL representa un RECURSO (producto, cliente, pedido).   ║
║    Cada VERBO HTTP dice que hacer con ese recurso.               ║
║                                                                   ║
║  Quien consume una API REST?                                      ║
║    - Un navegador web (Chrome, Firefox)                           ║
║    - Una app movil (Android, iOS)                                 ║
║    - Un frontend (React, Angular, Vue)                            ║
║    - Otra API (microservicios)                                    ║
║    - Ustedes con curl/PowerShell/Postman                          ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

### Los verbos HTTP

HTTP tiene 5 verbos principales. Cada uno dice QUÉ hacer. La URL dice CON QUÉ hacerlo:

| Verbo HTTP | Significado | Ejemplo | Anotación Spring |
|------------|-------------|---------|------------------|
| **GET** | Leer / listar | GET `/api/productos` | `@GetMapping` |
| **GET** | Leer uno por id | GET `/api/productos/3` | `@GetMapping("/{id}")` |
| **POST** | Crear nuevo | POST `/api/productos` | `@PostMapping` |
| **PUT** | Actualizar existente | PUT `/api/productos/3` | `@PutMapping("/{id}")` |
| **DELETE** | Eliminar | DELETE `/api/productos/3` | `@DeleteMapping("/{id}")` |

```
╔═══════════════════════════════════════════════════════════════════╗
║  COMO FUNCIONA UNA PETICION HTTP                                  ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  Peticion (lo que envia el cliente):                              ║
║                                                                   ║
║    POST /api/productos HTTP/1.1                                   ║
║    Content-Type: application/json                                 ║
║                                                                   ║
║    {"nombre":"Margarita","precio":8.50,"categoria":"CLASICA"}     ║
║                                                                   ║
║  Respuesta (lo que devuelve Spring):                              ║
║                                                                   ║
║    HTTP/1.1 200 OK                                                ║
║    Content-Type: application/json                                 ║
║                                                                   ║
║    {"id":1,"nombre":"Margarita","precio":8.50,"categoria":"CLASICA"}║
║                                                                   ║
║  Fijense: el id no lo enviaron ustedes.                           ║
║  Hibernate lo genero y Spring lo devolvio en la respuesta.        ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

### ¿Quién convierte Java a JSON?

**Jackson** (librería de serialización que viene incluida en `spring-boot-starter-web`). Cuando un método de un `@RestController` devuelve un objeto Java, Jackson lo convierte automáticamente a JSON. Cuando un método recibe `@RequestBody Producto`, Jackson convierte el JSON del body HTTP a un objeto Java.

Es lo mismo que hacíamos con Gson el día 7, pero automático. Ustedes no escriben una línea de conversión.

### Anatomía de un @RestController — El Patrón

Antes de ver el código, entiendan el **patrón**. TODOS los controladores REST en Spring siguen la misma estructura. Si entienden este patrón, pueden crear controladores para cualquier entidad (Pizza, Cliente, Pedido, lo que sea).

```
╔═══════════════════════════════════════════════════════════════════╗
║  RECETA PARA CREAR UN CONTROLADOR REST                           ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  1. @RestController                                              ║
║     Le dice a Spring: "esta clase maneja peticiones HTTP          ║
║     y DEVUELVE datos (no una pagina HTML)".                      ║
║     Sin esta anotacion, Spring no sabe que esta clase existe.    ║
║                                                                   ║
║  2. @RequestMapping("/api/RECURSO")                              ║
║     Define la ruta BASE. Todas las rutas del controlador         ║
║     empiezan con este prefijo.                                   ║
║     Convencion: /api/ + nombre del recurso en plural.            ║
║                                                                   ║
║  3. Inyeccion del servicio (constructor)                         ║
║     El controlador NUNCA accede al repositorio directamente.     ║
║     Siempre pasa por el servicio.                                ║
║                                                                   ║
║  4. Un metodo por operacion:                                     ║
║     @GetMapping          → listar todos                         ║
║     @GetMapping("/{id}") → buscar uno                           ║
║     @PostMapping          → crear nuevo                         ║
║     @PutMapping("/{id}")  → actualizar existente                ║
║     @DeleteMapping("/{id}")→ eliminar                           ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

### Las anotaciones clave — qué hace cada una

| Anotación | Dónde va | Qué hace |
|-----------|----------|----------|
| `@RestController` | En la clase | Marca la clase como controlador REST. Combina `@Controller` + `@ResponseBody` (todo lo que devuelve se convierte a JSON automáticamente) |
| `@RequestMapping("/api/productos")` | En la clase | Define la ruta base. Todos los endpoints heredan este prefijo |
| `@GetMapping` | En un método | Este método responde a peticiones GET (leer datos) |
| `@PostMapping` | En un método | Este método responde a peticiones POST (crear datos) |
| `@PutMapping("/{id}")` | En un método | Este método responde a peticiones PUT (actualizar datos) |
| `@DeleteMapping("/{id}")` | En un método | Este método responde a peticiones DELETE (eliminar datos) |
| `@PathVariable` | En un parámetro | Extrae un valor de la URL. En `GET /api/productos/3`, el `3` se asigna al parámetro `Long id` |
| `@RequestBody` | En un parámetro | Toma el JSON del body HTTP y lo convierte a un objeto Java (usando Jackson) |
| `ResponseEntity<T>` | Tipo de retorno | Permite controlar el código HTTP de respuesta: 200 OK, 404 Not Found, 204 No Content |

```
╔═══════════════════════════════════════════════════════════════════╗
║  COMO SPRING CONECTA UNA PETICION HTTP CON UN METODO JAVA        ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  Peticion HTTP:                                                  ║
║    GET /api/productos/3                                          ║
║         ───────────── ─                                          ║
║              |         |                                         ║
║              v         v                                         ║
║  @RequestMapping   @PathVariable                                 ║
║  ("/api/productos") Long id = 3                                  ║
║                                                                   ║
║  Spring busca:                                                   ║
║    1. Que clase tiene @RequestMapping("/api/productos")? ──→ ProductoController ║
║    2. Que metodo tiene @GetMapping("/{id}")? ──→ buscarPorId()   ║
║    3. El {id} de la URL es 3 ──→ @PathVariable Long id = 3      ║
║    4. Ejecuta buscarPorId(3)                                     ║
║    5. El resultado lo convierte a JSON con Jackson               ║
║                                                                   ║
║  Peticion HTTP con body:                                         ║
║    POST /api/productos                                           ║
║    Body: {"nombre":"Margarita","precio":8.50}                    ║
║                     |                                            ║
║                     v                                            ║
║               @RequestBody Producto producto                     ║
║               Jackson convierte el JSON a objeto Java            ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

### Plantilla reutilizable

Si mañana necesitan crear un controlador para `Pizza`, `Cliente`, o cualquier entidad, la estructura es SIEMPRE esta:

```java
@RestController
@RequestMapping("/api/RECURSO_EN_PLURAL")
public class RecursoController {

    private final RecursoService servicio;

    public RecursoController(RecursoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping                                    // GET /api/recurso
    public List<Recurso> listarTodos() { ... }

    @GetMapping("/{id}")                           // GET /api/recurso/3
    public ResponseEntity<Recurso> buscarPorId(@PathVariable Long id) { ... }

    @PostMapping                                   // POST /api/recurso
    public Recurso crear(@RequestBody Recurso r) { ... }

    @PutMapping("/{id}")                           // PUT /api/recurso/3
    public ResponseEntity<Recurso> actualizar(@PathVariable Long id,
                                               @RequestBody Recurso r) { ... }

    @DeleteMapping("/{id}")                        // DELETE /api/recurso/3
    public ResponseEntity<Void> eliminar(@PathVariable Long id) { ... }
}
```

Cambien `Recurso` por el nombre de su entidad y listo. Ese es el patrón completo de un CRUD REST.

> **Referencia oficial:** La documentación de Spring para controladores web está en:
> https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
> No necesitan leerla ahora, pero guárdenla. Cuando quieran hacer cosas más avanzadas (validaciones, paginación, filtros), ahí está todo.

### El controlador

*📁 Archivo a crear: `src/main/java/com/curso/demospring/controlador/ProductoController.java`*

```java
package com.curso.demospring.controlador;

import com.curso.demospring.modelo.Producto;
import com.curso.demospring.servicio.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Controlador REST para Producto.
 *
 * @RestController = esta clase maneja peticiones HTTP y devuelve JSON.
 * @RequestMapping("/api/productos") = todas las rutas empiezan con /api/productos.
 *
 * Spring convierte los objetos Java a JSON automaticamente (usando Jackson,
 * que viene incluido en spring-boot-starter-web).
 *
 * Recuerdan Gson del dia 7? Aqui NO necesitan Gson. Jackson hace lo mismo
 * pero de forma automatica.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    /**
     * GET /api/productos
     * Devuelve la lista de todos los productos en JSON.
     */
    @GetMapping
    public List<Producto> listarTodos() {
        return servicio.listarTodos();
    }

    /**
     * GET /api/productos/{id}
     * Devuelve un producto por su id.
     * Si no existe, devuelve 404 (Not Found).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscarPorId(@PathVariable Long id) {
        return servicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/productos
     * Crea un producto nuevo. El JSON viene en el body de la peticion.
     *
     * Ejemplo de JSON que se envia:
     * {
     *     "nombre": "Margarita",
     *     "precio": 8.50,
     *     "categoria": "CLASICA"
     * }
     */
    @PostMapping
    public Producto crear(@RequestBody Producto producto) {
        return servicio.guardar(producto);
    }

    /**
     * PUT /api/productos/{id}
     * Actualiza un producto existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id,
                                                @RequestBody Producto producto) {
        return servicio.buscarPorId(id)
                .map(existente -> {
                    producto.setId(id);
                    return ResponseEntity.ok(servicio.guardar(producto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/productos/{id}
     * Elimina un producto por su id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (servicio.buscarPorId(id).isPresent()) {
            servicio.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
```

---

## 4.5 Ejecutar y Probar la API

Ejecuten la aplicación (botón de play en `DemoSpringApplication`).

En la consola de IntelliJ van a ver mucho texto. Lo importante es la última línea:

```
Started DemoSpringApplication in X.X seconds
```

Eso significa que Spring Boot arrancó Tomcat (servidor web embebido), configuró Hibernate, creó las tablas en H2, y está escuchando peticiones HTTP en el puerto 8080.

> **IMPORTANTE:** Si entran a `http://localhost:8080` en el navegador, van a ver una página de error ("Whitelabel Error Page"). Eso es NORMAL — no tenemos un controlador para la ruta `/`. Nuestra API está en `/api/productos`.

### Paso 1: Verificar que la API responde

Abran el navegador y vayan a:

```
http://localhost:8080/api/productos
```

Van a ver: `[]` — un array vacío en JSON. Todavía no hay productos, pero la API FUNCIONA.

### Paso 2: Crear un producto (POST)

Desde PowerShell (abran una terminal nueva, la app tiene que seguir corriendo):

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/productos" `
  -ContentType "application/json" `
  -Body '{"nombre":"Margarita","precio":8.50,"categoria":"CLASICA"}'
```

PowerShell les va a devolver algo así:

```
id        : 1
nombre    : Margarita
precio    : 8.5
categoria : CLASICA
```

Eso es Spring convirtiendo su objeto Java a JSON y devolviéndolo. El `id: 1` lo generó Hibernate automáticamente.

### Paso 3: Ver el producto en el navegador

Refresquen `http://localhost:8080/api/productos` en el navegador:

```json
[
    {
        "id": 1,
        "nombre": "Margarita",
        "precio": 8.50,
        "categoria": "CLASICA"
    }
]
```

Piensen en lo que acaba de pasar:
1. Enviaron un JSON por HTTP (POST)
2. Jackson lo convirtió a un objeto `Producto`
3. El controller lo pasó al service
4. El service llamó `save()` en el repository
5. Spring Data generó el SQL INSERT
6. Hibernate lo ejecutó en H2
7. H2 guardó el dato y devolvió el id
8. Todo el camino de vuelta: Hibernate → Spring Data → Service → Controller → Jackson → JSON

**Cero líneas de SQL. Cero líneas de conversión JSON. Spring Boot + Hibernate hicieron todo.**

### Paso 4: Crear más productos

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/productos" `
  -ContentType "application/json" `
  -Body '{"nombre":"Carnivora","precio":12.00,"categoria":"CLASICA"}'

Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/productos" `
  -ContentType "application/json" `
  -Body '{"nombre":"Trufa Negra","precio":18.50,"categoria":"GOURMET"}'
```

Refresquen el navegador: ahora ven 3 productos en JSON.

### Paso 5: Buscar por id

En el navegador: `http://localhost:8080/api/productos/1`

Devuelve SOLO la Margarita. Eso es el endpoint `@GetMapping("/{id}")` en acción.

### Paso 6: Eliminar

```powershell
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/productos/2"
```

Refresquen: la Carnívora desapareció. Solo quedan 2 productos.

---

## 4.6 Verificar en DBeaver

Con la app corriendo, abran DBeaver y creen una nueva conexión:

```
╔═══════════════════════════════════════════════╗
║  DBeaver - Conexion a H2 en Spring Boot       ║
╠═══════════════════════════════════════════════╣
║                                               ║
║  Driver:    H2 Embedded                       ║
║  JDBC URL:  jdbc:h2:mem:testdb                ║
║  User:      sa                                ║
║  Password:  (dejar vacio)                     ║
║                                               ║
║  IMPORTANTE: la app debe estar corriendo.     ║
║  Si cierran la app, la BD desaparece          ║
║  (es H2 en memoria).                          ║
║                                               ║
╚═══════════════════════════════════════════════╝
```

Una vez conectados, van a ver la tabla `PRODUCTOS` con los datos que crearon por la API.

Pueden ejecutar SQL directamente en DBeaver:

```sql
SELECT * FROM PRODUCTOS;
```

Y lo más interesante: si insertan un dato directamente en SQL...

```sql
INSERT INTO PRODUCTOS (NOMBRE, PRECIO, CATEGORIA) VALUES ('Pepperoni', 11.50, 'CLASICA');
```

...y refrescan `http://localhost:8080/api/productos` en el navegador, el Pepperoni aparece. La API y la base de datos están conectadas en ambas direcciones.

### DevTools: Recarga Automática

Spring Boot DevTools (herramienta de desarrollo incluida en el proyecto) reinicia la aplicación automáticamente cuando detecta cambios en el código. No necesitan parar y volver a ejecutar cada vez que modifican algo.

> Si DevTools no funciona (a veces en Windows falla), simplemente paren la app y vuélvanla a ejecutar manualmente.

---

# ═══════════════════════════════
# PARTE V — EJERCICIO INTEGRADOR
# ═══════════════════════════════

# EJERCICIO INTEGRADOR — Crear Entidad Libro con Spring Boot

Acaban de construir un CRUD completo de Producto: entidad, repositorio, servicio, controlador. Con el Producto les fuimos guiando paso a paso. Ahora es su turno.

Van a crear una segunda entidad — `Libro` — dentro del mismo proyecto `demo-spring`. La idea es que repitan el patrón completo de las 4 capas **pero esta vez entendiendo qué hace cada pieza**. No se trata de copiar rápido, sino de que al terminar puedan responder algunas preguntas:

```
╔═══════════════════════════════════════════════════════════════╗
║  OBJETIVO                                                     ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  Crear la entidad Libro con las 4 capas de Spring Boot        ║
║  y exponer una API REST con estos endpoints:                  ║
║                                                               ║
║    GET    /api/libros              → listar todos              ║
║    GET    /api/libros/{id}         → buscar por id             ║
║    GET    /api/libros/autor/{autor}→ buscar por autor           ║
║    POST   /api/libros              → crear un libro            ║
║    PUT    /api/libros/{id}         → actualizar un libro       ║
║    DELETE /api/libros/{id}         → eliminar un libro         ║
║                                                               ║
║  Al terminar, deben poder crear un libro por HTTP,             ║
║  buscarlo por autor, y verlo en DBeaver.                       ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```

Si algo no compila o da 404, revisen primero:
1. ¿El paquete es hijo de `com.curso.demospring`?
2. ¿Reiniciaron la app después de crear los archivos?
3. ¿Las anotaciones están en su sitio (`@Entity`, `@RestController`, `@Service`)?

## GPS Arquitectónico del Ejercicio

```
src/main/java/com/curso/demospring/
├── modelo/
│   └── Libro.java                ← Entidad (Paso 1)
├── repositorio/
│   └── LibroRepository.java      ← Interfaz JpaRepository (Paso 2)
├── servicio/
│   └── LibroService.java         ← Lógica de negocio (Paso 3)
└── controlador/
    └── LibroController.java      ← Endpoints REST (Paso 4)
```

## Paso 1: Entidad

*📁 Archivo a crear: `src/main/java/com/curso/demospring/modelo/Libro.java`*

```java
package com.curso.demospring.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "libros")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String autor;

    private int anioPublicacion;

    private double precio;
}
```

## Paso 2: Repositorio con método custom

*📁 Archivo a crear: `src/main/java/com/curso/demospring/repositorio/LibroRepository.java`*

```java
package com.curso.demospring.repositorio;

import com.curso.demospring.modelo.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    // Spring genera la consulta a partir del nombre del metodo:
    // "findByAutor" -> SELECT * FROM libros WHERE autor = ?
    List<Libro> findByAutor(String autor);

    // Mas ejemplos de metodos que Spring genera automaticamente:
    // List<Libro> findByPrecioLessThan(double precio);
    // List<Libro> findByAnioPublicacionGreaterThan(int anio);
    // List<Libro> findByTituloContaining(String texto);
}
```

## Paso 3: Servicio

*📁 Archivo a crear: `src/main/java/com/curso/demospring/servicio/LibroService.java`*

```java
package com.curso.demospring.servicio;

import com.curso.demospring.modelo.Libro;
import com.curso.demospring.repositorio.LibroRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LibroService {

    private final LibroRepository repositorio;

    public LibroService(LibroRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Libro> listarTodos() {
        return repositorio.findAll();
    }

    public Optional<Libro> buscarPorId(Long id) {
        return repositorio.findById(id);
    }

    public Libro guardar(Libro libro) {
        return repositorio.save(libro);
    }

    public void eliminar(Long id) {
        repositorio.deleteById(id);
    }

    public List<Libro> buscarPorAutor(String autor) {
        return repositorio.findByAutor(autor);
    }
}
```

## Paso 4: Controlador

*📁 Archivo a crear: `src/main/java/com/curso/demospring/controlador/LibroController.java`*

```java
package com.curso.demospring.controlador;

import com.curso.demospring.modelo.Libro;
import com.curso.demospring.servicio.LibroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroService servicio;

    public LibroController(LibroService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Libro> listarTodos() {
        return servicio.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> buscarPorId(@PathVariable Long id) {
        return servicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Libro crear(@RequestBody Libro libro) {
        return servicio.guardar(libro);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (servicio.buscarPorId(id).isPresent()) {
            servicio.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/libros/autor/{autor}
     * Busca libros por autor (usa el metodo custom del repositorio).
     */
    @GetMapping("/autor/{autor}")
    public List<Libro> buscarPorAutor(@PathVariable String autor) {
        return servicio.buscarPorAutor(autor);
    }
}
```

## Probar el ejercicio

Desde PowerShell (con la app corriendo):

```powershell
# Crear libros
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/libros" `
  -ContentType "application/json" `
  -Body '{"titulo":"Don Quijote","autor":"Cervantes","anioPublicacion":1605,"precio":15.99}'

Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/libros" `
  -ContentType "application/json" `
  -Body '{"titulo":"La Galatea","autor":"Cervantes","anioPublicacion":1585,"precio":12.50}'

Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/libros" `
  -ContentType "application/json" `
  -Body '{"titulo":"Cien anios de soledad","autor":"Garcia Marquez","anioPublicacion":1967,"precio":18.00}'

# Listar todos
Invoke-RestMethod -Uri "http://localhost:8080/api/libros"

# Buscar por autor (metodo custom del repositorio!)
Invoke-RestMethod -Uri "http://localhost:8080/api/libros/autor/Cervantes"
```

También pueden verificar en DBeaver:

```sql
SELECT * FROM LIBROS;
SELECT * FROM LIBROS WHERE AUTOR = 'Cervantes';
```

---

# REFERENCIAS

## 6.1 GPS: La Arquitectura de Todo Backend Moderno

```
╔═══════════════════════════════════════════════════════════════════╗
║  GPS ARQUITECTONICO - DIA 11 (Spring Boot)                        ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  Browser / Postman / curl                                         ║
║       |                                                           ║
║       | HTTP (GET, POST, PUT, DELETE)                              ║
║       v                                                           ║
║  @RestController                                                  ║
║       |  Recibe la peticion, la pasa al servicio                  ║
║       v                                                           ║
║  @Service                                                         ║
║       |  Ejecuta la logica de negocio                             ║
║       v                                                           ║
║  JpaRepository (interfaz)                                         ║
║       |  Spring genera la implementacion (findAll, save, etc.)    ║
║       v                                                           ║
║  Hibernate (ORM)                                                  ║
║       |  Traduce objetos Java <-> SQL                             ║
║       v                                                           ║
║  H2 / PostgreSQL / MySQL                                          ║
║                                                                   ║
║  ESTA es la arquitectura de TODOS los backends Java modernos.     ║
║  Cambian las entidades, pero la estructura es siempre la misma.   ║
║                                                                   ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  COMPARACION CON LA PIZZERIA (todavia Java puro):                 ║
║                                                                   ║
║  Pizzeria (dia 10):       Demo Spring (hoy):                      ║
║  PizzeriaApp (main)       @RestController (HTTP)                  ║
║  PizzaService             @Service (igual)                        ║
║  PizzaRepositoryJPA       JpaRepository (3 lineas)                ║
║  EntityManager manual     Automatico                              ║
║  persistence.xml          application.properties                  ║
║  Gson para JSON           Jackson automatico                      ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

## 6.2 Tabla de Anotaciones Spring Boot

```
╔═══════════════════════════════════════════════════════════════════╗
║  ANOTACIONES SPRING BOOT - DIA 11                                 ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  ARRANQUE:                                                        ║
║  @SpringBootApplication   Clase principal. Activa autoconfigura-  ║
║                           cion, escaneo de componentes y config.  ║
║                                                                   ║
║  ENTIDADES (JPA):                                                 ║
║  @Entity                  Esta clase es una tabla                 ║
║  @Table                   Nombre de la tabla                      ║
║  @Id                      Clave primaria                          ║
║  @GeneratedValue          Id autoincremental                      ║
║                                                                   ║
║  CAPAS DE LA APLICACION:                                          ║
║  @Repository              Capa de acceso a datos                  ║
║  @Service                 Capa de logica de negocio               ║
║  @RestController          Capa de exposicion HTTP (API REST)      ║
║  @Component               Clase generica gestionada por Spring    ║
║                                                                   ║
║  ENDPOINTS HTTP:                                                  ║
║  @RequestMapping          Ruta base del controlador               ║
║  @GetMapping              Responde a GET                          ║
║  @PostMapping             Responde a POST                         ║
║  @PutMapping              Responde a PUT                          ║
║  @DeleteMapping           Responde a DELETE                       ║
║                                                                   ║
║  PARAMETROS:                                                      ║
║  @RequestBody             Lee el JSON del body de la peticion     ║
║  @PathVariable            Lee un valor de la URL (/productos/{id})║
║                                                                   ║
║  INYECCION:                                                       ║
║  @Autowired               Inyecta una dependencia (opcional si    ║
║                           solo hay un constructor)                ║
║                                                                   ║
║  LOMBOK:                                                          ║
║  @Data                    Getters + setters + toString + equals   ║
║  @NoArgsConstructor       Constructor vacio                       ║
║  @AllArgsConstructor      Constructor con todos los campos        ║
║  @Builder                 Patron Builder                          ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

## 6.3 Para Profundizar

**Documentación oficial:**
- Spring Boot Reference: https://docs.spring.io/spring-boot/docs/current/reference/html/
- Spring Data JPA Reference: https://docs.spring.io/spring-data/jpa/reference/
- Spring Initializr: https://start.spring.io

**Tutoriales recomendados:**
- Baeldung — Spring Boot REST API: https://www.baeldung.com/spring-boot-start
- Baeldung — Spring Data JPA: https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa
- DigitalOcean — Building REST APIs with Spring Boot: https://www.digitalocean.com/community/tutorials/spring-boot-rest-api

**Libro gratuito:**
- "Spring Boot in Action" (resumen en manning.com)
- Spring Guides oficiales: https://spring.io/guides

---

# ═══════════════════════════════
# PARTE VI — CIERRE DEL DÍA
# ═══════════════════════════════

## Resumen: lo que lograron hoy

```
╔═══════════════════════════════════════════════════════════════════╗
║              ¿QUE CAMBIA CON SPRING BOOT?                         ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  CAMBIA:                                                          ║
║  * Como se crean los objetos (Spring los crea, no ustedes)        ║
║  * Como se conectan (inyeccion automatica, no "new")              ║
║  * Como se configura la BD (properties, no XML)                   ║
║  * Como se expone al mundo (HTTP/REST, no solo consola)           ║
║  * Los repositorios (3 lineas en vez de 70)                       ║
║                                                                   ║
║  NO CAMBIA:                                                       ║
║  * La arquitectura por capas (controller/service/repository)      ║
║  * Las entidades JPA (@Entity, @Id, relaciones)                   ║
║  * La logica de negocio (validaciones, calculos)                  ║
║  * Los Streams y lambdas (siguen siendo Java)                     ║
║  * El patron de inyeccion de dependencias                         ║
║    (ya lo hacian por constructor, Spring lo automatiza)            ║
║                                                                   ║
║  TODO lo que aprendieron en las semanas anteriores SIGUE.         ║
║  Spring Boot solo lo hace mas facil y agrega la capa HTTP.        ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

## Commit del día

Este proyecto es independiente de la Pizzería. No necesitan commit en el repositorio de la Pizzería.

```bash
cd demo-spring
git init
git add -A
git commit -m "demo-spring: primer proyecto Spring Boot con API REST, JpaRepository y Lombok"
```
