# Trabajo Final — Lilit Khachatryan‌‌‌​‌​‌‌​﻿‍‍‌‍​‍​﻿​﻿​﻿​‍​﻿‌‌‌‍‌​‌‍​‍‌‍‌‍‌‍​‌​﻿‌‌​﻿‍‌‌‍​‌‌‍​‌​﻿​‍‌‍‌​​﻿‍‌‌‍‌‌

## Blueprint elegido
Analizador Financiero

## Descripcion
Panel de control para la gestión de socias, contratos y transacciones financieras.
Esta API permite gestionar socios, contratos y transacciones. 
Las pequeñas y medianas empresas pueden usarla para visualizar sus datos en un panel de control, 
filtrarlos y descubrir socios más beneficiosos según diferentes fechas.

## Entidades

| Entidad | Campos principales | Relaciones |
| --------| ------------------| ------------|
| Socia |nombre, apellido | OneToMany con Contratos |
| Contrato | contratoNumero | ManyToOne con Socia, OneToMany con Transacciones |
| Transacción | monto, fecha, descripción, tipo | ManyToOne con Contrato |

## Endpoints de la API

| Verbo | URL | Descripcion |
|-------|-----|-------------|
| GET | `/api/socias` | Listar todas las socias registradas |
| GET | `/api/socias/{id}` | Obtener una socia por su ID |
| POST | `/api/socias` | Crear una nueva socia |
| PUT | `/api/socias/{id}` | Actualizar los datos de una socia |
| DELETE | `/api/socias/{id}` | Eliminar una socia |
| GET | `/api/socias/{id}/beneficio` | Calcular el beneficio total de una socia, con filtro opcional por fechas `from` y `to` |
| GET | `/api/socias/dashboard` | Obtener los datos del dashboard con beneficio por socia y contratos, con filtros opcionales |
| GET | `/api/socias/dashboard/summary` | Obtener el resumen general del dashboard: total de socias, beneficio total y beneficio medio |
| GET | `/api/contratos/{id}` | Obtener un contrato por su ID |
| GET | `/api/contratos/socia/{sociaId}` | Listar los contratos de una socia, con filtro opcional por fechas |
| GET | `/api/contratos/socia/{sociaId}/contrato/{contratoId}` | Obtener el detalle de un contrato concreto de una socia |
| POST | `/api/contratos` | Crear un nuevo contrato |
| PUT | `/api/contratos/{id}` | Actualizar un contrato existente |
| DELETE | `/api/contratos/{id}` | Eliminar un contrato |
| GET | `/api/transacciones/{id}` | Obtener una transaccion por su ID |
| GET | `/api/transacciones/contrato/{contratoId}` | Listar las transacciones de un contrato, con filtro opcional por fechas |
| POST | `/api/transacciones` | Crear una nueva transaccion |
| PUT | `/api/transacciones/{id}` | Actualizar una transaccion existente |
| DELETE | `/api/transacciones/{id}` | Eliminar una transaccion |

## Como ejecutar

```bash
# Con Docker
docker compose up -d

# Sin Docker (H2)
mvn spring-boot:run
```
