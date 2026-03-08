# Día 14: Proyecto Personal — API REST Completa

Ayer empezaron su proyecto propio. Hoy lo completan: todos los servicios y controladores, validación de datos, manejo de errores y subida a GitHub.

Prof. Juan Marcelo Gutierrez Miranda

**Curso IFCD0014 — Semana 3, Día 14 (Lunes)**
**Objetivo:** Completar la API REST para todas las entidades. Agregar validación, manejo global de errores, datos iniciales y subir a GitHub.

> Si ayer no terminaron las entidades, usen la primera hora para ponerse al día.

---

# 1. Completar Todos los Servicios y Controladores

Ayer implementaron el servicio y controlador de su entidad principal. Hoy hacen lo mismo para **todas** las entidades.

## Checklist por entidad

Cada entidad de su proyecto debe tener:

```
[ ] Entity   → modelo/MiEntidad.java          (@Entity, @Data, relaciones)
[ ] Repository → repositorio/MiEntidadRepository.java  (JpaRepository + findBy...)
[ ] Service  → servicio/MiEntidadService.java  (@Service, CRUD + métodos custom)
[ ] Controller → controlador/MiEntidadController.java (@RestController, endpoints)
```

## Endpoints mínimos por controlador

Cada controlador debe tener al menos:

| Método HTTP | Ruta | Acción |
|-------------|------|--------|
| GET | `/api/entidad` | Listar todos |
| GET | `/api/entidad/{id}` | Buscar por id |
| POST | `/api/entidad` | Crear nuevo |
| PUT | `/api/entidad/{id}` | Actualizar existente |
| DELETE | `/api/entidad/{id}` | Eliminar |
| GET | `/api/entidad/filtro/...` | Al menos 1 endpoint personalizado |

El endpoint personalizado es el que usa los métodos `findBy...` del repositorio. Por ejemplo:

```
GET /api/mascotas/especie/PERRO        → findByEspecie(Especie.PERRO)
GET /api/consultas/veterinario/3       → findByVeterinarioId(3L)
GET /api/productos/baratos?max=20      → findByPrecioLessThan(20.0)
```

---

# 2. Validación con Bean Validation

Hasta ahora, si alguien envía un POST con datos vacíos o inválidos, la entidad se guarda igual. Eso no puede pasar en producción.

## Paso 1: Agregar la dependencia

En el `pom.xml`, agreguen dentro de `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Recarguen Maven (`Ctrl + Shift + O` o icono del elefante).

## Paso 2: Anotaciones de validación en las entidades

Ejemplo con la entidad Mascota (adapten a sus entidades):

```java
package com.veterinaria.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false)
    private String nombre;

    @NotNull(message = "La especie es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Especie especie;

    private LocalDate fechaNacimiento;

    @Min(value = 0, message = "El peso no puede ser negativo")
    @Max(value = 500, message = "El peso no puede superar 500 kg")
    private double peso;

    @ManyToOne
    @JoinColumn(name = "dueno_id")
    private Dueno dueno;
}
```

## Anotaciones disponibles

| Para | Anotación | Qué valida |
|------|-----------|------------|
| Strings | `@NotNull`, `@NotBlank`, `@Size(min,max)`, `@Email`, `@Pattern(regexp)` | null, vacío, longitud, formato |
| Números | `@Min(value)`, `@Max(value)`, `@Positive`, `@PositiveOrZero` | rango, signo |
| Fechas | `@Past`, `@Future`, `@PastOrPresent` | temporalidad |

Todas aceptan `message = "texto de error personalizado"`.

## Paso 3: Activar validación en el controlador

Agreguen `@Valid` antes de `@RequestBody` en los métodos POST y PUT:

```java
import jakarta.validation.Valid;

// POST
@PostMapping
public ResponseEntity<Mascota> crear(@Valid @RequestBody Mascota mascota) {
    Mascota nueva = mascotaService.crear(mascota);
    return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
}

// PUT
@PutMapping("/{id}")
public ResponseEntity<Mascota> actualizar(
        @PathVariable Long id, @Valid @RequestBody Mascota mascota) {
    try {
        Mascota actualizada = mascotaService.actualizar(id, mascota);
        return ResponseEntity.ok(actualizada);
    } catch (RuntimeException e) {
        return ResponseEntity.notFound().build();
    }
}
```

## Paso 4: Probar la validación

Envíen un POST con datos inválidos (nombre vacío, peso negativo). Postman debe devolver `400 Bad Request` con los mensajes de error. El formato exacto lo mejoramos en el siguiente paso.

---

# 3. Manejo Global de Errores

Sin un manejo adecuado, Spring devuelve errores con mucha información técnica (stack traces) que el cliente no debería ver. Creamos un manejador global para devolver errores limpios.

## Paso 1: Crear la clase de respuesta de error

**📁** `src/main/java/com/miproyecto/controlador/ErrorResponse.java`

```java
package com.miproyecto.controlador;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private List<String> mensajes;
}
```

## Paso 2: Crear el GlobalExceptionHandler

**📁** `src/main/java/com/miproyecto/controlador/GlobalExceptionHandler.java`

```java
package com.miproyecto.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Errores de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        List<String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Error de validacion",
                errores
        );

        return ResponseEntity.badRequest().body(response);
    }

    // Entidad no encontrada
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            RuntimeException ex) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "No encontrado",
                List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Cualquier otro error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                List.of("Ocurrio un error inesperado")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
```

Noten:
- `@RestControllerAdvice`: intercepta excepciones de **todos** los controladores
- `@ExceptionHandler`: define qué tipo de excepción maneja cada método
- Las respuestas son JSON limpio, sin stack traces

Ahora los errores de validación devuelven `400` con mensajes limpios (`"nombre: El nombre es obligatorio"`), y los ids no encontrados devuelven `404` sin stack traces.

---

# 4. Crear una Colección de Tests en Postman

Organizar las peticiones en Postman les va a ahorrar tiempo y les servirá como documentación viva de su API.

## Paso 1: Crear la colección

En Postman: **New** → **Collection** → nombre: `Mi Proyecto — API REST`. Creen una carpeta por cada entidad.

## Paso 2: Crear las peticiones

Para cada endpoint, creen una petición. Ejemplo para Mascotas:

- **GET** `http://localhost:8080/api/mascotas` — Listar todas
- **GET** `http://localhost:8080/api/mascotas/1` — Buscar por id
- **POST** `http://localhost:8080/api/mascotas` — Body raw JSON:

```json
{
    "nombre": "Luna",
    "especie": "GATO",
    "fechaNacimiento": "2023-05-15",
    "peso": 4.5,
    "dueno": { "id": 1 }
}
```

- **PUT** `http://localhost:8080/api/mascotas/1` — Body raw JSON con datos actualizados
- **DELETE** `http://localhost:8080/api/mascotas/1`

## Paso 3: Exportar

Clic derecho en la colección → **Export** → Collection v2.1 → guardar el `.json` en la raíz del proyecto.

---

# 5. Datos Iniciales con data.sql

Creen el archivo `data.sql` en `src/main/resources/` con INSERT statements para todas sus entidades.

## Ejemplo (Veterinaria)

**📁** `src/main/resources/data.sql`

```sql
-- Primero: entidades SIN relaciones
INSERT INTO duenos (nombre, telefono, email, direccion)
    VALUES ('Maria Garcia', '612345678', 'maria@email.com', 'Calle Mayor 10');
INSERT INTO veterinarios (nombre, especialidad, numero_colegiado)
    VALUES ('Dr. Rodriguez', 'GENERAL', 'VET-001');
INSERT INTO medicamentos (nombre, laboratorio, precio)
    VALUES ('Amoxicilina', 'Pfizer', 12.50);

-- Segundo: entidades CON relaciones
INSERT INTO mascotas (nombre, especie, fecha_nacimiento, peso, dueno_id)
    VALUES ('Luna', 'GATO', '2022-03-15', 4.5, 1);
INSERT INTO consultas (fecha, motivo, diagnostico, coste, mascota_id, veterinario_id)
    VALUES (CURRENT_TIMESTAMP, 'Revision anual', 'Saludable', 35.00, 1, 1);

-- Tercero: tablas intermedias (@ManyToMany)
INSERT INTO consulta_medicamentos (consulta_id, medicamento_id) VALUES (1, 1);
```

El orden importa: primero entidades sin relaciones, después las que dependen de otras, al final las tablas intermedias. Pongan al menos 5-10 registros por entidad.

Asegúrense de tener `spring.jpa.defer-datasource-initialization=true` en `application.properties`.

---

# 6. Subir a GitHub

Este proyecto es su portafolio. Súbanlo a GitHub.

## Paso 1: Crear .gitignore

**📁** `.gitignore` (en la raíz del proyecto)

```
target/
*.class
.idea/
*.iml
out/
.vscode/
.DS_Store
*.log
.env
```

## Paso 2: Inicializar y subir

```bash
cd mi-proyecto/
git init
git add .
git commit -m "Proyecto inicial: entidades, repositorios, servicios, controladores REST"
```

En GitHub: **New repository** → nombre del proyecto → **NO** marcar "Initialize with README" → Create. Luego:

```bash
git remote add origin https://github.com/SU_USUARIO/SU_PROYECTO.git
git branch -M main
git push -u origin main
```

Verifiquen en GitHub que ven sus archivos Java, `pom.xml`, `application.properties` y `data.sql`.

**En una entrevista:** "Tengo una API REST con Spring Boot y JPA. Entidades con relaciones, validacion, manejo de errores y CRUD completo." Eso vale más que cualquier certificado.

---

# 7. Checklist de Fin de Día

```
[ ] TODAS las entidades tienen Service + Controller
[ ] Cada controlador tiene CRUD completo (GET, POST, PUT, DELETE)
[ ] Cada controlador tiene al menos 1 endpoint personalizado
[ ] Validación con @NotBlank, @NotNull, @Min, @Max en entidades
[ ] @Valid en los métodos POST y PUT de los controladores
[ ] GlobalExceptionHandler creado y funcionando
[ ] Colección de Postman con todos los endpoints organizados
[ ] data.sql con datos de prueba (5-10 registros por entidad)
[ ] .gitignore correcto
[ ] Proyecto subido a GitHub
```

No se preocupen si no marcan absolutamente todas las casillas. Lo importante es que la aplicación **funcione** y esté en GitHub.

---

# 8. Problemas Comunes y Soluciones

## "Infinite recursion" en el JSON

Causa: Entidad A referencia a B, y B referencia a A. Jackson intenta serializar A → B → A → B → infinito. Solo pasa con relaciones **bidireccionales** (@OneToMany + @ManyToOne).

**Solución 1:** `@JsonIgnore` en un lado de la relación:

```java
// En Dueno.java:
@OneToMany(mappedBy = "dueno")
@JsonIgnore                      // ← NO incluir en JSON
private List<Mascota> mascotas;
```

**Solución 2:** `@JsonManagedReference` (lado "uno") + `@JsonBackReference` (lado "muchos").

Si NO tienen `@OneToMany` (como en la Pizzería), no tendrán este problema.

## Otros problemas frecuentes

| Problema | Solución |
|----------|----------|
| "H2 table not found" | Revisar `ddl-auto=create-drop`, nombres en `data.sql` coincidan con `@Table`, columnas en snake_case |
| Validation not working | Verificar dependencia `spring-boot-starter-validation` y `@Valid` antes de `@RequestBody` |
| POST con @ManyToOne devuelve null | Pasar solo el id: `{ "dueno": { "id": 1 } }`, NO el objeto completo |
| Lombok no genera getters | Settings → Compiler → Enable annotation processing + Plugin Lombok habilitado |

---

# 9. Resumen y Próximos Pasos

```
╔══════════════════════════════════════════════════════════════╗
║              RESUMEN DEL DÍA 14                               ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  LO QUE HICIERON HOY:                                       ║
║  ✓ API REST completa para todas las entidades                ║
║  ✓ Validación de datos con Bean Validation                   ║
║  ✓ Manejo global de errores (@ControllerAdvice)              ║
║  ✓ Colección de Postman organizada                           ║
║  ✓ Datos iniciales con data.sql                              ║
║  ✓ Proyecto en GitHub                                        ║
║                                                              ║
║  LO QUE TIENEN AHORA:                                       ║
║  → Una aplicación Spring Boot profesional                    ║
║  → Con validación y manejo de errores                        ║
║  → Documentada con Postman                                   ║
║  → Publicada en GitHub                                       ║
║                                                              ║
║  PRÓXIMOS PASOS (próximos días):                             ║
║  → Testing (JUnit + MockMvc)                                 ║
║  → Seguridad (Spring Security)                               ║
║  → Base de datos real (MySQL/PostgreSQL)                     ║
║  → Despliegue (Docker / Railway / Render)                    ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```
