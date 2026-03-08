# Día 13: Proyecto Personal — Diseñen su Propia Aplicación

Llevan 12 días construyendo la Pizzería juntos. Hoy es SU turno. Van a crear una aplicación Spring Boot desde cero, con su propio dominio, sus propias entidades y sus propios endpoints REST.

Prof. Juan Marcelo Gutierrez Miranda

**Curso IFCD0014 — Semana 3, Día 13 (Viernes)**
**Objetivo:** Diseñar y comenzar a implementar una aplicación Spring Boot propia. Al final del día: entidades con relaciones, al menos un CRUD completo.

> Este proyecto será su pieza de portafolio. Lo van a subir a GitHub y lo pueden mostrar en entrevistas.

---

# 1. El Momento ha Llegado

Hasta ahora siguieron instrucciones paso a paso. Eso está bien para aprender. Pero en el mundo laboral nadie les va a dar un manual. Hoy practican lo más importante: **tomar decisiones técnicas propias**.

Ya saben: crear proyecto (Initializr), entidades (@Entity), relaciones (@ManyToOne, @ManyToMany), repositorios (JpaRepository), servicios (@Service), controladores (@RestController), configuración (H2 + data.sql) y probar con Postman. **Hoy aplican todo eso a un dominio que ustedes eligen.**

---

# 2. Elegir el Dominio (15 minutos)

Elijan un dominio que les interese. Van a trabajar en esto durante varios días, así que conviene que les motive.

## Ideas

| Dominio | Entidades ejemplo | Relaciones |
|---------|-------------------|------------|
| Gimnasio | Socio, Clase, Inscripcion, Entrenador | Socio ↔ Clase (ManyToMany), Clase → Entrenador (ManyToOne) |
| Veterinaria | Mascota, Dueno, Consulta, Veterinario | Mascota → Dueno (ManyToOne), Consulta → Veterinario (ManyToOne) |
| Tienda de ropa | Producto, Categoria, Venta, Cliente | Producto → Categoria (ManyToOne), Venta ↔ Producto (ManyToMany) |
| Biblioteca | Libro, Autor, Socio, Prestamo | Libro ↔ Autor (ManyToMany), Prestamo → Socio (ManyToOne) |
| Restaurante | Plato, Mesa, Reserva, Camarero | Reserva → Mesa (ManyToOne), Reserva → Camarero (ManyToOne) |
| Cine | Pelicula, Sala, Sesion, Entrada | Sesion → Pelicula (ManyToOne), Sesion → Sala (ManyToOne) |
| Taller mecánico | Vehiculo, Cliente, Reparacion, Mecanico | Vehiculo → Cliente (ManyToOne), Reparacion ↔ Repuesto (ManyToMany) |
| Liga de fútbol | Jugador, Equipo, Partido, Estadistica | Jugador → Equipo (ManyToOne), Partido ↔ Equipo (ManyToMany) |
| Tienda de música | Album, Artista, Cancion, Genero | Album → Artista (ManyToOne), Album ↔ Genero (ManyToMany) |
| Hotel | Habitacion, Huesped, Reserva, Servicio | Reserva → Habitacion (ManyToOne), Reserva ↔ Servicio (ManyToMany) |

Pueden inventar otro dominio si ninguno de estos les convence.

## Reglas mínimas

```
╔══════════════════════════════════════════════════════════════╗
║              REQUISITOS MÍNIMOS                               ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  → Mínimo 3 entidades (mejor 4)                             ║
║  → Al menos 1 relación @ManyToOne                            ║
║  → Al menos 1 relación @ManyToMany                           ║
║  → Al menos 1 enum                                           ║
║  → Cada entidad con mínimo 3 atributos (además del id)       ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

# 3. Diseñar las Entidades (30 minutos)

Antes de tocar código, diseñen en papel o en la pizarra. Esta es la parte más importante. Si el diseño está bien, el código sale solo. Si el diseño está mal, van a perder horas arreglando problemas.

## Plantilla por entidad

Para cada entidad, llenen esto:

```
Entidad: ___________________
Tabla:   ___________________

Atributos:
  - id (Long, @Id @GeneratedValue)
  - _____________ (tipo: _______)
  - _____________ (tipo: _______)
  - _____________ (tipo: _______)
  - _____________ (tipo: _______)

Relaciones:
  - _____________ → _____________ (@ManyToOne / @ManyToMany)

Enum (si tiene):
  - _____________ con valores: _____, _____, _____
```

## Ejemplo completo: Veterinaria

Para que vean cómo queda la plantilla llena:

```
Entidad: Mascota          Tabla: mascotas
  - id, nombre, especie (enum: PERRO, GATO, AVE, REPTIL, OTRO)
  - fechaNacimiento (LocalDate), peso (double)
  - dueno → Dueno (@ManyToOne)

Entidad: Dueno             Tabla: duenos
  - id, nombre, telefono, email, direccion

Entidad: Veterinario       Tabla: veterinarios
  - id, nombre, especialidad (enum: GENERAL, CIRUGIA, DERMATOLOGIA, CARDIOLOGIA)
  - numeroColegiado

Entidad: Consulta          Tabla: consultas
  - id, fecha, motivo, diagnostico, coste
  - mascota → Mascota (@ManyToOne), veterinario → Veterinario (@ManyToOne)
  - medicamentos → List<Medicamento> (@ManyToMany)

Entidad: Medicamento       Tabla: medicamentos
  - id, nombre, laboratorio, precio
```

## Patrones comunes de relaciones

| Anotación | Significa | Ejemplo |
|-----------|-----------|---------|
| `@ManyToOne` | Muchos X pertenecen a un Y | Mascota → Dueno, Pedido → Cliente |
| `@ManyToMany` | Muchos X se relacionan con muchos Y | Consulta ↔ Medicamento, Pedido ↔ Pizza |
| `@OneToMany` | Lado inverso de @ManyToOne (opcional) | Dueno → List&lt;Mascota&gt; (con mappedBy) |

---

# 4. Crear el Proyecto (20 minutos)

## Paso 1: Spring Initializr

Vayan a [https://start.spring.io](https://start.spring.io):

```
Group:    com.miproyecto    (cámbienlo por su dominio: com.veterinaria, com.gimnasio...)
Artifact: nombre-del-proyecto
Package:  com.miproyecto
Java:     17
```

## Paso 2: Dependencias

Las mismas que la Pizzería:

```
✓ Spring Web
✓ Spring Data JPA
✓ H2 Database
✓ Lombok
✓ Spring Boot DevTools
```

## Paso 3: Estructura de paquetes

Creen estos paquetes dentro de su paquete raíz:

```
com.miproyecto/
├── modelo/          ← o entity/
├── repositorio/     ← o repository/
├── servicio/        ← o service/
└── controlador/     ← o controller/
```

Pueden usar los nombres en español (como la Pizzería) o en inglés. Lo importante es ser **consistente**: o todo en español, o todo en inglés. No mezclen.

---

# 5. Implementar las Entidades (1.5 horas)

Usen las entidades de la Pizzería como referencia directa. El patrón es siempre el mismo.

## Checklist por entidad

Para **cada** entidad que diseñaron, verifiquen:

```
[ ] @Entity en la clase
[ ] @Table(name = "nombre_tabla") — plural, snake_case
[ ] @Data @NoArgsConstructor @AllArgsConstructor (Lombok)
[ ] @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
[ ] Atributos con @Column(nullable = false) donde corresponda
[ ] @Enumerated(EnumType.STRING) para enums
[ ] @ManyToOne + @JoinColumn para relaciones muchos-a-uno
[ ] @ManyToMany + @JoinTable para relaciones muchos-a-muchos
[ ] Los enums creados como clases separadas (sin @Entity)
```

## Ejemplo: Mascota.java (referencia)

```java
package com.veterinaria.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "mascotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Especie especie;

    private LocalDate fechaNacimiento;

    private double peso;

    @ManyToOne
    @JoinColumn(name = "dueno_id")
    private Dueno dueno;
}
```

## Ejemplo: Consulta.java con dos @ManyToOne y un @ManyToMany

```java
package com.veterinaria.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consultas")
@Data @NoArgsConstructor @AllArgsConstructor
public class Consulta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fecha;
    @Column(nullable = false) private String motivo;
    private String diagnostico;
    private double coste;

    @ManyToOne @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    @ManyToOne @JoinColumn(name = "veterinario_id")
    private Veterinario veterinario;

    @ManyToMany
    @JoinTable(name = "consulta_medicamentos",
        joinColumns = @JoinColumn(name = "consulta_id"),
        inverseJoinColumns = @JoinColumn(name = "medicamento_id"))
    private List<Medicamento> medicamentos = new ArrayList<>();
}
```

---

# 6. Implementar Repositorios (30 minutos)

Un repositorio por entidad. Todos extienden `JpaRepository`.

## Checklist por repositorio

```
[ ] Interface (no clase)
[ ] Extiende JpaRepository<Entidad, Long>
[ ] @Repository
[ ] 2-3 métodos de búsqueda personalizados (findBy...)
```

## Ejemplo: MascotaRepository (referencia)

```java
package com.veterinaria.repositorio;

import com.veterinaria.modelo.Especie;
import com.veterinaria.modelo.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    List<Mascota> findByEspecie(Especie especie);

    List<Mascota> findByDuenoId(Long duenoId);

    List<Mascota> findByNombreContainingIgnoreCase(String nombre);
}
```

## Recordatorio: nombres de método → queries automáticas

```
findByNombre(String n)                     → WHERE nombre = ?
findByPrecioGreaterThan(double p)          → WHERE precio > ?
findByPrecioLessThan(double p)             → WHERE precio < ?
findByNombreContainingIgnoreCase(String n) → WHERE LOWER(nombre) LIKE LOWER('%?%')
findByFechaAfter(LocalDate f)              → WHERE fecha > ?
findByActivoTrue()                         → WHERE activo = true
findByTipoAndCategoria(Tipo t, Cat c)      → WHERE tipo = ? AND categoria = ?
```

---

# 7. Primer Servicio y Controlador (1 hora)

Empiecen por la entidad **principal** de su dominio (la que tiene más relaciones o la más importante).

## El patrón es siempre el mismo

Copien `PizzaService` y `PizzaController` del día 12, cambien los nombres y adapten. No reinventen la rueda. El servicio siempre tiene: `listarTodos()`, `buscarPorId()`, `crear()`, `actualizar()`, `eliminar()` + métodos personalizados. El controlador mapea cada uno a un endpoint HTTP.

---

# 8. Checklist de Fin de Día

Antes de irse, verifiquen:

```
[ ] 3+ entidades con anotaciones JPA correctas
[ ] Al menos 1 relación @ManyToOne funcionando
[ ] Al menos 1 relación @ManyToMany funcionando
[ ] Al menos 1 enum con @Enumerated(EnumType.STRING)
[ ] Al menos 1 Controller completo con CRUD (GET, POST, PUT, DELETE)
[ ] La aplicación arranca sin errores
[ ] Al menos 1 endpoint GET devuelve datos en el navegador
[ ] application.properties configurado (H2, show-sql)
```

Si llegaron hasta aquí, van muy bien. Lo que falte lo completan mañana.

---

# 9. Horario Sugerido del Día

| Hora | Actividad | Detalle |
|------|-----------|---------|
| 9:00 - 9:15 | Elegir dominio | Decidir rápido, no perfeccionismo |
| 9:15 - 9:45 | Diseñar entidades | En papel o pizarra, con relaciones |
| 9:45 - 10:00 | Crear proyecto | Spring Initializr + abrir en IntelliJ |
| 10:00 - 11:00 | Implementar entidades | @Entity, @ManyToOne, @ManyToMany, enums |
| 11:00 - 11:30 | **Descanso** | |
| 11:30 - 12:30 | Repositorios + primer servicio | JpaRepository + @Service |
| 12:30 - 13:30 | Primer controlador + probar | @RestController + Postman |
| 13:30 - 14:00 | Resolver dudas + commit | git init, git add, git commit |

**Consejo:** Mejor 3 entidades simples que FUNCIONAN que 6 entidades complejas que NO COMPILAN. Empiecen simple, prueben que arranca, luego agreguen. Así se trabaja en el mundo real: iterando.

---

# 10. Referencia Rápida

## application.properties (copien y adapten)

```properties
# Base de datos
spring.datasource.url=jdbc:h2:mem:miproyectodb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# H2 Console
spring.h2-console.enabled=true
spring.h2-console.path=/h2-console

# JSON legible
spring.jackson.serialization.indent_output=true

# Datos iniciales
spring.jpa.defer-datasource-initialization=true
```

## Imports frecuentes

- **Entidades:** `jakarta.persistence.*` + `lombok.*`
- **Repositorios:** `org.springframework.data.jpa.repository.JpaRepository`
- **Servicios:** `org.springframework.stereotype.Service` + `org.springframework.transaction.annotation.Transactional`
- **Controladores:** `org.springframework.web.bind.annotation.*` + `org.springframework.http.ResponseEntity`

## Si algo no compila

| Error | Solución |
|-------|----------|
| "Cannot resolve symbol Entity" | Falta `import jakarta.persistence.*` o la dependencia Spring Data JPA |
| "Table not found" | Revisar `@Table(name)` y `spring.jpa.hibernate.ddl-auto=create-drop` |
| "No default constructor" | Falta `@NoArgsConstructor` o Lombok no habilitado en IntelliJ |
| Endpoint devuelve 404 | Revisar `@RequestMapping` y que el controlador esté en subpaquete de la clase principal |
| JSON sale vacío `[]` | No hay datos. Crear `data.sql` o hacer POST primero |
