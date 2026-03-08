# Día 10: La Pizzería Recuerda — Hibernate en Nuestro Proyecto

Ayer aprendieron Hibernate con un proyecto nuevo (`curso-hibernate`, entidad `Producto`, CRUD con `EntityManager`, H2 en memoria). Hoy traemos ese poder a NUESTRO proyecto: la Pizzería va a guardar datos en una base de datos real.

Prof. Juan Marcelo Gutiérrez Miranda

**Curso IFCD0014 — Semana 2, Día 10 (Viernes)**
**Objetivo:** Integrar Hibernate en la Pizzería para que las pizzas, clientes y pedidos se persistan en H2. Anotar las entidades, refactorizar los repositorios y ver las relaciones JPA en acción.

> Este manual es de consulta. Sigan los pasos con el proyecto abierto en IntelliJ.

---

# PARTE I — PREPARAR EL PROYECTO

## 1.1 Objetivo del Día

Hasta ahora, cada vez que cerramos la Pizzería, los datos desaparecen. Todo vive en `HashMap` y `ArrayList` dentro de la RAM. Hoy cambiamos eso.

```
ANTES (dias 3-9):
  PizzeriaApp -> Service -> Repository -> ArrayList/HashMap (RAM)
  Cierran la app = se pierde todo

DESPUES (hoy):
  PizzeriaApp -> Repository -> EntityManager -> H2 (base de datos)
  Cierran la app = los datos siguen ahi (si usamos H2 en archivo)
```

Lo mejor: la lógica de negocio casi no cambia. Solo cambia CÓMO se guardan los datos.

---

## 1.2 Limpiar el Proyecto — Quitar Archivos de la Versión Anterior

Antes de agregar Hibernate, necesitamos limpiar el proyecto. La Pizzería tiene clases que fueron útiles para aprender herencia e interfaces, pero que **no son compatibles** con la nueva versión JPA:

- La `Pizza` original era abstracta con herencia (`PizzaClasica`, `PizzaGourmet`). Hibernate necesita una clase concreta.
- Los servicios (`PedidoService`, `ClienteService`) llaman métodos que ya no van a existir en las nuevas entidades.
- Los repositorios en memoria usan `int` como id, pero Hibernate usa `Long`.
- Las interfaces `Vendible` y `Preparable` ya no aplican porque Pizza se convierte en una entidad JPA simple.

**Archivos a ELIMINAR** (clic derecho en IntelliJ → Delete):

```
src/main/java/com/pizzeria/
├── modelo/
│   ├── PizzaClasica.java       ← ELIMINAR
│   ├── PizzaGourmet.java       ← ELIMINAR
│   ├── Vendible.java           ← ELIMINAR
│   ├── Preparable.java         ← ELIMINAR
│   ├── Bebida.java             ← ELIMINAR
│   ├── Postre.java             ← ELIMINAR
│   └── Combo.java              ← ELIMINAR (se recrea como @Entity)
├── servicio/                   ← ELIMINAR carpeta completa
├── excepcion/                  ← ELIMINAR carpeta completa
└── repositorio/
    ├── PedidoRepositoryMemoria.java  ← ELIMINAR
    ├── ClienteRepositoryMemoria.java ← ELIMINAR
    └── PedidoRepository.java         ← ELIMINAR
```

**Archivos que SE QUEDAN** (se van a reescribir):

```
src/main/java/com/pizzeria/
├── modelo/
│   ├── Pizza.java              ← se reescribe como @Entity
│   ├── Cliente.java            ← se reescribe como @Entity
│   ├── Pedido.java             ← se reescribe como @Entity
│   ├── TipoCliente.java        ← no cambia (enum)
│   └── CategoriaCliente.java   ← no cambia (enum)
├── repositorio/
│   └── ClienteRepository.java  ← se actualiza la interfaz
└── PizzeriaApp.java            ← se reescribe
```

> Después de eliminar, es normal que `mvn compile` dé errores porque `Pizza.java`, `Cliente.java` y `Pedido.java` todavía tienen el código viejo. En las siguientes secciones los reescribimos.

---

## 1.3 Agregar Dependencias al pom.xml

Necesitamos dos dependencias nuevas:
- **Hibernate** (`hibernate-core`): el ORM que traduce clases Java a tablas SQL. Cuando ponemos `@Entity` en una clase, Hibernate sabe que tiene que crear una tabla para ella.
- **H2** (`h2`): una base de datos completa escrita en Java. Se ejecuta dentro de nuestra aplicación sin instalar nada. Es como tener un MySQL portátil dentro del proyecto.
- **Gson** ya lo tenemos del día 7.

*📁 Archivo a modificar: `pom.xml`*

Reemplacen la sección `<dependencies>` completa:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.pizzeria</groupId>
    <artifactId>Pizzeria_Alumno</artifactId>
    <version>5.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Hibernate: el ORM que traduce objetos Java a SQL -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>6.4.4.Final</version>
        </dependency>

        <!-- H2: base de datos embebida en Java (no necesita instalacion) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
        </dependency>

        <!-- Gson: para exportar a JSON (lo tenemos del dia 7) -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.11.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Plugin para ejecutar desde terminal con mvn exec:java -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>com.pizzeria.PizzeriaApp</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

Después de guardar, hagan clic en el icono del elefante de Maven para recargar dependencias.

**Dependencias del proyecto:**

| Dependencia | Para qué |
|-------------|----------|
| `org.hibernate.orm:hibernate-core:6.4.4.Final` | ORM: mapea clases Java a tablas |
| `com.h2database:h2:2.2.224` | Base de datos embebida en memoria |
| `com.google.code.gson:gson:2.11.0` | Exportar/importar JSON (día 7) |

---

## 1.4 Crear persistence.xml

Este archivo es la configuración central de Hibernate. Le dice tres cosas:
1. **Cuáles son mis entidades** (las clases con `@Entity` que tiene que gestionar)
2. **Dónde está la base de datos** (URL, usuario, contraseña)
3. **Cómo comportarse** (crear tablas automáticamente, mostrar SQL, etc.)

*📁 Archivo a crear: `src/main/resources/META-INF/persistence.xml`*

Primero creen la carpeta `META-INF` dentro de `resources/`:

```
Pizzeria_Alumno/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/pizzeria/...
│       └── resources/
│           └── META-INF/          ← CREAR esta carpeta
│               └── persistence.xml ← CREAR este archivo
├── pom.xml
└── .gitignore
```

Contenido completo de `persistence.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
             https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">

    <persistence-unit name="pizzeria-pu" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <!-- Nuestras entidades -->
        <class>com.pizzeria.modelo.Pizza</class>
        <class>com.pizzeria.modelo.Cliente</class>
        <class>com.pizzeria.modelo.Pedido</class>

        <properties>
            <!-- Conexion a H2 en memoria -->
            <property name="jakarta.persistence.jdbc.driver"
                      value="org.h2.Driver"/>
            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:h2:mem:pizzeriadb"/>
            <property name="jakarta.persistence.jdbc.user"
                      value="sa"/>
            <property name="jakarta.persistence.jdbc.password"
                      value=""/>

            <!-- Hibernate crea las tablas automaticamente -->
            <property name="hibernate.hbm2ddl.auto"
                      value="create-drop"/>

            <!-- Mostrar las sentencias SQL en consola -->
            <property name="hibernate.show_sql"
                      value="true"/>
            <property name="hibernate.format_sql"
                      value="true"/>

            <!-- Dialecto para H2 -->
            <property name="hibernate.dialect"
                      value="org.hibernate.dialect.H2Dialect"/>
        </properties>
    </persistence-unit>

</persistence>
```

**Explicación de cada propiedad:**

| Propiedad | Significado |
|-----------|-------------|
| `persistence-unit name` | Nombre que usamos para obtener el `EntityManagerFactory` |
| `transaction-type` | `RESOURCE_LOCAL` = nosotros manejamos las transacciones |
| `<class>` | Cada entidad que Hibernate debe gestionar |
| `jdbc.url` | `jdbc:h2:mem:pizzeriadb` = base de datos en memoria llamada "pizzeriadb" |
| `hbm2ddl.auto` | `create-drop` = crea tablas al iniciar, las borra al cerrar |
| `show_sql` | Muestra cada SQL que Hibernate genera (muy útil para aprender) |
| `format_sql` | Formatea el SQL para que sea legible |

---

# PARTE II — ANOTAR LAS ENTIDADES

Aquí es donde ocurre la transformación real. Agregar anotaciones JPA a nuestras clases les dice a Hibernate: "esta clase se mapea a una tabla, estos campos son columnas, este campo es la clave primaria."

Pero no es solo poner anotaciones. Hibernate necesita que las clases cumplan ciertas reglas:

```
+------------------------------------------------------------+
|  QUE NECESITA HIBERNATE DE UNA CLASE                        |
+------------------------------------------------------------+
|                                                             |
|  1. @Entity en la clase --> "yo soy una tabla"              |
|  2. Un campo @Id --> "esta es mi clave primaria"            |
|  3. Constructor sin argumentos --> Hibernate lo usa          |
|     internamente para crear objetos cuando lee la BD        |
|  4. Getters y setters --> Hibernate los usa para llenar      |
|     los campos del objeto                                   |
|  5. NO puede ser final ni abstract --> Hibernate crea        |
|     proxies que heredan de tu clase                          |
|                                                             |
+------------------------------------------------------------+
```

**¿Por qué un constructor vacío?** Hibernate usa reflexión para crear objetos. Cuando lee una fila de la base de datos, primero crea un objeto vacío con ese constructor, y DESPUÉS le llena los campos uno por uno con los setters. Sin constructor vacío → `InstantiationException`.

## 2.1 Pizza.java como @Entity

Vamos a reescribir `Pizza.java` como una entidad JPA. La versión original era abstracta con herencia (`PizzaClasica`, `PizzaGourmet`). Para Hibernate, la simplificamos: una clase concreta con un `enum Categoria` que indica el tipo de pizza.

*📁 Archivo a modificar: `src/main/java/com/pizzeria/modelo/Pizza.java`*

Reemplacen el contenido completo con esta versión adaptada para JPA:

```java
package com.pizzeria.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad Pizza - version JPA.
 *
 * Esta version simplifica la pizza original para poder persistirla
 * con Hibernate. La jerarquia de herencia (PizzaClasica, PizzaGourmet)
 * se puede mapear despues con @Inheritance.
 *
 * Cada anotacion le dice algo a Hibernate:
 * - @Entity: "esta clase se mapea a una tabla"
 * - @Table: "la tabla se llama 'pizzas'"
 * - @Id: "este campo es la clave primaria"
 * - @GeneratedValue: "genera el id automaticamente (autoincrement)"
 * - @Enumerated: "guarda el enum como texto, no como numero"
 */
@Entity
@Table(name = "pizzas")
public class Pizza {

    // =====================================================
    // Enum para las categorias de pizza
    // =====================================================

    public enum Categoria {
        CLASICA,
        GOURMET,
        VEGANA,
        SIN_GLUTEN
    }

    // =====================================================
    // Campos persistentes
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private double precio;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    // =====================================================
    // Constructores
    // =====================================================

    /**
     * Constructor vacio: OBLIGATORIO para Hibernate.
     * Hibernate necesita crear el objeto primero y luego llenar los campos.
     * Puede ser protected (no necesita ser publico).
     */
    protected Pizza() {
    }

    /**
     * Constructor para crear pizzas desde el codigo.
     */
    public Pizza(String nombre, double precio, Categoria categoria) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que 0");
        }
        this.nombre = nombre.trim();
        this.precio = precio;
        this.categoria = categoria;
    }

    // =====================================================
    // Getters y Setters
    // =====================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    // =====================================================
    // toString
    // =====================================================

    @Override
    public String toString() {
        return String.format("Pizza{id=%d, nombre='%s', precio=%.2f, categoria=%s}",
                id, nombre, precio, categoria);
    }
}
```

**Puntos clave:**

```
╔══════════════════════════════════════════════════╗
║  REGLAS PARA UNA ENTIDAD JPA                    ║
╠══════════════════════════════════════════════════╣
║                                                  ║
║  1. @Entity en la clase                          ║
║  2. Un campo @Id (clave primaria)                ║
║  3. Constructor sin argumentos (puede ser        ║
║     protected)                                   ║
║  4. Getters y setters para los campos            ║
║     persistentes                                 ║
║  5. NO puede ser final ni abstract               ║
║                                                  ║
╚══════════════════════════════════════════════════╝
```

---

## 2.2 Cliente.java como @Entity

*📁 Archivo a modificar: `src/main/java/com/pizzeria/modelo/Cliente.java`*

Reemplacen el contenido completo:

```java
package com.pizzeria.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad Cliente - version JPA.
 *
 * @Enumerated(EnumType.STRING) guarda el valor del enum como texto.
 * Sin esta anotacion, Hibernate guardaria el ORDINAL (0, 1, 2),
 * que se rompe si alguien reordena los valores del enum.
 *
 * SIEMPRE usen EnumType.STRING. Nunca el ordinal.
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private TipoCliente tipo;

    private String email;

    private String telefono;

    @Enumerated(EnumType.STRING)
    private CategoriaCliente categoria;

    // =====================================================
    // Constructores
    // =====================================================

    /**
     * Constructor vacio: OBLIGATORIO para Hibernate.
     */
    protected Cliente() {
    }

    /**
     * Constructor para crear clientes desde el codigo.
     */
    public Cliente(String nombre, TipoCliente tipo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de cliente es obligatorio");
        }
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.categoria = CategoriaCliente.BRONCE;
    }

    // =====================================================
    // Getters y Setters
    // =====================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoCliente getTipo() { return tipo; }
    public void setTipo(TipoCliente tipo) { this.tipo = tipo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public CategoriaCliente getCategoria() { return categoria; }
    public void setCategoria(CategoriaCliente categoria) { this.categoria = categoria; }

    // =====================================================
    // toString
    // =====================================================

    @Override
    public String toString() {
        return String.format("[%s] %s (#%d) - %s",
                tipo, nombre, id, categoria.getDescripcion());
    }
}
```

---

## 2.3 Pedido.java como @Entity — las relaciones

Pizza y Cliente son entidades independientes: cada una vive en su propia tabla. Pero un Pedido no tiene sentido sin un Cliente, y un Pedido contiene varias Pizzas. En una base de datos, estas conexiones se representan con **claves foráneas** y **tablas intermedias**. En Java, son simplemente referencias entre objetos. El trabajo de Hibernate es traducir entre estos dos mundos.

### Las dos relaciones del Pedido

**`@ManyToOne` — Muchos pedidos pertenecen a UN cliente:**

En la base de datos, esto se traduce a una columna `cliente_id` en la tabla `pedidos`. Esa columna guarda el id del cliente al que pertenece el pedido. Es la relación más común: facturas→clientes, empleados→departamentos, comentarios→posts.

```
Tabla "pedidos":
| id | cliente_id | fecha      | total |
|----|------------|------------|-------|
| 1  | 1          | 2026-03-06 | 27.00 |   <-- cliente_id apunta a clientes.id
| 2  | 2          | 2026-03-06 | 31.50 |
```

**`@ManyToMany` — Un pedido tiene MUCHAS pizzas, y una pizza está en MUCHOS pedidos:**

Esto no se puede resolver con una sola columna. No podemos poner `pizza_id` en `pedidos` (porque un pedido tiene varias pizzas), ni `pedido_id` en `pizzas` (porque una pizza está en varios pedidos). La solución: una **tabla intermedia** que solo tiene dos columnas.

```
Tabla "pedido_pizzas" (Hibernate la crea automaticamente):
| pedido_id | pizza_id |
|-----------|----------|
| 1         | 1        |   <-- pedido 1 tiene Margarita
| 1         | 3        |   <-- pedido 1 tiene Trufa Negra
| 2         | 2        |   <-- pedido 2 tiene Carnivora
| 2         | 1        |   <-- pedido 2 tiene Margarita
```

Esta tabla NO tiene una clase Java. No van a crear `PedidoPizzas.java`. Hibernate la crea y la mantiene SOLO porque ve `@ManyToMany` en el código.

*📁 Archivo a modificar: `src/main/java/com/pizzeria/modelo/Pedido.java`*

Reemplacen el contenido completo:

```java
package com.pizzeria.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Pedido - version JPA con relaciones.
 *
 * RELACIONES:
 *
 * @ManyToOne  -> Muchos pedidos pertenecen a UN cliente.
 *   En la tabla: columna "cliente_id" que apunta a la tabla "clientes".
 *
 * @ManyToMany -> Un pedido tiene MUCHAS pizzas, y una pizza puede estar
 *   en MUCHOS pedidos. Hibernate crea una tabla intermedia automaticamente.
 *
 * @JoinTable  -> Define la tabla intermedia para la relacion ManyToMany.
 *   - name: nombre de la tabla intermedia
 *   - joinColumns: columna que apunta a ESTA entidad (pedido_id)
 *   - inverseJoinColumns: columna que apunta a la OTRA entidad (pizza_id)
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToMany
    @JoinTable(
        name = "pedido_pizzas",
        joinColumns = @JoinColumn(name = "pedido_id"),
        inverseJoinColumns = @JoinColumn(name = "pizza_id")
    )
    private List<Pizza> pizzas = new ArrayList<>();

    private LocalDateTime fecha;

    private double total;

    // =====================================================
    // Constructores
    // =====================================================

    /**
     * Constructor vacio: OBLIGATORIO para Hibernate.
     */
    protected Pedido() {
    }

    /**
     * Constructor para crear pedidos desde el codigo.
     */
    public Pedido(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio");
        }
        this.cliente = cliente;
        this.pizzas = new ArrayList<>();
        this.fecha = LocalDateTime.now();
        this.total = 0.0;
    }

    // =====================================================
    // Metodos de negocio
    // =====================================================

    /**
     * Agrega una pizza al pedido y recalcula el total.
     */
    public void agregarPizza(Pizza pizza) {
        if (pizza == null) {
            throw new IllegalArgumentException("La pizza no puede ser nula");
        }
        this.pizzas.add(pizza);
        recalcularTotal();
    }

    /**
     * Recalcula el total sumando los precios de todas las pizzas.
     */
    public void recalcularTotal() {
        this.total = pizzas.stream()
                .mapToDouble(Pizza::getPrecio)
                .sum();
    }

    // =====================================================
    // Getters y Setters
    // =====================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public List<Pizza> getPizzas() { return pizzas; }
    public void setPizzas(List<Pizza> pizzas) { this.pizzas = pizzas; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public int getCantidadItems() { return pizzas.size(); }

    // =====================================================
    // toString
    // =====================================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Pedido{id=%d, cliente='%s', pizzas=%d, total=%.2f, fecha=%s}",
                id,
                cliente != null ? cliente.getNombre() : "sin cliente",
                pizzas.size(),
                total,
                fecha));
        return sb.toString();
    }
}
```

---

### Anotaciones JPA de relaciones

```
╔═══════════════════════════════════════════════════════════════════╗
║  ANOTACIONES JPA DE RELACIONES                                   ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  @ManyToOne                                                       ║
║    Muchos pedidos -> un cliente                                   ║
║    Hibernate agrega columna "cliente_id" a la tabla pedidos       ║
║    Es la relacion mas comun en cualquier aplicacion               ║
║                                                                   ║
║  @ManyToMany                                                      ║
║    Muchos pedidos <-> muchas pizzas                               ║
║    Hibernate crea una TABLA INTERMEDIA (pedido_pizzas)            ║
║    La tabla tiene dos columnas: pedido_id y pizza_id              ║
║                                                                   ║
║  @JoinColumn(name = "cliente_id")                                 ║
║    Nombre de la columna FK en la tabla del pedido                 ║
║    Sin esto, Hibernate inventa un nombre (cliente_id igualmente,  ║
║    pero mejor ser explicitos)                                     ║
║                                                                   ║
║  @JoinTable(name = "pedido_pizzas", ...)                          ║
║    Configura la tabla intermedia del ManyToMany:                  ║
║    - joinColumns: columna que apunta a "esta" entidad             ║
║    - inverseJoinColumns: columna que apunta a "la otra"           ║
║                                                                   ║
║  @Enumerated(EnumType.STRING)                                     ║
║    Guarda el enum como texto ("CLASICA", "GOURMET")               ║
║    Sin esta anotacion, Hibernate guarda el ordinal (0, 1, 2)     ║
║    Regla: SIEMPRE usar STRING, nunca ORDINAL                     ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

---

# PARTE III — REPOSITORIOS CON ENTITYMANAGER

Ahora reemplazamos la implementación que usa `HashMap` por una que usa `EntityManager`. La interfaz del repositorio se mantiene (ese es el poder del patrón Repository): los servicios que usen estos repositorios no necesitan saber si los datos están en memoria, en H2, o en PostgreSQL.

### El patrón de transacción

Cada escritura en la base de datos sigue SIEMPRE el mismo patrón:

```
1. em.getTransaction().begin()     --> abrir transaccion
2. em.persist(objeto) / merge()    --> ejecutar la operacion
3. em.getTransaction().commit()    --> confirmar: los datos se escriben
4. Si falla: rollback()            --> deshacer todo

Ejemplo real: una transferencia bancaria.
Sacar 100 EUR de cuenta A y meterlos en cuenta B son DOS operaciones.
Si la segunda falla, la primera tiene que deshacerse.
Sin transacciones, podrian perder dinero.
```

### Equivalencias JPA / SQL

Cada método de `EntityManager` tiene un equivalente SQL directo:

| Método JPA | Equivalente SQL |
|------------|-----------------|
| `em.persist(pizza)` | `INSERT INTO pizzas (nombre, precio, categoria) VALUES (...)` |
| `em.merge(pizza)` | `UPDATE pizzas SET nombre=?, precio=? WHERE id=?` |
| `em.find(Pizza.class, id)` | `SELECT * FROM pizzas WHERE id = ?` |
| `em.remove(pizza)` | `DELETE FROM pizzas WHERE id = ?` |
| `em.createQuery("SELECT p FROM Pizza p", ...)` | `SELECT * FROM pizzas` |

> `"SELECT p FROM Pizza p"` NO es SQL. Es JPQL (Java Persistence Query Language). Usa nombres de CLASES (`Pizza` con mayúscula), no de tablas (`pizzas`). Hibernate lo traduce a SQL real.

## 3.1 PizzaRepositoryJPA

### Interfaz actualizada

*📁 Archivo a modificar: `src/main/java/com/pizzeria/repositorio/PizzaRepository.java`*

```java
package com.pizzeria.repositorio;

import com.pizzeria.modelo.Pizza;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de pizzas.
 * Ahora trabaja con Long como id (antes no tenia id propio).
 */
public interface PizzaRepository {

    Pizza guardar(Pizza pizza);

    Optional<Pizza> buscarPorId(Long id);

    List<Pizza> listarTodas();

    void eliminar(Long id);
}
```

### Implementación con EntityManager

*📁 Archivo a crear: `src/main/java/com/pizzeria/repositorio/PizzaRepositoryJPA.java`*

```java
package com.pizzeria.repositorio;

import com.pizzeria.modelo.Pizza;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion del repositorio de pizzas usando JPA/Hibernate.
 *
 * PATRON: cada operacion sigue estos pasos:
 * 1. Abrir transaccion
 * 2. Ejecutar la operacion
 * 3. Confirmar (commit)
 * 4. Si algo falla: deshacer (rollback)
 *
 * Este patron es IDENTICO para todas las entidades.
 * En Spring, @Transactional hace esto automaticamente.
 */
public class PizzaRepositoryJPA implements PizzaRepository {

    private final EntityManager em;

    public PizzaRepositoryJPA(EntityManager em) {
        this.em = em;
    }

    @Override
    public Pizza guardar(Pizza pizza) {
        try {
            em.getTransaction().begin();
            if (pizza.getId() == null) {
                em.persist(pizza);   // INSERT: pizza nueva
            } else {
                em.merge(pizza);     // UPDATE: pizza existente
            }
            em.getTransaction().commit();
            return pizza;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    @Override
    public Optional<Pizza> buscarPorId(Long id) {
        Pizza pizza = em.find(Pizza.class, id);
        return Optional.ofNullable(pizza);
    }

    @Override
    public List<Pizza> listarTodas() {
        return em.createQuery("SELECT p FROM Pizza p", Pizza.class)
                .getResultList();
    }

    @Override
    public void eliminar(Long id) {
        try {
            em.getTransaction().begin();
            Pizza pizza = em.find(Pizza.class, id);
            if (pizza != null) {
                em.remove(pizza);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
}
```

**Equivalencias JPA / SQL:**

| Método JPA | Equivalente SQL |
|------------|-----------------|
| `em.persist(pizza)` | `INSERT INTO pizzas (nombre, precio, categoria) VALUES (...)` |
| `em.merge(pizza)` | `UPDATE pizzas SET nombre=?, precio=? WHERE id=?` |
| `em.find(Pizza.class, id)` | `SELECT * FROM pizzas WHERE id = ?` |
| `em.remove(pizza)` | `DELETE FROM pizzas WHERE id = ?` |
| `em.createQuery("SELECT p FROM Pizza p", ...)` | `SELECT * FROM pizzas` |

> Nota: `"SELECT p FROM Pizza p"` NO es SQL. Es JPQL (Java Persistence Query Language). Usa nombres de CLASES, no de tablas. Hibernate lo traduce a SQL real.

---

## 3.2 ClienteRepositoryJPA

*📁 Archivo a crear: `src/main/java/com/pizzeria/repositorio/ClienteRepositoryJPA.java`*

```java
package com.pizzeria.repositorio;

import com.pizzeria.modelo.Cliente;
import com.pizzeria.modelo.TipoCliente;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion del repositorio de clientes usando JPA/Hibernate.
 * Mismo patron que PizzaRepositoryJPA: transaccion -> operacion -> commit.
 */
public class ClienteRepositoryJPA implements ClienteRepository {

    private final EntityManager em;

    public ClienteRepositoryJPA(EntityManager em) {
        this.em = em;
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        try {
            em.getTransaction().begin();
            if (cliente.getId() == null) {
                em.persist(cliente);
            } else {
                em.merge(cliente);
            }
            em.getTransaction().commit();
            return cliente;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        Cliente cliente = em.find(Cliente.class, id);
        return Optional.ofNullable(cliente);
    }

    @Override
    public List<Cliente> buscarTodos() {
        return em.createQuery("SELECT c FROM Cliente c", Cliente.class)
                .getResultList();
    }

    @Override
    public Optional<Cliente> buscarPorNombre(String nombre) {
        List<Cliente> resultados = em.createQuery(
                "SELECT c FROM Cliente c WHERE LOWER(c.nombre) = LOWER(:nombre)",
                Cliente.class)
                .setParameter("nombre", nombre)
                .getResultList();
        return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    }

    @Override
    public List<Cliente> buscarPorTipo(TipoCliente tipo) {
        return em.createQuery(
                "SELECT c FROM Cliente c WHERE c.tipo = :tipo",
                Cliente.class)
                .setParameter("tipo", tipo)
                .getResultList();
    }

    @Override
    public void eliminar(Long id) {
        try {
            em.getTransaction().begin();
            Cliente cliente = em.find(Cliente.class, id);
            if (cliente != null) {
                em.remove(cliente);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
}
```

> Fíjense que `buscarPorNombre` y `buscarPorTipo` usan JPQL con parámetros (`:nombre`, `:tipo`). Esto previene inyección SQL: NUNCA concatenen valores en las consultas.

### Actualizar la interfaz ClienteRepository

*📁 Archivo a modificar: `src/main/java/com/pizzeria/repositorio/ClienteRepository.java`*

```java
package com.pizzeria.repositorio;

import com.pizzeria.modelo.Cliente;
import com.pizzeria.modelo.TipoCliente;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository {

    Cliente guardar(Cliente cliente);

    Optional<Cliente> buscarPorId(Long id);

    List<Cliente> buscarTodos();

    Optional<Cliente> buscarPorNombre(String nombre);

    List<Cliente> buscarPorTipo(TipoCliente tipo);

    void eliminar(Long id);
}
```

---

# PARTE IV — EJECUTAR Y VERIFICAR

## 4.1 PizzeriaApp con Hibernate

Ahora el `main` inicializa el `EntityManagerFactory`, crea los repositorios JPA y trabaja con la base de datos.

*📁 Archivo a modificar: `src/main/java/com/pizzeria/PizzeriaApp.java`*

```java
package com.pizzeria;

import com.pizzeria.modelo.Cliente;
import com.pizzeria.modelo.CategoriaCliente;
import com.pizzeria.modelo.Pedido;
import com.pizzeria.modelo.Pizza;
import com.pizzeria.modelo.TipoCliente;
import com.pizzeria.repositorio.ClienteRepositoryJPA;
import com.pizzeria.repositorio.PizzaRepositoryJPA;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

/**
 * Aplicacion principal de la Pizzeria - Version 5 (Hibernate).
 *
 * CAMBIO PRINCIPAL:
 * Antes: repositorios usaban HashMap (datos en RAM).
 * Ahora: repositorios usan EntityManager (datos en H2).
 *
 * La logica de negocio NO cambio. Solo cambio el DONDE se guardan los datos.
 */
public class PizzeriaApp {

    public static void main(String[] args) {

        System.out.println("=============================================");
        System.out.println("   PIZZERIA JAVA - Version 5 (Hibernate)");
        System.out.println("=============================================\n");

        // ==========================================================
        // 1. INICIALIZAR HIBERNATE
        //    EntityManagerFactory lee persistence.xml y configura todo
        // ==========================================================

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pizzeria-pu");
        EntityManager em = emf.createEntityManager();

        System.out.println("Hibernate inicializado. Base de datos H2 lista.\n");

        // ==========================================================
        // 2. CREAR REPOSITORIOS JPA
        //    Antes: new PizzaRepositoryMemoria()
        //    Ahora: new PizzaRepositoryJPA(em)
        // ==========================================================

        PizzaRepositoryJPA pizzaRepo = new PizzaRepositoryJPA(em);
        ClienteRepositoryJPA clienteRepo = new ClienteRepositoryJPA(em);

        // ==========================================================
        // 3. CREAR PIZZAS Y GUARDARLAS EN LA BASE DE DATOS
        // ==========================================================

        System.out.println("--- CREAR PIZZAS ---\n");

        Pizza margarita = new Pizza("Margarita", 8.50, Pizza.Categoria.CLASICA);
        Pizza carnivora = new Pizza("Carnivora", 12.00, Pizza.Categoria.CLASICA);
        Pizza trufa = new Pizza("Trufa Negra", 18.50, Pizza.Categoria.GOURMET);
        Pizza vegana = new Pizza("Mediterranea Vegana", 11.00, Pizza.Categoria.VEGANA);

        pizzaRepo.guardar(margarita);
        pizzaRepo.guardar(carnivora);
        pizzaRepo.guardar(trufa);
        pizzaRepo.guardar(vegana);

        System.out.println("Pizzas guardadas en la base de datos:");
        List<Pizza> todasLasPizzas = pizzaRepo.listarTodas();
        todasLasPizzas.forEach(System.out::println);

        // ==========================================================
        // 4. CREAR CLIENTES Y GUARDARLOS
        // ==========================================================

        System.out.println("\n--- CREAR CLIENTES ---\n");

        Cliente ana = new Cliente("Ana Lopez", TipoCliente.PERSONA);
        ana.setEmail("ana.lopez@gmail.com");

        Cliente carlos = new Cliente("Carlos Martinez", TipoCliente.PERSONA);

        Cliente banco = new Cliente("Banco Santander", TipoCliente.EMPRESA);
        banco.setEmail("pedidos@santander.es");
        banco.setTelefono("911234567");

        clienteRepo.guardar(ana);
        clienteRepo.guardar(carlos);
        clienteRepo.guardar(banco);

        System.out.println("Clientes guardados:");
        clienteRepo.buscarTodos().forEach(System.out::println);

        // ==========================================================
        // 5. CREAR UN PEDIDO CON RELACIONES
        //    Aqui se ven @ManyToOne y @ManyToMany en accion
        // ==========================================================

        System.out.println("\n--- CREAR PEDIDO ---\n");

        try {
            em.getTransaction().begin();

            Pedido pedido1 = new Pedido(ana);
            pedido1.agregarPizza(margarita);
            pedido1.agregarPizza(trufa);
            em.persist(pedido1);

            Pedido pedido2 = new Pedido(carlos);
            pedido2.agregarPizza(carnivora);
            pedido2.agregarPizza(vegana);
            pedido2.agregarPizza(margarita);
            em.persist(pedido2);

            Pedido pedidoBanco = new Pedido(banco);
            pedidoBanco.agregarPizza(margarita);
            pedidoBanco.agregarPizza(margarita);
            pedidoBanco.agregarPizza(carnivora);
            em.persist(pedidoBanco);

            em.getTransaction().commit();

            System.out.println("Pedidos guardados:");
            System.out.println("  " + pedido1);
            System.out.println("  " + pedido2);
            System.out.println("  " + pedidoBanco);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Error al crear pedidos: " + e.getMessage());
        }

        // ==========================================================
        // 6. CONSULTAR PEDIDOS DESDE LA BASE DE DATOS
        // ==========================================================

        System.out.println("\n--- CONSULTAR PEDIDOS ---\n");

        List<Pedido> todosPedidos = em.createQuery(
                "SELECT p FROM Pedido p", Pedido.class)
                .getResultList();

        System.out.println("Total de pedidos en la base de datos: " + todosPedidos.size());
        for (Pedido p : todosPedidos) {
            System.out.printf("  Pedido #%d - Cliente: %s - Pizzas: %d - Total: %.2f%n",
                    p.getId(),
                    p.getCliente().getNombre(),
                    p.getCantidadItems(),
                    p.getTotal());
        }

        // ==========================================================
        // 7. RELACIONES EN ACCION: acceder al cliente desde el pedido
        // ==========================================================

        System.out.println("\n--- RELACIONES EN ACCION ---\n");

        Pedido primerPedido = todosPedidos.get(0);
        System.out.println("Pedido #" + primerPedido.getId() + ":");
        System.out.println("  Cliente: " + primerPedido.getCliente().getNombre());
        System.out.println("  Tipo: " + primerPedido.getCliente().getTipo());
        System.out.println("  Pizzas:");
        for (Pizza pizza : primerPedido.getPizzas()) {
            System.out.printf("    - %s (%.2f)%n", pizza.getNombre(), pizza.getPrecio());
        }

        // ==========================================================
        // 8. BUSCAR POR ID
        // ==========================================================

        System.out.println("\n--- BUSCAR POR ID ---\n");

        pizzaRepo.buscarPorId(1L).ifPresent(pizza ->
                System.out.println("Pizza con id 1: " + pizza));

        clienteRepo.buscarPorId(2L).ifPresent(cliente ->
                System.out.println("Cliente con id 2: " + cliente));

        // ==========================================================
        // 9. ELIMINAR
        //    Solo se puede eliminar una pizza que NO este en un pedido.
        //    Si intentan borrar una pizza que esta en pedido_pizzas,
        //    H2 lanza error de integridad referencial (FK constraint).
        // ==========================================================

        System.out.println("\n--- ELIMINAR ---\n");

        // Creamos una pizza extra que NO esta en ningun pedido
        Pizza temporal = new Pizza("Pizza Temporal", 5.00, Pizza.Categoria.CLASICA);
        pizzaRepo.guardar(temporal);

        System.out.println("Pizzas antes de eliminar: " + pizzaRepo.listarTodas().size());
        pizzaRepo.eliminar(temporal.getId());
        System.out.println("Pizzas despues de eliminar: " + pizzaRepo.listarTodas().size());

        // ==========================================================
        // 10. CERRAR RECURSOS
        // ==========================================================

        em.close();
        emf.close();

        System.out.println("\n=============================================");
        System.out.println("   Hibernate cerrado. Datos persistidos.");
        System.out.println("=============================================");
    }
}
```

---

## 4.2 Compilar y Ejecutar

Desde la terminal de IntelliJ:

```bash
mvn clean compile
```

Para ejecutar, usen el botón de play en IntelliJ (la clase `PizzeriaApp` tiene `main()`).

O desde la terminal:

```bash
mvn compile exec:java -Dexec.mainClass="com.pizzeria.PizzeriaApp"
```

### Lo que van a ver en la consola

Al ejecutar, Hibernate muestra las sentencias SQL que genera. Van a ver algo así:

```sql
Hibernate:
    create table pizzas (
        id bigint generated by default as identity,
        nombre varchar(255),
        precio double,
        categoria varchar(255),
        primary key (id)
    )

Hibernate:
    create table clientes (
        id bigint generated by default as identity,
        nombre varchar(255),
        tipo varchar(255),
        email varchar(255),
        telefono varchar(255),
        categoria varchar(255),
        primary key (id)
    )

Hibernate:
    create table pedidos (
        id bigint generated by default as identity,
        cliente_id bigint,
        fecha timestamp,
        total double,
        primary key (id)
    )

Hibernate:
    create table pedido_pizzas (
        pedido_id bigint not null,
        pizza_id bigint not null
    )
```

**Las tablas se crearon automáticamente.** Miren el SQL: Hibernate leyó las anotaciones `@Entity`, `@Table`, `@ManyToOne`, `@ManyToMany` y generó todo el DDL.

Después, al guardar pizzas:

```sql
Hibernate:
    insert into pizzas (categoria, nombre, precio, id)
    values (?, ?, ?, default)
```

Y al consultar pedidos:

```sql
Hibernate:
    select p1_0.id, p1_0.cliente_id, p1_0.fecha, p1_0.total
    from pedidos p1_0
```

> Cada `?` es un parámetro. Hibernate usa prepared statements por seguridad (previene inyección SQL).

---

## 4.3 Las Relaciones en la Base de Datos

Cuando crean un pedido con un cliente y pizzas, Hibernate hace varias operaciones:

```
1. INSERT INTO pedidos (cliente_id, fecha, total)    <- crea el pedido
2. INSERT INTO pedido_pizzas (pedido_id, pizza_id)   <- asocia pizza 1
3. INSERT INTO pedido_pizzas (pedido_id, pizza_id)   <- asocia pizza 2
```

La tabla `pedido_pizzas` es la **tabla intermedia** del `@ManyToMany`. Hibernate la crea y la mantiene automáticamente.

```
╔═══════════════════════════════════════════════════════════════════╗
║  COMO SE VEN LAS RELACIONES EN LA BASE DE DATOS                  ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  Tabla "clientes":                                                ║
║  | id | nombre         | tipo    | categoria | email     |        ║
║  |----|----------------|---------|-----------|-----------|        ║
║  | 1  | Ana Lopez      | PERSONA | BRONCE    | ana@...   |        ║
║  | 2  | Carlos Martinez| PERSONA | BRONCE    | null      |        ║
║                                                                   ║
║  Tabla "pizzas":                                                  ║
║  | id | nombre     | precio | categoria |                         ║
║  |----|------------|--------|-----------|                         ║
║  | 1  | Margarita  | 8.50   | CLASICA   |                         ║
║  | 2  | Carnivora  | 12.00  | CLASICA   |                         ║
║  | 3  | Trufa Negra| 18.50  | GOURMET   |                         ║
║                                                                   ║
║  Tabla "pedidos":                                                 ║
║  | id | cliente_id | fecha      | total |                         ║
║  |----|------------|------------|-------|                         ║
║  | 1  | 1          | 2026-03-06 | 27.00 |  <- Ana (id=1)          ║
║  | 2  | 2          | 2026-03-06 | 31.50 |  <- Carlos (id=2)       ║
║                                                                   ║
║  Tabla "pedido_pizzas" (intermedia @ManyToMany):                  ║
║  | pedido_id | pizza_id |                                         ║
║  |-----------|----------|                                         ║
║  | 1         | 1        |  <- pedido 1 tiene Margarita             ║
║  | 1         | 3        |  <- pedido 1 tiene Trufa Negra           ║
║  | 2         | 2        |  <- pedido 2 tiene Carnivora             ║
║  | 2         | 1        |  <- pedido 2 tiene Margarita             ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

---

# ═══════════════════════════════
# PARTE V — EJERCICIO INTEGRADOR
# ═══════════════════════════════

# EJERCICIO INTEGRADOR — Combo como @Entity

Conviertan la clase `Combo` en una entidad JPA. El combo tiene un nombre, una descripción, una lista de pizzas (relación `@ManyToMany`) y un precio especial.

## GPS Arquitectónico del Ejercicio

```
src/main/java/com/pizzeria/
├── modelo/
│   └── Combo.java                ← la entidad (DADO en el manual)
├── repositorio/
│   └── ComboRepositoryJPA.java   ← USTEDES lo crean
├── PizzeriaApp.java              ← USTEDES agregan el código
└── src/main/resources/META-INF/
    └── persistence.xml           ← USTEDES agregan el <class>
```

## Código de Combo.java (dado)

*📁 Archivo a crear: `src/main/java/com/pizzeria/modelo/Combo.java`*

```java
package com.pizzeria.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "combos")
public class Combo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String descripcion;

    @ManyToMany
    @JoinTable(
        name = "combo_pizzas",
        joinColumns = @JoinColumn(name = "combo_id"),
        inverseJoinColumns = @JoinColumn(name = "pizza_id")
    )
    private List<Pizza> pizzas = new ArrayList<>();

    private double precioEspecial;

    // Constructor vacio para Hibernate
    protected Combo() {
    }

    public Combo(String nombre, String descripcion, double precioEspecial) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioEspecial = precioEspecial;
    }

    // --- Metodos de negocio ---

    public void agregarPizza(Pizza pizza) {
        this.pizzas.add(pizza);
    }

    public double getAhorro() {
        double precioSinCombo = pizzas.stream()
                .mapToDouble(Pizza::getPrecio)
                .sum();
        return precioSinCombo - precioEspecial;
    }

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public List<Pizza> getPizzas() { return pizzas; }
    public void setPizzas(List<Pizza> pizzas) { this.pizzas = pizzas; }

    public double getPrecioEspecial() { return precioEspecial; }
    public void setPrecioEspecial(double precioEspecial) { this.precioEspecial = precioEspecial; }

    @Override
    public String toString() {
        return String.format("Combo{id=%d, nombre='%s', pizzas=%d, precio=%.2f}",
                id, nombre, pizzas.size(), precioEspecial);
    }
}
```

## Tareas del ejercicio

1. Agregar `<class>com.pizzeria.modelo.Combo</class>` en `persistence.xml`
2. Crear un `ComboRepositoryJPA` (implementación con `EntityManager`)
3. En el `main`: crear un combo "Familiar" con 2 pizzas, guardarlo, listarlo
4. Verificar que Hibernate crea la tabla `combos` y `combo_pizzas`

**Pistas (si se traban):**

1. El `ComboRepositoryJPA` sigue EXACTAMENTE el mismo patrón que `PizzaRepositoryJPA`: constructor con `EntityManager`, `guardar()` con `persist/merge`, `listarTodos()` con `createQuery`
2. En el `main`, necesitan una transacción para crear el combo con pizzas (igual que los pedidos)
3. Para listar combos: `em.createQuery("SELECT c FROM Combo c", Combo.class)`
4. El ahorro se calcula con `familiar.getAhorro()` — la suma de precios individuales menos el precio especial

---

# REFERENCIAS

## 5.1 GPS Arquitectónico: Antes y Después

```
╔═══════════════════════════════════════════════════════════════════╗
║               GPS ARQUITECTONICO - DIA 10                         ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  ANTES (dias 3-9):                                                ║
║                                                                   ║
║  PizzeriaApp                                                      ║
║       |                                                           ║
║       v                                                           ║
║  PedidoService / ClienteService                                   ║
║       |                                                           ║
║       v                                                           ║
║  PedidoRepositoryMemoria / ClienteRepositoryMemoria               ║
║       |                                                           ║
║       v                                                           ║
║  HashMap (RAM)                                                    ║
║  --- cierran la app = datos perdidos ---                          ║
║                                                                   ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  AHORA (dia 10):                                                  ║
║                                                                   ║
║  PizzeriaApp                                                      ║
║       |                                                           ║
║       v                                                           ║
║  Repositorios (PizzaRepositoryJPA, ClienteRepositoryJPA)          ║
║       |                                                           ║
║       v                                                           ║
║  EntityManager (JPA)                                              ║
║       |                                                           ║
║       v                                                           ║
║  Hibernate (ORM)                                                  ║
║       |                                                           ║
║       v                                                           ║
║  H2 Database (persiste datos)                                     ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

## 5.2 Cheat Sheet de Anotaciones JPA

| Anotación | Significado | Dónde va |
|-----------|-------------|----------|
| `@Entity` | Esta clase es una tabla | En la clase |
| `@Table(name = "...")` | Nombre de la tabla | En la clase |
| `@Id` | Clave primaria | En el campo id |
| `@GeneratedValue` | Id autoincremental | En el campo id |
| `@Enumerated(EnumType.STRING)` | Guardar enum como texto | En campos enum |
| `@ManyToOne` | Relación muchos-a-uno | En el campo de la relación |
| `@ManyToMany` | Relación muchos-a-muchos | En la colección |
| `@JoinColumn(name = "...")` | Nombre de la columna FK | Junto a `@ManyToOne` |
| `@JoinTable(...)` | Tabla intermedia | Junto a `@ManyToMany` |

## 5.3 Para Profundizar

**Documentación oficial:**
- Hibernate ORM User Guide: https://docs.jboss.org/hibernate/orm/6.4/userguide/html_single/Hibernate_User_Guide.html
- Jakarta Persistence (JPA) spec: https://jakarta.ee/specifications/persistence/3.1/
- H2 Database docs: https://www.h2database.com/html/main.html

**Tutoriales recomendados:**
- Baeldung — JPA Relationships: https://www.baeldung.com/jpa-hibernate-associations
- Baeldung — Hibernate with H2: https://www.baeldung.com/hibernate-h2
- DigitalOcean — JPA Annotations: https://www.digitalocean.com/community/tutorials/jpa-hibernate-annotations

**Libro gratuito:**
- "Java Persistence with Hibernate" (resumen gratuito en manning.com)

---

# ═══════════════════════════════
# PARTE VI — CIERRE DEL DÍA
# ═══════════════════════════════

## Resumen: lo que lograron hoy

```
╔═══════════════════════════════════════════════════════════════════╗
║                    RESUMEN DEL DIA 10                             ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  QUE CAMBIO:                                                      ║
║  * pom.xml: se agrego hibernate-core y h2                         ║
║  * persistence.xml: configuracion de Hibernate                    ║
║  * Pizza, Cliente, Pedido: anotados con @Entity                   ║
║  * Repositorios: de HashMap a EntityManager                       ║
║  * PizzeriaApp: inicializa EntityManagerFactory                   ║
║                                                                   ║
║  QUE NO CAMBIO:                                                   ║
║  * Los enums (CategoriaCliente, TipoCliente, Categoria)           ║
║  * La logica de negocio (reglas de validacion)                    ║
║  * El patron Repository (interfaz + implementacion)               ║
║  * La arquitectura por capas                                      ║
║                                                                   ║
║  ANOTACIONES NUEVAS:                                              ║
║  @Entity          -> esta clase es una tabla                      ║
║  @Table           -> nombre de la tabla                           ║
║  @Id              -> clave primaria                               ║
║  @GeneratedValue  -> id autoincremental                           ║
║  @Enumerated      -> como guardar enums (siempre STRING)          ║
║  @ManyToOne       -> relacion muchos-a-uno (pedido->cliente)      ║
║  @ManyToMany      -> relacion muchos-a-muchos (pedido<->pizzas)   ║
║  @JoinColumn      -> nombre de la columna FK                      ║
║  @JoinTable       -> tabla intermedia para ManyToMany             ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
```

## Commit del día

```bash
git add -A
git commit -m "v5: integrar Hibernate en Pizzeria - entidades JPA, repositorios con EntityManager, H2"
git tag v5
```
