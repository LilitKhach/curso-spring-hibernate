# Día 12: La Pizzería se Convierte en API REST con Spring Boot

Ayer aprendieron Spring Boot con un proyecto genérico (Producto). Hoy, Spring Boot entra en la Pizzería. Al final del día, la Pizzería será una API REST completa que responde a peticiones HTTP con datos en JSON.

Prof. Juan Marcelo Gutierrez Miranda

**Curso IFCD0014 — Semana 3, Día 12 (Jueves)**
**Objetivo:** Migrar la Pizzería a Spring Boot. Crear repositorios, servicios y controladores REST. Probar con Postman.

> Este manual es de consulta. Sigan los pasos con el proyecto abierto en IntelliJ.

---

# 1. Objetivo del Día

Ayer hicieron esto con `Producto`:

```
Producto → ProductoRepository → ProductoService → ProductoController → GET /api/productos
```

Hoy hacen exactamente lo mismo, pero con **la Pizzería que llevan construyendo desde el día 3**: Pizza, Cliente, Pedido. Tres entidades reales, con relaciones `@ManyToOne` y `@ManyToMany`, CRUD completo y Postman.

---

# 2. Crear el Proyecto Spring Boot para la Pizzería

## Paso 1: Spring Initializr

Abran [https://start.spring.io](https://start.spring.io) y configuren:

```
Project: Maven  |  Language: Java  |  Spring Boot: 3.4.x  |  Java: 17
Group: com.pizzeria  |  Artifact: pizzeria-spring  |  Package: com.pizzeria
```

## Paso 2: Dependencias

Agreguen estas 5 dependencias (botón ADD DEPENDENCIES):

| Dependencia | Para qué |
|-------------|----------|
| Spring Web | Controladores REST, responder HTTP |
| Spring Data JPA | Repositorios automáticos, Hibernate incluido |
| H2 Database | Base de datos en memoria (sin instalar nada) |
| Lombok | @Data, @NoArgsConstructor (menos código) |
| Spring Boot DevTools | Restart automático al guardar |

## Paso 3: Descargar y abrir

1. Clic en **GENERATE** → descarga un `.zip`
2. Descomprimir en su carpeta de trabajo
3. IntelliJ → File → Open → seleccionar la carpeta `pizzeria-spring`
4. Esperar a que Maven descargue todas las dependencias

## Paso 4: Primera ejecución

Ejecuten `PizzeriaSpringApplication.java` (clic derecho → Run). Si ven `Started PizzeriaSpringApplication`, el proyecto está listo. Paren la aplicación (botón rojo).

---

# 3. Copiar y Adaptar las Entidades

Creen estos paquetes: `com.pizzeria.modelo`, `com.pizzeria.repositorio`, `com.pizzeria.servicio`, `com.pizzeria.controlador`. En IntelliJ: clic derecho en `com.pizzeria` → New → Package.

## Paso 1: Los Enums (sin anotaciones JPA)

**📁** `src/main/java/com/pizzeria/modelo/Categoria.java`

```java
package com.pizzeria.modelo;

public enum Categoria {
    CLASICA,
    PREMIUM,
    VEGANA,
    INFANTIL
}
```

**📁** `src/main/java/com/pizzeria/modelo/CategoriaCliente.java`

```java
package com.pizzeria.modelo;

public enum CategoriaCliente {
    BRONCE,
    PLATA,
    ORO,
    PLATINO
}
```

**📁** `src/main/java/com/pizzeria/modelo/TipoCliente.java`

```java
package com.pizzeria.modelo;

public enum TipoCliente {
    PARTICULAR,
    EMPRESA,
    FRANQUICIA
}
```

## Paso 2: Pizza.java

**📁** `src/main/java/com/pizzeria/modelo/Pizza.java`

```java
package com.pizzeria.modelo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pizzas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pizza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private double precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;
}
```

Es igual que el día 10, pero ahora Lombok genera getters, setters, toString, equals y hashCode con `@Data`.

## Paso 3: Cliente.java

**📁** `src/main/java/com/pizzeria/modelo/Cliente.java`

```java
package com.pizzeria.modelo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    private CategoriaCliente categoriaCliente;

    @Enumerated(EnumType.STRING)
    private TipoCliente tipoCliente;
}
```

## Paso 4: Pedido.java

**📁** `src/main/java/com/pizzeria/modelo/Pedido.java`

```java
package com.pizzeria.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
```

Las relaciones son las mismas del día 10. `@JoinTable` define la tabla intermedia `pedido_pizzas`.

---

# 4. Repositorios con Spring Data

## PizzaRepository

**📁** `src/main/java/com/pizzeria/repositorio/PizzaRepository.java`

```java
package com.pizzeria.repositorio;

import com.pizzeria.modelo.Categoria;
import com.pizzeria.modelo.Pizza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PizzaRepository extends JpaRepository<Pizza, Long> {

    // Spring genera el SQL: SELECT * FROM pizzas WHERE categoria = ?
    List<Pizza> findByCategoria(Categoria categoria);

    // SELECT * FROM pizzas WHERE precio < ?
    List<Pizza> findByPrecioLessThan(double precio);

    // SELECT * FROM pizzas WHERE LOWER(nombre) LIKE LOWER('%?%')
    List<Pizza> findByNombreContainingIgnoreCase(String nombre);
}
```

**Cero implementación.** Spring lee el nombre del método y genera la query SQL:

```
findBy + Atributo + Condicion → SQL automático

findByCategoria(Categoria c)            → WHERE categoria = ?
findByPrecioLessThan(double precio)     → WHERE precio < ?
findByNombreContainingIgnoreCase(String) → WHERE LOWER(nombre) LIKE LOWER('%?%')
```

## ClienteRepository

**📁** `src/main/java/com/pizzeria/repositorio/ClienteRepository.java`

```java
package com.pizzeria.repositorio;

import com.pizzeria.modelo.Cliente;
import com.pizzeria.modelo.CategoriaCliente;
import com.pizzeria.modelo.TipoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByTipoCliente(TipoCliente tipoCliente);

    List<Cliente> findByCategoriaCliente(CategoriaCliente categoriaCliente);

    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
}
```

## PedidoRepository

**📁** `src/main/java/com/pizzeria/repositorio/PedidoRepository.java`

```java
package com.pizzeria.repositorio;

import com.pizzeria.modelo.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByTotalGreaterThan(double total);
}
```

---

# 5. La Capa de Servicio

El controlador NO debe acceder al repositorio directamente. Siempre pasa por el servicio.

## PizzaService

**📁** `src/main/java/com/pizzeria/servicio/PizzaService.java`

```java
package com.pizzeria.servicio;

import com.pizzeria.modelo.Categoria;
import com.pizzeria.modelo.Pizza;
import com.pizzeria.repositorio.PizzaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PizzaService {

    private final PizzaRepository pizzaRepository;

    // Spring inyecta el repositorio automáticamente (constructor injection)
    public PizzaService(PizzaRepository pizzaRepository) {
        this.pizzaRepository = pizzaRepository;
    }

    public List<Pizza> listarTodas() {
        return pizzaRepository.findAll();
    }

    public Optional<Pizza> buscarPorId(Long id) {
        return pizzaRepository.findById(id);
    }

    @Transactional
    public Pizza crear(Pizza pizza) {
        return pizzaRepository.save(pizza);
    }

    @Transactional
    public Pizza actualizar(Long id, Pizza datosNuevos) {
        Pizza existente = pizzaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                    "Pizza no encontrada con id: " + id));

        existente.setNombre(datosNuevos.getNombre());
        existente.setPrecio(datosNuevos.getPrecio());
        existente.setCategoria(datosNuevos.getCategoria());

        return pizzaRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!pizzaRepository.existsById(id)) {
            throw new RuntimeException("Pizza no encontrada con id: " + id);
        }
        pizzaRepository.deleteById(id);
    }

    public List<Pizza> buscarPorCategoria(Categoria categoria) {
        return pizzaRepository.findByCategoria(categoria);
    }

    public List<Pizza> buscarBaratas(double precioMax) {
        return pizzaRepository.findByPrecioLessThan(precioMax);
    }
}
```

`@Service` registra la clase como bean. `@Transactional` asegura que las escrituras usen transacción. No necesitan `@Autowired` si solo hay un constructor (Spring lo inyecta automáticamente).

---

# 6. El Controlador REST

## PizzaController

**📁** `src/main/java/com/pizzeria/controlador/PizzaController.java`

```java
package com.pizzeria.controlador;

import com.pizzeria.modelo.Categoria;
import com.pizzeria.modelo.Pizza;
import com.pizzeria.servicio.PizzaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pizzas")
public class PizzaController {

    private final PizzaService pizzaService;

    public PizzaController(PizzaService pizzaService) {
        this.pizzaService = pizzaService;
    }

    // GET http://localhost:8080/api/pizzas
    @GetMapping
    public List<Pizza> listarTodas() {
        return pizzaService.listarTodas();
    }

    // GET http://localhost:8080/api/pizzas/1
    @GetMapping("/{id}")
    public ResponseEntity<Pizza> buscarPorId(@PathVariable Long id) {
        return pizzaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST http://localhost:8080/api/pizzas
    @PostMapping
    public ResponseEntity<Pizza> crear(@RequestBody Pizza pizza) {
        Pizza nueva = pizzaService.crear(pizza);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // PUT http://localhost:8080/api/pizzas/1
    @PutMapping("/{id}")
    public ResponseEntity<Pizza> actualizar(
            @PathVariable Long id, @RequestBody Pizza pizza) {
        try {
            Pizza actualizada = pizzaService.actualizar(id, pizza);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE http://localhost:8080/api/pizzas/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            pizzaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET http://localhost:8080/api/pizzas/categoria/PREMIUM
    @GetMapping("/categoria/{categoria}")
    public List<Pizza> buscarPorCategoria(
            @PathVariable Categoria categoria) {
        return pizzaService.buscarPorCategoria(categoria);
    }

    // GET http://localhost:8080/api/pizzas/baratas?precioMax=10.0
    @GetMapping("/baratas")
    public List<Pizza> buscarBaratas(@RequestParam double precioMax) {
        return pizzaService.buscarBaratas(precioMax);
    }
}
```

## Anotaciones clave

| Anotación | Qué hace |
|-----------|----------|
| `@RestController` | Combina @Controller + @ResponseBody. Todo se convierte a JSON |
| `@RequestMapping("/api/pizzas")` | Prefijo de ruta para todos los endpoints de la clase |
| `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` | Mapean métodos HTTP a métodos Java |
| `@PathVariable` | Extrae un valor de la URL: `/api/pizzas/{id}` |
| `@RequestBody` | Convierte el JSON del cuerpo HTTP a un objeto Java |
| `@RequestParam` | Extrae un parámetro de la URL: `?precioMax=10.0` |
| `ResponseEntity<T>` | Controla el código HTTP: `.ok()` → 200, `.notFound()` → 404, `.status(201)` → 201 |

---

# 7. application.properties

**📁** `src/main/resources/application.properties`

```properties
# ===== BASE DE DATOS H2 =====
spring.datasource.url=jdbc:h2:mem:pizzeriadb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# ===== JPA / HIBERNATE =====
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# ===== CONSOLA H2 (para ver las tablas) =====
spring.h2-console.enabled=true
spring.h2-console.path=/h2-console

# ===== FORMATO JSON =====
spring.jackson.serialization.indent_output=true

# ===== DATOS INICIALES =====
spring.jpa.defer-datasource-initialization=true
```

Hibernate crea las tablas automáticamente a partir de las anotaciones `@Entity`. La última línea (`defer-datasource-initialization`) hace que `data.sql` se ejecute **después** de crear las tablas.

## Datos iniciales con data.sql

**📁** `src/main/resources/data.sql`

```sql
-- Pizzas
INSERT INTO pizzas (nombre, precio, categoria) VALUES ('Margarita', 8.50, 'CLASICA');
INSERT INTO pizzas (nombre, precio, categoria) VALUES ('Cuatro Quesos', 11.00, 'PREMIUM');
INSERT INTO pizzas (nombre, precio, categoria) VALUES ('Pepperoni', 9.50, 'CLASICA');
INSERT INTO pizzas (nombre, precio, categoria) VALUES ('Vegetal', 10.00, 'VEGANA');
INSERT INTO pizzas (nombre, precio, categoria) VALUES ('Hawaiana', 9.00, 'CLASICA');
INSERT INTO pizzas (nombre, precio, categoria) VALUES ('Trufa y Parmesano', 15.00, 'PREMIUM');
INSERT INTO pizzas (nombre, precio, categoria) VALUES ('Mini Jamon', 6.50, 'INFANTIL');

-- Clientes
INSERT INTO clientes (nombre, categoria_cliente, tipo_cliente)
    VALUES ('Carlos Garcia', 'ORO', 'PARTICULAR');
INSERT INTO clientes (nombre, categoria_cliente, tipo_cliente)
    VALUES ('Ana Lopez', 'PLATA', 'PARTICULAR');
INSERT INTO clientes (nombre, categoria_cliente, tipo_cliente)
    VALUES ('Restaurante El Buen Sabor', 'PLATINO', 'EMPRESA');
INSERT INTO clientes (nombre, categoria_cliente, tipo_cliente)
    VALUES ('Maria Torres', 'BRONCE', 'PARTICULAR');

-- Pedidos
INSERT INTO pedidos (cliente_id, fecha, total)
    VALUES (1, CURRENT_TIMESTAMP, 28.00);
INSERT INTO pedidos (cliente_id, fecha, total)
    VALUES (2, CURRENT_TIMESTAMP, 11.00);
INSERT INTO pedidos (cliente_id, fecha, total)
    VALUES (1, CURRENT_TIMESTAMP, 15.00);

-- Relacion pedido-pizzas
INSERT INTO pedido_pizzas (pedido_id, pizza_id) VALUES (1, 1);
INSERT INTO pedido_pizzas (pedido_id, pizza_id) VALUES (1, 2);
INSERT INTO pedido_pizzas (pedido_id, pizza_id) VALUES (1, 5);
INSERT INTO pedido_pizzas (pedido_id, pizza_id) VALUES (2, 2);
INSERT INTO pedido_pizzas (pedido_id, pizza_id) VALUES (3, 6);
```

---

# 8. Probar con el Navegador y Postman

## Paso 1: Arrancar la aplicación

Desde IntelliJ, ejecuten `PizzeriaSpringApplication`. También pueden usar la terminal:

```bash
mvn spring-boot:run
```

Esperen a ver `Started PizzeriaSpringApplication`.

## Paso 2: Probar GET con el navegador

Abran el navegador y vayan a `http://localhost:8080/api/pizzas`. Deben ver un JSON con las 7 pizzas del `data.sql`. Prueben también:

```
http://localhost:8080/api/pizzas/1
http://localhost:8080/api/pizzas/categoria/PREMIUM
http://localhost:8080/api/pizzas/baratas?precioMax=10
```

## Paso 3: Probar POST con Postman

En Postman: **POST** `http://localhost:8080/api/pizzas`, Body → raw → JSON:

```json
{
    "nombre": "Barbacoa Especial",
    "precio": 12.50,
    "categoria": "PREMIUM"
}
```

Respuesta: `201 Created` con la pizza creada (incluyendo el `id` generado).

## Paso 4: Probar PUT y DELETE

**PUT** `http://localhost:8080/api/pizzas/8` con JSON actualizado → `200 OK`.

**DELETE** `http://localhost:8080/api/pizzas/8` → `204 No Content`. Verifiquen con GET que ya no aparece.

## Paso 6: Consola H2

Para ver las tablas directamente, abran `http://localhost:8080/h2-console`. Datos de conexión: JDBC URL `jdbc:h2:mem:pizzeriadb`, User `sa`, Password vacío. Ahí pueden ejecutar `SELECT * FROM pizzas;` y ver los datos.

---

# 9. Ejercicio: Crear ClienteController

Ya tienen `ClienteRepository`. Ahora creen el servicio y el controlador siguiendo el mismo patrón que Pizza.

## ClienteService

**📁** `src/main/java/com/pizzeria/servicio/ClienteService.java`

Creen la clase con estos métodos:

```java
@Service
public class ClienteService {

    // Inyectar ClienteRepository por constructor

    // listarTodos()        → findAll()
    // buscarPorId(Long id) → findById()
    // crear(Cliente c)     → save()
    // actualizar(Long id, Cliente datosNuevos) → buscar + setear + save()
    // eliminar(Long id)    → deleteById()
    // buscarPorTipo(TipoCliente tipo)  → findByTipoCliente()
}
```

## ClienteController

**📁** `src/main/java/com/pizzeria/controlador/ClienteController.java`

Endpoints a implementar:

| Método | URL | Acción |
|--------|-----|--------|
| GET | `/api/clientes` | Listar todos |
| GET | `/api/clientes/{id}` | Buscar por id |
| POST | `/api/clientes` | Crear nuevo |
| PUT | `/api/clientes/{id}` | Actualizar |
| DELETE | `/api/clientes/{id}` | Eliminar |
| GET | `/api/clientes/tipo/{tipo}` | Filtrar por TipoCliente |

Ejemplo de JSON para POST/PUT:

```json
{
    "nombre": "Pedro Ramirez",
    "categoriaCliente": "PLATA",
    "tipoCliente": "PARTICULAR"
}
```

Prueben todos los endpoints con Postman.

---

# 10. GPS Arquitectónico

Este es el flujo completo de una petición HTTP en la Pizzería Spring:

```
╔══════════════════════════════════════════════════════════════╗
║              FLUJO DE UNA PETICIÓN HTTP                       ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Postman/Navegador                                           ║
║       │                                                      ║
║       ▼                                                      ║
║  PizzaController         @RestController                     ║
║  (recibe HTTP,           (convierte JSON ↔ Java)             ║
║   delega al servicio)                                        ║
║       │                                                      ║
║       ▼                                                      ║
║  PizzaService            @Service                            ║
║  (lógica de negocio,     (validaciones, cálculos)            ║
║   usa el repositorio)                                        ║
║       │                                                      ║
║       ▼                                                      ║
║  PizzaRepository         JpaRepository                       ║
║  (interfaz — Spring      (Spring genera la implementación)   ║
║   genera el SQL)                                             ║
║       │                                                      ║
║       ▼                                                      ║
║  H2 Database             (automático, en memoria)            ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

## Comparación: día 10 vs día 12

| Aspecto | Día 10 (Hibernate puro) | Día 12 (Spring Boot) |
|---------|------------------------|---------------------|
| Acceso a datos | EntityManager manual | JpaRepository automático |
| Configuración | persistence.xml (40+ líneas) | application.properties (10 líneas) |
| Creación de objetos | main() crea todo a mano | Spring inyecta automáticamente |
| Interfaz | Solo consola | REST API con JSON |
| Líneas para CRUD de Pizza | ~80 líneas | ~15 líneas |

**80 líneas de boilerplate → 3 anotaciones. Eso es Spring.**

Los archivos que faltan (`ClienteService`, `ClienteController`) son el ejercicio de ustedes.

---

# 11. Resumen y Próximos Pasos

```
╔══════════════════════════════════════════════════════════════╗
║              RESUMEN DEL DÍA 12                               ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  LO QUE HICIERON HOY:                                       ║
║  ✓ Proyecto Spring Boot desde cero para la Pizzería          ║
║  ✓ 3 entidades con relaciones (@ManyToOne, @ManyToMany)      ║
║  ✓ 3 repositorios con queries automáticas                    ║
║  ✓ Servicio con lógica de negocio                            ║
║  ✓ Controlador REST con CRUD completo                        ║
║  ✓ Datos iniciales con data.sql                              ║
║  ✓ Pruebas con Postman y navegador                           ║
║                                                              ║
║  PATRÓN QUE APRENDIERON:                                     ║
║  Entity → Repository → Service → Controller                  ║
║  (Este patrón lo van a usar en TODOS sus proyectos Spring)   ║
║                                                              ║
║  PRÓXIMOS DÍAS:                                              ║
║  → Día 13: Proyecto personal — diseñen su propia aplicación  ║
║  → Día 14: Completar API + validación + GitHub               ║
║  → Día 15+: Seguridad, testing, despliegue                   ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```
