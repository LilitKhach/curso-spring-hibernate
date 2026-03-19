# Analizador Financiero

Panel de control para gestionar socias, contratos y transacciones.

---

## Descripción General de la Arquitectura

Este proyecto cuenta con una arquitectura **full-stack completa**:

### Backend (Spring Boot Java)
- **Ubicación:** Directorio raíz
- **Puerto:** 8080
- **Base de datos:** PostgreSQL
- **API:** REST con documentación Swagger

### Frontend (React TypeScript)
- **Ubicación:** `/frontend`
- **Puerto:** 3000
- **Framework UI:** React 18 con TypeScript
- **Estilos:** CSS3 con diseño responsivo

### Base de Datos
- **Tipo:** PostgreSQL
- **Puerto:** 5432
- **Panel de administración:** Adminer en el puerto 9090

---

## Estructura del Proyecto
```
analizador-de-contratos/
├── src/ # Backend code
│ ├── main/java/com/finanalizador/
│ │ ├── AnalizadorFinancieroApplication.java
│ │ ├── controlador/ # REST controllers
│ │ ├── dto/ # Data Transfer Objects
│ │ ├── modelo/ # Entity models
│ │ ├── repositorio/ # Data access layer (repositories)
│ │ └── servicio/ # Business logic
│ └── resources/
│ └── application.properties
├── frontend/ # React frontend
│ ├── public/
│ ├── src/
│ ├── Dockerfile
│ ├── .env
│ ├── package.json
│ └── tsconfig.json
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── FRONTEND_SETUP.md
├── DOCKER_SETUP.md
└── README.md
```

## Endpoints de la API

### Gestión de Socias
- `POST /api/socias` – Crear socia  
- `GET /api/socias` – Obtener todas las socias  
- `GET /api/socias/{id}` – Obtener socia específica  
- `PUT /api/socias/{id}` – Actualizar socia  
- `DELETE /api/socias/{id}` – Eliminar socia  

### Dashboard y Analítica
- `GET /api/socias/{id}/beneficio` – Beneficio de una socia por rango de fechas  
- `GET /api/socias/dashboard` – Datos detallados del dashboard  
- `GET /api/socias/dashboard/summary` – Resumen de estadísticas  

### Gestión de Contratos
- `GET /api/contratos/{id}` – Obtener contrato específico  
- `GET /api/contratos/socia/{sociaId}` – Contratos de una socia  
- `POST /api/contratos` – Crear contrato  
- `PUT /api/contratos/{id}` – Actualizar contrato  
- `DELETE /api/contratos/{id}` – Eliminar contrato  

### Gestión de Transacciones
- `GET /api/transacciones/{id}` – Obtener transacción específica  
- `GET /api/transacciones/contrato/{contratoId}` – Transacciones por contrato  
- `POST /api/transacciones` – Crear transacción  
- `PUT /api/transacciones/{id}` – Actualizar transacción  
- `DELETE /api/transacciones/{id}` – Eliminar transacción  

---

## Características del Frontend

- **Dashboard:** Resumen de socias, beneficios totales y promedio  
- **Filtro por fecha:** Selección de rango para visualizar datos  
- **Tabla de Socias:** Filas expandibles mostrando contratos  
- **Diseño responsivo:** Compatible con desktop, tablet y móvil  
- **Componentes:**  
  - Dashboard  
  - DashboardSummary  
  - SociasTable  
  - DateFilter  
- **Estilos:** Gradientes modernos, animaciones suaves, sombras profesionales

---

## Inicio Rápido

### Opción 1: Desarrollo Local

**Backend:**
mvn spring-boot:run

**Frontend:**
cd frontend
npm install
npm start

**Acceso:**

Frontend: http://localhost:3000
API: http://localhost:8080/api
Swagger: http://localhost:8080/swagger-ui.html

### Opción 2: Docker (Recomendado)
docker-compose up -d

**Acceso:**

Frontend: http://localhost:3000
API: http://localhost:8080/api
Swagger: http://localhost:8080/swagger-ui.html
Adminer: http://localhost:9090

## Configuración de Entorno
### Desarrollo

**Backend (application.properties):**

server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/analizador

**Frontend (.env):**

REACT_APP_API_URL=http://localhost:8080/api

**Producción (Docker)**

Backend: puerto 8080
Frontend: puerto 3000, REACT_APP_API_URL=http://localhost:8080/api
Base de datos: PostgreSQL 16, puerto 5432

**Tecnologías Usadas**

Backend: Java 17+, Spring Boot 3.x, Spring Data JPA, PostgreSQL, Swagger/OpenAPI, Lombok, Maven
Frontend: React 18, TypeScript, CSS3, Node.js, npm
DevOps: Docker, Docker Compose, Adminer

**Esquema de la Base de Datos**

- **Socia**: Información de socios
- **Contrato**: Contratos asociados a socias
- **Transaccion**: Transacciones de contratos
- **TipoTransaccion**: Enum (INGRESO/GASTO)

**Relaciones:**

Socia → Contrato (1:N)
Contrato → Transaccion (1:N)

### Próximos Pasos**

**Configurar frontend:**

cd frontend
npm install
npm start

**O usar Docker:**

docker-compose up -d

**Acceso a la aplicación:** http://localhost:3000

**Agregar datos de prueba vía Swagger:** http://localhost:8080/swagger-ui.html

### Características Implementadas

- API REST backend con 3 controladores
- Dashboard UI en React con TypeScript
- Filtrado por rango de fechas
- Diseño responsivo
- Dockerización completa
- Integración con PostgreSQL
- Documentación Swagger
- Arquitectura limpia (Service/Repository/Controller)
- Manejo global de excepciones
- DTOs para transferencia de datos
