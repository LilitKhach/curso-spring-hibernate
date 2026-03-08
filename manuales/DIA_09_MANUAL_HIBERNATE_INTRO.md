# Día 9 — Hibernate: Tu primera base de datos con Java

> **Curso:** IFCD0014 — Java, Spring, Hibernate, Docker
> **Profesor:** Juan Marcelo Gutierrez Miranda
> **Objetivo del día:** Aprender ORM con Hibernate — crear entidades JPA, conectar a H2, hacer CRUD completo sin escribir SQL
> **Requisito:** Haber completado los días 6-8 (Maven completo)
> **Nota:** Este manual se proyecta en pantalla. Todo lo que necesitan está aquí. Si se pierden, pidan que vuelva a la sección.

---

# PARTE I — EL PROBLEMA Y LA SOLUCIÓN

# 1. El problema y la solución: ORM

## 1.1 La Pizzería tiene Alzheimer

Hasta ahora, la Pizzería guarda todo en `ArrayList`:

```java
// PizzaRepository.java (version actual)
private final List<Pizza> pizzas = new ArrayList<>();

public PizzaRepository() {
    pizzas.add(new Pizza("Margherita", 8.50, Categoria.CLASICA));
    pizzas.add(new Pizza("Pepperoni", 10.00, Categoria.CLASICA));
    // ...
}
```

**El problema:** cada vez que cierran la aplicación, se pierden los datos. Es como tener un restaurante que olvida todos los pedidos al cerrar la caja.

```
╔═════════════════════════════════════════════════════════════╗
║  SIN BASE DE DATOS:                                         ║
║                                                             ║
║  Ejecutar app → Crear pizza → Crear pedido → Cerrar app     ║
║  → ¡TODO PERDIDO!                                           ║
║  Ejecutar app → Empezar de cero...                          ║
║                                                             ║
║  CON BASE DE DATOS:                                         ║
║                                                             ║
║  Ejecutar app → Crear pizza → Crear pedido → Cerrar app     ║
║  Ejecutar app → Los datos siguen ahi  ← ESTO QUEREMOS       ║
╚═════════════════════════════════════════════════════════════╝
```

La solución obvia es usar una base de datos (SQL). Pero hay un problema...

---

## 1.2 El desajuste de impedancia: Objetos ≠ Tablas

En Java pensamos en **objetos**:

```java
Pizza margherita = new Pizza("Margherita", 8.50, Categoria.CLASICA);
margherita.getCategoria();  // devuelve un enum
```

En SQL pensamos en **tablas**:

```sql
CREATE TABLE pizzas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(5,2),
    categoria VARCHAR(20)
);
```

Las diferencias son reales:

| Java (objetos) | SQL (tablas) |
|----------------|-------------|
| Herencia (`class PizzaPremium extends Pizza`) | No existe herencia en tablas |
| Listas (`List<Pizza>`) | No existen listas, se usan JOINs |
| Enums (`Categoria.CLASICA`) | Strings o números |
| Referencias (`pedido.getCliente()`) | Foreign keys (IDs numéricos) |

Este choque entre el mundo orientado a objetos y el mundo relacional se llama **Object-Relational Impedance Mismatch**. Es el problema que Hibernate resuelve.

---

## 1.3 ¿Qué es un ORM?

### ¿Qué es?

**ORM = Object-Relational Mapping** (Mapeo Objeto-Relacional). Un ORM es un traductor automático entre objetos Java y tablas SQL. Ustedes escriben Java, el ORM escribe SQL.

### ¿Por qué existe?

Sin ORM, tendrían que escribir SQL a mano para cada operación de cada clase: `INSERT`, `SELECT`, `UPDATE`, `DELETE`, parsear `ResultSet`... Con 20 entidades, son cientos de queries manuales. El ORM automatiza todo eso.

### ¿Se usa siempre?

En la industria Java empresarial, el **90% de los proyectos** usan JPA/Hibernate. Es el estándar de facto. En cualquier empresa donde trabajen con Java, van a usar un ORM.

### Cómo funciona

```
  Mundo Java                    ORM                    Mundo SQL
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│ Pizza.java   │  ──────>│  Hibernate   │──────>  │ tabla pizzas │
│ - nombre     │         │  (traduce)   │         │ - nombre     │
│ - precio     │  <──────│              │<──────  │ - precio     │
│ - categoria  │         └──────────────┘         │ - categoria  │
└──────────────┘                                  └──────────────┘

  pizza.setNombre("Hawai")  →  UPDATE pizzas SET nombre='Hawai' WHERE id=3
  em.persist(pizza)         →  INSERT INTO pizzas (nombre, precio) VALUES (...)
  em.find(Pizza.class, 1)   →  SELECT * FROM pizzas WHERE id=1
  em.remove(pizza)          →  DELETE FROM pizzas WHERE id=3
```

### JPA vs Hibernate

- **JPA** (Jakarta Persistence API) es la **especificación** — define las anotaciones y las reglas
- **Hibernate** (framework ORM escrito en Java, el más usado del mundo) es la **implementación** — el motor que hace el trabajo

Es como la diferencia entre una interfaz y una clase en Java:

```java
// JPA es como esto:
public interface PersistenceProvider {
    void persist(Object entity);
    Object find(Class type, Object id);
}

// Hibernate es como esto:
public class HibernateProvider implements PersistenceProvider {
    // Toda la magia esta aqui dentro
}
```

Ustedes escriben código con anotaciones JPA (`@Entity`, `@Id`). Hibernate se encarga del SQL. Si mañana deciden cambiar Hibernate por EclipseLink (otro ORM), el código **no cambia**.

---

# PARTE II — PREPARAR EL PROYECTO HIBERNATE

# 2. Crear el proyecto

Vamos a crear un proyecto **separado** de la Pizzería para aprender Hibernate sin riesgo. Mañana lo traemos a la Pizzería.

## 2.1 Nuevo proyecto Maven: curso-hibernate

### Paso 1: Nuevo proyecto en IntelliJ

```
File → New → Project

  Name:           curso-hibernate
  Build system:   Maven
  JDK:            17
  GroupId:        com.cursojava
  ArtifactId:     curso-hibernate
```

### Paso 2: Configurar el pom.xml

*📁 Archivo a modificar: `pom.xml`*

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.cursojava</groupId>
    <artifactId>curso-hibernate</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Hibernate: el motor ORM -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>6.4.4.Final</version>
        </dependency>

        <!-- H2: base de datos en memoria (cero instalacion) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- exec-maven-plugin: ejecutar Main desde terminal -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>com.cursojava.Main</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

Solo dos dependencias: **Hibernate** para el ORM y **H2** (base de datos escrita en Java que corre en memoria — no necesita instalación, ideal para desarrollo y tests) para la base de datos. El plugin `exec-maven-plugin` permite ejecutar el `Main` desde terminal.

### Paso 3: Verificar que compila

```bash
mvn compile
```

Deben ver `BUILD SUCCESS`. Si no, revisen que el `pom.xml` esté bien escrito.

> **Nota:** La primera vez que compilan con Hibernate, Maven descarga muchas dependencias (~40 JARs). Puede tardar 2-3 minutos. Solo pasa la primera vez.

---

## 2.2 El archivo persistence.xml: la configuración de Hibernate

### ¿Qué es persistence.xml?

Es el **archivo de configuración de JPA**. Le dice a Hibernate CÓMO conectarse a la base de datos, QUÉ hacer con las tablas, y CÓMO comportarse (mostrar SQL, formatear, etc.).

### ¿Por qué existe?

Hibernate necesita saber: ¿qué base de datos?, ¿qué usuario?, ¿crear tablas automáticamente?, ¿mostrar el SQL generado?. Todo eso va en este archivo.

### ¿Dónde va?

**SIEMPRE** en `src/main/resources/META-INF/persistence.xml`. Es una ubicación estándar de JPA — si la cambian, Hibernate no lo encuentra.

### Crear el archivo

Ubicación **exacta** (si las carpetas no existen, créenlas):

```
curso-hibernate/
└── src/main/resources/
    └── META-INF/
        └── persistence.xml     ← CREAR ESTE ARCHIVO
```

### Contenido completo

*📁 Archivo a crear: `src/main/resources/META-INF/persistence.xml`*

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
             https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">

    <persistence-unit name="curso-hibernate-pu">
        <properties>
            <!-- Conexion a H2 en memoria -->
            <property name="jakarta.persistence.jdbc.driver"
                      value="org.h2.Driver"/>
            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:h2:mem:testdb"/>
            <property name="jakarta.persistence.jdbc.user"
                      value="sa"/>
            <property name="jakarta.persistence.jdbc.password"
                      value=""/>

            <!-- Hibernate crea las tablas automaticamente -->
            <property name="hibernate.hbm2ddl.auto"
                      value="create-drop"/>

            <!-- Mostrar el SQL que genera Hibernate -->
            <property name="hibernate.show_sql"
                      value="true"/>

            <!-- Formatear el SQL para que sea legible -->
            <property name="hibernate.format_sql"
                      value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

### Explicación de cada propiedad

| Propiedad | Valor | Qué hace |
|-----------|-------|----------|
| `jdbc.driver` | `org.h2.Driver` | Driver de la BD (H2) |
| `jdbc.url` | `jdbc:h2:mem:testdb` | URL de conexión (en memoria) |
| `jdbc.user` | `sa` | Usuario (default de H2) |
| `jdbc.password` | (vacío) | Sin contraseña |
| `hbm2ddl.auto` | `create-drop` | Crea tablas al iniciar, las borra al cerrar |
| `show_sql` | `true` | Imprime el SQL en consola |
| `format_sql` | `true` | Formatea el SQL bonito |

> **`create-drop`** significa: "Cada vez que ejecuto la app, borra las tablas viejas y las crea de nuevo." Perfecto para desarrollo. En producción usarían `validate` o `update`.

### ¿Hibernate crea la base de datos por nosotros?

**Sí, pero con matices.** Hibernate genera las tablas automáticamente a partir de las anotaciones `@Entity`, `@Table`, `@Column`, etc. No escriben ni una línea de SQL. Pero **ustedes controlan cuánta libertad le dan** con la propiedad `hbm2ddl.auto`:

| Valor | Qué hace | Cuándo usarlo |
|-------|----------|---------------|
| `create` | Borra las tablas y las recrea desde cero | Desarrollo inicial |
| `create-drop` | Crea al arrancar, borra al cerrar la app | Tests y aprendizaje |
| `update` | Añade columnas/tablas nuevas, NO borra nada | Desarrollo avanzado |
| `validate` | Solo verifica que las tablas coincidan, NO toca nada | Producción |
| `none` | No hace absolutamente nada | Producción real |

**Regla de oro:** En desarrollo y tests, usen `create-drop` o `update` — es cómodo y rápido. En producción, **NUNCA** usen `create` — borraría los datos de todos los clientes cada vez que reinicien el servidor. En producción se usa `validate` + herramientas de migración como Flyway o Liquibase que controlan los cambios de forma segura.

En este curso usamos `create-drop` con H2 (base de datos en memoria). Cuando pasemos a PostgreSQL, cambiaremos a `update` o `validate`.

### Desglose de la URL de conexión

```
jdbc : h2 : mem : testdb
 │      │     │      │
 │      │     │      └─ nombre de la BD
 │      │     └──────── tipo: en memoria
 │      └────────────── driver: H2
 └───────────────────── protocolo Java
```

> Si cambian `mem` por `file:./midb`, H2 guarda en disco en vez de en memoria (los datos sobreviven al reinicio). En producción se reemplaza toda la URL por la de PostgreSQL: `jdbc:postgresql://localhost:5432/pizzeria`.

---

# PARTE III — ENTIDADES Y CRUD

# 3. Entidades, EntityManager y CRUD

## 3.1 Primera entidad: Producto.java

### ¿Qué es una entidad?

Una **entidad** es una clase Java que JPA mapea a una tabla SQL. Se marca con anotaciones (`@Entity`, `@Id`, `@Column`). Cada instancia de la clase es una **fila** en la tabla.

### ¿Por qué existen las entidades?

Sin entidades, tendrían que escribir `CREATE TABLE`, `INSERT`, `SELECT` a mano y convertir `ResultSet` en objetos manualmente. Con entidades, Hibernate lee las anotaciones y genera el SQL automáticamente.

### ¿Dónde van?

En el paquete `modelo` (por convención). En Spring Boot verán también `entity` o `domain`.

### Crear la clase

*📁 Archivo a crear: `src/main/java/com/cursojava/modelo/Producto.java`*

```java
package com.cursojava.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

@Entity                              // "Esta clase es una tabla"
@Table(name = "productos")          // Nombre de la tabla en la BD
public class Producto {

    @Id                              // "Este campo es la Primary Key"
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    @Column(nullable = false, length = 100)   // NOT NULL, max 100 chars
    private String nombre;

    @Column                          // Columna normal (nullable por defecto)
    private double precio;

    @Column
    private int stock;

    // Constructor VACIO — OBLIGATORIO para JPA
    public Producto() {
    }

    // Constructor con datos (sin id — la BD lo genera)
    public Producto(String nombre, double precio, int stock) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String toString() {
        return "Producto{id=" + id + ", nombre='" + nombre + "', precio=" + precio + ", stock=" + stock + "}";
    }
}
```

> **Todos los imports vienen de `jakarta.persistence`**, que es JPA. No de `org.hibernate`. Ustedes escriben código JPA, no código Hibernate. Si ven `javax.persistence` en internet, cámbienlo a `jakarta.persistence` — es lo mismo pero actualizado (Jakarta EE reemplazó a Java EE).

> **El constructor vacío es OBLIGATORIO.** JPA necesita poder crear instancias de la clase sin argumentos para llenarlas con datos de la BD. Si no lo ponen, tendrán un error críptico: `No default constructor for entity`.

> **¿Por qué `Long` y no `int`?** Por convención en JPA se usa `Long` (objeto) en vez de `long` (primitivo). El objeto puede ser `null`, que es importante cuando el ID todavía no se ha generado.

### Explicación de cada anotación

| Anotación | Va en | Significa |
|-----------|-------|-----------|
| `@Entity` | La clase | "Soy una entidad JPA — mapéame a una tabla" |
| `@Table(name = "productos")` | La clase | "Mi tabla se llama `productos`" (si no lo pones, usa el nombre de la clase) |
| `@Id` | Un campo | "Soy la clave primaria" |
| `@GeneratedValue(IDENTITY)` | Un campo | "La BD genera el ID automáticamente (auto-increment)" |
| `@Column(nullable = false)` | Un campo | "Esta columna no puede ser NULL" |
| `@Column` | Un campo | "Esta es una columna normal" (pueden omitirlo si no necesitan configuración extra) |

---

## 3.2 El EntityManager: la puerta a la base de datos

### ¿Qué es el EntityManager?

El `EntityManager` es el objeto central de JPA. **Todas las operaciones con la BD pasan por él**: guardar, buscar, actualizar, eliminar. Es la "puerta de entrada" a la base de datos.

### ¿Por qué existe?

Sin EntityManager, tendrían que crear conexiones JDBC, preparar statements SQL, parsear `ResultSet` manualmente. El EntityManager abstrae todo eso en métodos simples como `persist()`, `find()`, `remove()`.

### ¿Dónde va?

Se crea en el `main()` usando `Persistence.createEntityManagerFactory()`. En Spring Boot, Spring lo crea automáticamente (lo verán en el día 11).

### Crear la clase Main

*📁 Archivo a crear: `src/main/java/com/cursojava/Main.java`*

```java
package com.cursojava;

import com.cursojava.modelo.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Main {

    public static void main(String[] args) {
        // 1. Crear la fabrica (lee persistence.xml)
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("curso-hibernate-pu");

        // 2. Crear el EntityManager (la "conexion")
        EntityManager em = emf.createEntityManager();

        System.out.println("=== Hibernate conectado a H2 ===\n");

        // ... aqui van las operaciones CRUD ...

        // 3. Cerrar todo (SIEMPRE)
        em.close();
        emf.close();

        System.out.println("\n=== Conexion cerrada ===");
    }
}
```

Tres pasos siempre iguales: **crear la fábrica**, **crear el EntityManager**, y al final **cerrar todo**. Siempre cierren el EntityManager y la fábrica.

> **El string `"curso-hibernate-pu"`** debe coincidir exactamente con el `persistence-unit name` del archivo `persistence.xml`. Si no coincide, Hibernate no encuentra la configuración.

### Ejecutar por primera vez

Desde IntelliJ: clic derecho en `Main.java` → **Run**.

O desde terminal:

```bash
mvn compile exec:java
```

**Observen la consola:** Hibernate imprime el SQL de creación de la tabla:

```sql
Hibernate:
    create table productos (
        id bigint generated by default as identity,
        nombre varchar(100) not null,
        precio float(53),
        stock integer,
        primary key (id)
    )
```

¡La tabla se creó sola! Ustedes no escribieron ni una línea de SQL. Hibernate leyó las anotaciones de `Producto.java` y generó el `CREATE TABLE` automáticamente. Fíjense cómo `@Column(nullable = false, length = 100)` se convirtió en `varchar(100) not null`.

---

## 3.3 Transacciones: la red de seguridad

### ¿Qué es una transacción?

Una **transacción** es un grupo de operaciones que se ejecutan como una unidad atómica. O se ejecutan TODAS, o no se ejecuta NINGUNA. Si algo falla a mitad de camino, se deshace todo (rollback) y la base de datos queda como estaba antes.

### Analogía: la transferencia bancaria

Imaginen una transferencia de 100€ de la cuenta A a la cuenta B:

```
1. Restar 100€ de la cuenta A
2. Sumar 100€ a la cuenta B
```

¿Qué pasa si el paso 1 se ejecuta pero el paso 2 falla (por ejemplo, se cae la red)? El dinero desaparece. Por eso ambas operaciones van dentro de una transacción: si falla cualquiera, se deshace todo.

### Las propiedades ACID

Las transacciones garantizan 4 propiedades (ACID):

| Propiedad | Significado | Ejemplo |
|-----------|-------------|---------|
| **Atomicidad** | Todo o nada | Si falla un INSERT de 3, no se guarda ninguno |
| **Consistencia** | La BD pasa de un estado válido a otro | No puede quedar un pedido sin cliente |
| **Isolation** | Las transacciones no se interfieren | Dos usuarios no ven datos a medio escribir |
| **Durabilidad** | Lo commiteado no se pierde | Aunque se apague el servidor, los datos están |

### Cómo se usan en JPA

```java
// 1. Abrir la transaccion
em.getTransaction().begin();

// 2. Hacer operaciones (INSERT, UPDATE, DELETE)
em.persist(producto);
producto.setPrecio(99.99);

// 3. Confirmar: todo se guarda de golpe
em.getTransaction().commit();

// Si algo falla ANTES del commit:
// em.getTransaction().rollback();  ← deshace todo
```

```
╔════════════════════════════════════════════════╗
║  Regla de oro de las transacciones:             ║
║                                                 ║
║  ¿Escriben en la BD?  → Transaccion obligatoria ║
║  ¿Solo leen?          → No necesitan transaccion ║
╚════════════════════════════════════════════════╝
```

> **En Spring Boot** no van a escribir `begin()` ni `commit()` a mano. Spring lo hace automáticamente con la anotación `@Transactional`. Pero es importante entender qué pasa por debajo.

---

## 3.4 Ciclo de vida de una entidad

Un objeto JPA pasa por **4 estados** durante su vida. Entender esto evita el 80% de los errores con Hibernate.

```
                    persist()
  ┌──────────┐    ─────────>    ┌──────────┐
  │ TRANSIENT │                  │ MANAGED  │
  │ (nuevo)   │    <─────────    │ (gestio- │
  └──────────┘      find()      │  nado)   │
       │                         └──────────┘
       │                           │      │
       │                   remove()│      │ clear() / close()
       │                           ▼      ▼
       │                    ┌──────────┐  ┌──────────┐
       │                    │ REMOVED  │  │ DETACHED │
       │                    │(eliminado│  │(desconec-│
       │                    └──────────┘  │  tado)   │
       │                                  └──────────┘
```

| Estado | Qué significa | Ejemplo |
|--------|--------------|---------|
| **Transient** | Objeto Java normal, la BD no sabe que existe | `new Producto("Laptop", 899.99, 15)` |
| **Managed** | Hibernate lo vigila. Cualquier cambio se detecta automáticamente | Después de `persist()` o `find()` |
| **Detached** | Fue managed pero ya no. Hibernate dejó de vigilarlo | Después de `em.close()` |
| **Removed** | Marcado para eliminación. Se borra al hacer commit | Después de `em.remove()` |

### ¿Por qué importa?

- **Dirty checking** solo funciona en estado **Managed**: si cambian un campo con `setPrecio()`, Hibernate detecta el cambio y genera el UPDATE automáticamente.
- Si cambian un objeto **Detached**, Hibernate NO se entera. Tendrían que usar `em.merge(objeto)` para reconectarlo.
- Si intentan hacer `persist()` de un objeto que ya tiene ID, Hibernate protesta (`detached entity passed to persist`).

> **Dirty Checking:** Hibernate guarda una "foto" de cada entidad managed cuando la lee. Al hacer `commit()`, compara la foto con el estado actual. Si algo cambió, genera el UPDATE automáticamente. Es como un detective que compara fotos de antes y después.

---

## 3.5 CRUD completo: las 4 operaciones sagradas

### CREATE — Guardar un producto

*📁 Archivo a modificar: `src/main/java/com/cursojava/Main.java`*

Agregar dentro del `main()` donde dice "aquí van las operaciones CRUD":

```java
// Iniciar transaccion (OBLIGATORIO para escrituras)
em.getTransaction().begin();

Producto laptop = new Producto("Laptop Lenovo", 899.99, 15);
em.persist(laptop);    // INSERT INTO productos ...

em.getTransaction().commit();

System.out.println("Producto guardado con ID: " + laptop.getId());
```

Observen la consola:

```sql
Hibernate:
    insert into productos (nombre, precio, stock)
    values (?, ?, ?)
```

> **`em.persist()`** es como decir: "Hibernate, guarda este objeto en la BD." Hibernate genera el INSERT automáticamente. Los `?` son parámetros — Hibernate usa prepared statements automáticamente, lo que previene SQL injection.

> **Antes del `persist`**, `laptop.getId()` vale `null`. **Después del `commit`**, vale `1` — la BD generó el ID y Hibernate lo trajo de vuelta al objeto.

> **EXPERIMENTA — Romper para entender:**
> 1. Comenten las líneas `begin()` y `commit()` y ejecuten. ¿Qué error sale? → `TransactionRequiredException`. Toda escritura necesita transacción.
> 2. Añadan `System.out.println("ID antes: " + laptop.getId())` ANTES del persist, y otro DESPUÉS del commit. Vean cómo el ID pasa de `null` a `1`.
> 3. Hagan persist del mismo objeto dos veces seguidas dentro de la misma transacción. ¿Qué pasa? → No da error, Hibernate lo detecta.

### READ — Buscar un producto

```java
// Por ID (no necesita transaccion)
Producto encontrado = em.find(Producto.class, 1L);
System.out.println("Encontrado: " + encontrado);
```

```sql
Hibernate:
    select p1_0.id, p1_0.nombre, p1_0.precio, p1_0.stock
    from productos p1_0
    where p1_0.id=?
```

> **`em.find()`** busca por primary key. Le pasan la clase y el ID. Devuelve el objeto o `null` si no existe.

> **EXPERIMENTA:**
> 1. Busquen un ID que no existe: `em.find(Producto.class, 999L)`. ¿Qué devuelve? → `null` (no lanza excepción).
> 2. ¿Necesitaron transacción para leer? → No. Solo las escrituras la necesitan.

### UPDATE — Modificar un producto

```java
em.getTransaction().begin();

Producto producto = em.find(Producto.class, 1L);
producto.setPrecio(799.99);    // Solo cambiar el campo

em.getTransaction().commit();  // Hibernate detecta el cambio y hace UPDATE
```

```sql
Hibernate:
    update productos
    set nombre=?, precio=?, stock=?
    where id=?
```

> **Dirty Checking:** No llaman a ningún método `update()`. Hibernate **detecta automáticamente** que el objeto cambió y genera el UPDATE al hacer commit. Hibernate guarda una "foto" del objeto cuando lo lee. Al hacer `commit`, compara la foto con el estado actual. Si algo cambió, genera el UPDATE. Esto se llama "dirty checking".

> **EXPERIMENTA — Dirty Checking en acción:**
> 1. Dentro de la transacción, hagan `setPrecio()` y luego vuelvan a poner el precio original antes del commit. ¿Hibernate genera UPDATE? → No, porque el valor final es igual al original.
> 2. Cambien dos campos a la vez (`setPrecio()` + `setStock()`). ¿Genera dos UPDATEs? → No, uno solo con ambos cambios.

### DELETE — Eliminar un producto

```java
em.getTransaction().begin();

Producto producto = em.find(Producto.class, 1L);
em.remove(producto);    // DELETE FROM productos WHERE id=1

em.getTransaction().commit();
```

> Primero buscan el objeto con `find`, después lo eliminan con `remove`. No pueden eliminar un objeto que no esté gestionado por el EntityManager.

> **EXPERIMENTA:**
> 1. Intenten hacer `em.remove(new Producto("Test", 1.0, 1))` sin buscarlo primero. ¿Qué pasa? → Error `IllegalArgumentException: Removing a detached instance`. Hay que buscarlo primero con `find()`.
> 2. Después del remove + commit, intenten buscar el mismo ID con `find()`. ¿Qué devuelve? → `null`.

### LISTAR TODOS — Con JPQL

```java
var query = em.createQuery("SELECT p FROM Producto p", Producto.class);
var productos = query.getResultList();

System.out.println("\n=== Todos los productos ===");
for (Producto p : productos) {
    System.out.println("  " + p);
}
```

> **¿Qué es JPQL?** JPQL = Java Persistence Query Language. Es similar a SQL pero usa nombres de **clases** Java (`Producto`) en vez de nombres de **tablas** SQL (`productos`). Hibernate traduce JPQL a SQL automáticamente. Ejemplo: `"SELECT p FROM Producto p"` en JPQL se convierte en `"SELECT * FROM productos"` en SQL.

### Consulta con filtro (parámetros con nombre)

```java
List<Producto> caros = em.createQuery(
        "SELECT p FROM Producto p WHERE p.precio > :precioMin", Producto.class)
        .setParameter("precioMin", 100.0)
        .getResultList();
```

> **`:precioMin`** es un parámetro con nombre. Es más seguro que concatenar strings porque previene SQL injection. **Siempre** usen parámetros, **nunca** concatenen valores en la query.

### SQL vs JPQL: tabla comparativa

| SQL (tablas) | JPQL (clases Java) |
|---|---|
| `SELECT * FROM productos` | `SELECT p FROM Producto p` |
| `SELECT * FROM productos WHERE id = 1` | `em.find(Producto.class, 1L)` |
| `SELECT * FROM productos WHERE precio > 100` | `SELECT p FROM Producto p WHERE p.precio > :min` |
| `SELECT * FROM productos ORDER BY precio DESC` | `SELECT p FROM Producto p ORDER BY p.precio DESC` |
| `SELECT COUNT(*) FROM productos` | `SELECT COUNT(p) FROM Producto p` |
| `SELECT AVG(precio) FROM productos` | `SELECT AVG(p.precio) FROM Producto p` |
| `SELECT * FROM productos WHERE nombre LIKE '%Laptop%'` | `SELECT p FROM Producto p WHERE p.nombre LIKE :nombre` |

> **Regla clave:** En JPQL se usan nombres de **clases** y **campos** Java, no nombres de tablas ni columnas SQL. `Producto` (clase) en vez de `productos` (tabla). `p.precio` (campo Java) en vez de `precio` (columna SQL).

### Más ejemplos de JPQL para practicar en vivo

```java
// Contar cuantos productos hay
Long total = em.createQuery("SELECT COUNT(p) FROM Producto p", Long.class)
        .getSingleResult();
System.out.println("Total productos: " + total);

// Precio promedio
Double promedio = em.createQuery("SELECT AVG(p.precio) FROM Producto p", Double.class)
        .getSingleResult();
System.out.printf("Precio promedio: %.2f€%n", promedio);

// Buscar por nombre parcial (LIKE)
List<Producto> laptops = em.createQuery(
        "SELECT p FROM Producto p WHERE p.nombre LIKE :patron", Producto.class)
        .setParameter("patron", "%Laptop%")
        .getResultList();

// Ordenar por precio descendente
List<Producto> ordenados = em.createQuery(
        "SELECT p FROM Producto p ORDER BY p.precio DESC", Producto.class)
        .getResultList();

// Productos con stock bajo (< 20 unidades)
List<Producto> stockBajo = em.createQuery(
        "SELECT p FROM Producto p WHERE p.stock < :minStock ORDER BY p.stock", Producto.class)
        .setParameter("minStock", 20)
        .getResultList();
```

> **EXPERIMENTA:**
> 1. Cambien el ORDER BY de `DESC` a `ASC`. ¿Qué cambia?
> 2. Prueben `getSingleResult()` con una query que devuelve 0 resultados. ¿Qué error sale? → `NoResultException`. Por eso para búsquedas que pueden no encontrar nada, usen `getResultList()`.
> 3. Escriban una query JPQL que busque productos entre dos precios usando `BETWEEN :min AND :max`.

### El Main completo: todo junto

*📁 Archivo a modificar: `src/main/java/com/cursojava/Main.java`*

```java
package com.cursojava;

import com.cursojava.modelo.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("curso-hibernate-pu");
        EntityManager em = emf.createEntityManager();

        // === CREATE ===
        System.out.println("=== CREAR PRODUCTOS ===");
        em.getTransaction().begin();

        Producto laptop = new Producto("Laptop Lenovo", 899.99, 15);
        Producto mouse = new Producto("Mouse Logitech", 29.99, 100);
        Producto teclado = new Producto("Teclado Mecanico", 79.99, 50);

        em.persist(laptop);
        em.persist(mouse);
        em.persist(teclado);

        em.getTransaction().commit();
        System.out.println("Creados 3 productos. IDs: " + laptop.getId() + ", " + mouse.getId() + ", " + teclado.getId());

        // === READ ===
        System.out.println("\n=== BUSCAR POR ID ===");
        Producto encontrado = em.find(Producto.class, 1L);
        System.out.println("ID 1: " + encontrado);

        // === UPDATE ===
        System.out.println("\n=== ACTUALIZAR PRECIO ===");
        em.getTransaction().begin();
        encontrado.setPrecio(799.99);
        em.getTransaction().commit();
        System.out.println("Nuevo precio: " + encontrado.getPrecio());

        // === DELETE ===
        System.out.println("\n=== ELIMINAR MOUSE ===");
        em.getTransaction().begin();
        Producto aEliminar = em.find(Producto.class, 2L);
        em.remove(aEliminar);
        em.getTransaction().commit();

        // === LISTAR TODOS ===
        System.out.println("\n=== LISTAR TODOS ===");
        List<Producto> todos = em.createQuery("SELECT p FROM Producto p", Producto.class)
                .getResultList();
        for (Producto p : todos) {
            System.out.println("  " + p);
        }
        System.out.println("Total: " + todos.size() + " productos");

        // === CONSULTA CON FILTRO ===
        System.out.println("\n=== PRODUCTOS CON PRECIO > 100 ===");
        List<Producto> caros = em.createQuery(
                "SELECT p FROM Producto p WHERE p.precio > :precioMin", Producto.class)
                .setParameter("precioMin", 100.0)
                .getResultList();
        for (Producto p : caros) {
            System.out.println("  " + p);
        }

        em.close();
        emf.close();
        System.out.println("\n=== FIN ===");
    }
}
```

### Salida esperada

```
=== CREAR PRODUCTOS ===
Hibernate: insert into productos (nombre, precio, stock) values (?, ?, ?)
Hibernate: insert into productos (nombre, precio, stock) values (?, ?, ?)
Hibernate: insert into productos (nombre, precio, stock) values (?, ?, ?)
Creados 3 productos. IDs: 1, 2, 3

=== BUSCAR POR ID ===
Hibernate: select ... from productos ... where id=?
ID 1: Producto{id=1, nombre='Laptop Lenovo', precio=899.99, stock=15}

=== ACTUALIZAR PRECIO ===
Hibernate: update productos set nombre=?, precio=?, stock=? where id=?
Nuevo precio: 799.99

=== ELIMINAR MOUSE ===
Hibernate: delete from productos where id=?

=== LISTAR TODOS ===
Hibernate: select ... from productos ...
  Producto{id=1, nombre='Laptop Lenovo', precio=799.99, stock=15}
  Producto{id=3, nombre='Teclado Mecanico', precio=79.99, stock=50}
Total: 2 productos

=== PRODUCTOS CON PRECIO > 100 ===
  Producto{id=1, nombre='Laptop Lenovo', precio=799.99, stock=15}

=== FIN ===
```

> **¿Por qué hay 2 productos al listar y no 3?** Porque eliminaron el mouse. Crearon 3, eliminaron 1, quedan 2.

---

# ═══════════════════════════════════════════════
# PARTE IV — EJERCICIO INTEGRADOR
# ═══════════════════════════════════════════════

# EJERCICIO INTEGRADOR — Entidad Libro

## GPS Arquitectónico

```
╔═══════════════════════════════════════════════════════════════╗
║              EJERCICIO: ENTIDAD LIBRO                         ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  ARCHIVOS A CREAR:                                            ║
║                                                               ║
║  src/main/java/com/cursojava/                                 ║
║  ├── modelo/                                                  ║
║  │   └── Libro.java           ← Entidad JPA (nueva)          ║
║  └── LibroMain.java           ← Main con CRUD de libros      ║
║                                                               ║
║  ARCHIVOS QUE YA EXISTEN (no modificar):                      ║
║                                                               ║
║  src/main/resources/META-INF/                                 ║
║  └── persistence.xml          ← Ya configurado                ║
║                                                               ║
║  pom.xml                      ← Ya tiene Hibernate + H2      ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```

## Enunciado

Crear una nueva entidad `Libro` en el mismo proyecto.

### La clase Libro

Ubicación: `src/main/java/com/cursojava/modelo/Libro.java`

Atributos:
- `Long id` (auto-generado)
- `String titulo` (no puede ser null, máximo 200 caracteres)
- `String autor` (no puede ser null)
- `double precio`
- `int anioPublicacion`

### Lo que deben implementar

1. La clase `Libro.java` con todas las anotaciones JPA
2. En una clase nueva `LibroMain.java`:
   - Crear 5 libros
   - Listar todos
   - Buscar por autor: `"SELECT l FROM Libro l WHERE l.autor = :autor"`
   - Buscar por rango de precio: `"SELECT l FROM Libro l WHERE l.precio BETWEEN :min AND :max"`
   - Actualizar el precio de un libro
   - Eliminar un libro

### Pistas

1. `Libro.java` sigue **exactamente** el mismo patrón que `Producto.java` — copien la estructura y cambien los campos
2. **No olviden el constructor vacío** — es el error más común
3. Para `anioPublicacion` (camelCase en Java), pueden mapear a snake_case en SQL: `@Column(name = "anio_publicacion")`
4. Las consultas JPQL usan el nombre de la **clase** (`Libro`), no el de la **tabla** (`libros`)
5. Recuerden: transacción para escrituras (`begin` + `commit`), sin transacción para lecturas
6. El `LibroMain.java` sigue el mismo patrón que `Main.java` — los 3 pasos: crear fábrica, crear EntityManager, cerrar al final

---

# REFERENCIAS

# 4.1 Mapa de anotaciones JPA aprendidas hoy

| Anotación | Va en | Significa |
|-----------|-------|-----------|
| `@Entity` | La clase | "Soy una tabla" |
| `@Table(name = "...")` | La clase | "Mi tabla se llama..." |
| `@Id` | Un campo | "Soy la primary key" |
| `@GeneratedValue` | Un campo | "La BD genera mi valor" |
| `@Column(...)` | Un campo | "Configuración de esta columna" |

# 4.2 Mapa de métodos del EntityManager

| Método | Operación SQL | Necesita transacción |
|--------|--------------|---------------------|
| `persist(objeto)` | INSERT | Sí |
| `find(Clase, id)` | SELECT WHERE id=? | No |
| `merge(objeto)` | UPDATE | Sí |
| `remove(objeto)` | DELETE | Sí |
| `createQuery(jpql)` | SELECT ... | No |

# 4.3 Para profundizar

**Documentación oficial:**
- [Jakarta Persistence (JPA) Specification](https://jakarta.ee/specifications/persistence/3.1/)
- [Hibernate ORM User Guide](https://docs.jboss.org/hibernate/orm/6.4/userguide/html_single/Hibernate_User_Guide.html)
- [H2 Database Documentation](https://www.h2database.com/html/main.html)

**Tutoriales recomendados:**
- [Baeldung: Learn JPA & Hibernate](https://www.baeldung.com/learn-jpa-hibernate) — Colección completa de tutoriales JPA
- [Baeldung: Introduction to Hibernate](https://www.baeldung.com/hibernate-5-spring) — Guía paso a paso

**Recurso gratuito:**
- [Java Persistence with Hibernate (Manning)](https://www.manning.com/books/java-persistence-with-hibernate) — Libro de referencia del framework

# 4.4 Errores comunes y cómo solucionarlos

Estos son los errores que van a encontrar hoy. No se asusten — todos tienen solución rápida.

| # | Mensaje de error | Causa | Solución |
|---|-----------------|-------|----------|
| 1 | `No Persistence provider for EntityManager named "curso-hibernate-pu"` | Hibernate no encuentra `persistence.xml` | Verificar que está en `src/main/resources/META-INF/persistence.xml` (exactamente esa ruta, con mayúsculas en META-INF) |
| 2 | `No default constructor for entity: Producto` | Falta el constructor vacío `public Producto() {}` | Añadir un constructor sin parámetros a la entidad |
| 3 | `Unknown entity: com.cursojava.modelo.Producto` | Falta `@Entity` en la clase | Añadir `@Entity` encima de la clase |
| 4 | `TransactionRequiredException` | Intentan escribir sin transacción | Envolver en `begin()` ... `commit()` |
| 5 | `Cannot resolve symbol 'jakarta.persistence'` | Hibernate no se descargó | Recargar Maven en IntelliJ (icono del elefante o Ctrl+Shift+O) |
| 6 | `javax.persistence` no compila | Import de la versión vieja | Cambiar `javax.persistence` por `jakarta.persistence` |
| 7 | `detached entity passed to persist` | El objeto ya tiene un ID asignado | Usar `em.merge(objeto)` en vez de `em.persist(objeto)` |
| 8 | `NoResultException` | `getSingleResult()` no encontró nada | Usar `getResultList()` y verificar si está vacía |
| 9 | `ClassNotFoundException: org.h2.Driver` | H2 no se descargó | Recargar Maven y verificar la dependencia en el pom.xml |
| 10 | La primera compilación tarda mucho | Maven descarga ~40 JARs de Hibernate | Esperar 2-3 minutos. Solo pasa la primera vez |

### El error más común del día

```
org.hibernate.InstantiationException: No default constructor for entity: com.cursojava.modelo.Producto
```

**Solución:** Añadir siempre un constructor vacío a TODAS las entidades:

```java
// OBLIGATORIO para JPA — sin esto, Hibernate no puede crear instancias
public Producto() {
}
```

**¿Por qué?** Hibernate necesita crear objetos "vacíos" para llenarlos con datos de la BD. Si no hay constructor sin parámetros, no puede hacerlo.

---

# ═══════════════════════════════════════════════
# PARTE V — CIERRE DEL DÍA
# ═══════════════════════════════════════════════

## Resumen: lo que lograron hoy

| # | Concepto | Qué aprendieron |
|---|----------|-----------------|
| 1 | **Impedance mismatch** | Objetos Java ≠ tablas SQL — por eso necesitamos un traductor |
| 2 | **ORM** | Hibernate traduce automáticamente entre Java y SQL |
| 3 | **JPA vs Hibernate** | JPA es la especificación (interfaz), Hibernate la implementación |
| 4 | **H2** | Base de datos en memoria, perfecta para aprender |
| 5 | **persistence.xml** | Configura la conexión a la BD |
| 6 | **@Entity + @Id + @Column** | Mapean una clase Java a una tabla SQL |
| 7 | **EntityManager** | La puerta de entrada a la BD (persist, find, remove) |
| 8 | **CRUD + dirty checking** | Create, Read, Update (automático), Delete |
| 9 | **JPQL** | SQL pero con nombres de clases Java |

## Commit del día

Si trabajaron en el proyecto `curso-hibernate` y quieren hacer commit:

```bash
cd curso-hibernate
git init
git add .
git commit -m "Proyecto curso-hibernate: entidad Producto + CRUD completo con H2 (dia 9)"
```

Si completaron el ejercicio Libro:

```bash
git add .
git commit -m "Agregar entidad Libro con CRUD y consultas JPQL"
```
