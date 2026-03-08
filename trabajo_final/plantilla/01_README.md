# Trabajo Final — [Tu Nombre]

## Blueprint elegido
[Nombre del blueprint o dominio propio]

## Descripcion
[2-3 lineas describiendo que hace tu aplicacion]

## Entidades

| Entidad | Campos principales | Relaciones |
|---------|-------------------|------------|
| [Ej: Pelicula] | titulo, anio, genero | ManyToOne con Director, ManyToMany con Actor |
| ... | ... | ... |

## Endpoints de la API

| Verbo | URL | Descripcion |
|-------|-----|-------------|
| GET | `/api/peliculas` | Listar todas |
| POST | `/api/peliculas` | Crear nueva |
| ... | ... | ... |

## Como ejecutar

```bash
# Con Docker
docker compose up -d

# Sin Docker (H2)
mvn spring-boot:run
```
