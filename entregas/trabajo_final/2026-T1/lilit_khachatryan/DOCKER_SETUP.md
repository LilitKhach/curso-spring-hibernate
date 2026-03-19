# Docker setup instructions

## Complete Docker Setup

The project now includes Docker configuration for both frontend and backend.

### Services

1. **frontend** (React) - Port 3000
2. **api** (Spring Boot) - Port 8080
3. **db** (PostgreSQL) - Port 5432
4. **adminer** (Database UI) - Port 9090

### Running the Application

#### Prerequisites

- Docker
- Docker Compose

#### Start all services

```bash
docker-compose up -d
```

#### Stop all services

```bash
docker-compose down
```

#### View logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f frontend
docker-compose logs -f api
docker-compose logs -f db
```

### Access the Application

- **Frontend**: http://localhost:3000
- **API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Database Admin**: http://localhost:9090

### Environment Configuration

#### Frontend (.env in frontend directory)

```
REACT_APP_API_URL=http://localhost:8080/api  # Development
REACT_APP_API_URL=http://api:8080/api        # Docker
```

#### Backend (environment in docker-compose.yml)

- `SERVER_PORT=8080`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/analizador`
- `SPRING_DATASOURCE_USERNAME=postgres`
- `SPRING_DATASOURCE_PASSWORD=secret`

### Building Images

#### Build all services

```bash
docker-compose build
```

#### Build specific service

```bash
docker-compose build frontend
docker-compose build api
```

### Development vs Production

#### Development (Local)

Use `http://localhost` references in `.env` file:

```
REACT_APP_API_URL=http://localhost:8080/api
```

Run locally without Docker:

```bash
# Terminal 1 - Backend
cd analizador-de-contratos
mvn spring-boot:run

# Terminal 2 - Frontend
cd frontend
npm start
```

#### Production (Docker)

The docker-compose.yml uses service names (internal DNS):

- Frontend environment: `REACT_APP_API_URL=http://api:8080/api`
- Backend port: `8080` (internally)
- Database: `jdbc:postgresql://db:5432/analizador`

### Common Issues

**Frontend can't connect to API**
- Ensure both services are running: `docker-compose ps`
- Check API is healthy: `docker-compose logs api`
- Verify environment variable in frontend: Check `.env` file

**Database connection issues**
- Ensure database is healthy: `docker-compose logs db`
- Check PostgreSQL is running on port 5432
- Verify credentials in docker-compose.yml

**Port already in use**
- Modify ports in docker-compose.yml
- Or stop conflicting services: `docker ps` and `docker stop <container>`

### Cleanup

#### Remove containers and volumes

```bash
docker-compose down -v
```

#### Remove images

```bash
docker-compose down --rmi all
```

#### Remove all Docker resources

```bash
docker system prune -a
```

