# Frontend Setup Guide

## Quick Start

### Prerequisites

- Node.js 16+ (with npm)
- React 18
- TypeScript

### Installation

1. **Navigate to frontend directory**
```bash
cd frontend
```

2. **Install dependencies**
```bash
npm install
```

3. **Create environment file** (if not exists)
```bash
# For development (local)
echo "REACT_APP_API_URL=http://localhost:8080/api" > .env

# For Docker
echo "REACT_APP_API_URL=http://api:8080/api" > .env.production
```

### Development

**Start development server:**
```bash
npm start
```

The app will open at http://localhost:3000

**Development environment variable:**
- `REACT_APP_API_URL=http://localhost:8080/api`

### Production Build

**Build the application:**
```bash
npm run build
```

Creates optimized production build in `build/` folder.

**Production environment variable:**
- `REACT_APP_API_URL=http://api:8080/api` (for Docker)

## Docker Setup

### Build Docker Image

```bash
docker build -t analizador-frontend ./frontend
```

### Run with Docker Compose

From root directory:
```bash
docker-compose up -d
```

This will start:
- Frontend on port 3000
- Backend API on port 8080
- PostgreSQL on port 5432
- Adminer on port 9090

### Access URLs

- **Frontend**: http://localhost:3000
- **API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Database Admin**: http://localhost:9090

## Project Structure

```
frontend/
├── public/
│   └── index.html              # Main HTML template
├── src/
│   ├── api/
│   │   └── apiClient.ts        # API client
│   ├── components/
│   │   ├── Dashboard/
│   │   │   ├── Dashboard.tsx
│   │   │   └── Dashboard.css
│   │   ├── DashboardSummary/
│   │   │   ├── DashboardSummary.tsx
│   │   │   └── DashboardSummary.css
│   │   ├── SociasTable/
│   │   │   ├── SociasTable.tsx
│   │   │   └── SociasTable.css
│   │   └── DateFilter/
│   │       ├── DateFilter.tsx
│   │       └── DateFilter.css
│   ├── App.tsx                 # Main component
│   ├── App.css                 # Global styles
│   └── index.tsx               # Entry point
├── Dockerfile                  # Docker configuration
├── .env                        # Development env vars
├── .env.production             # Production env vars
├── .gitignore
├── package.json
├── tsconfig.json
└── README.md
```

## API Integration

The frontend communicates with the backend API at `/api` endpoint.

### Available API Endpoints

**Socias:**
- `GET /socias` - Get all socias
- `GET /socias/{id}` - Get specific socia
- `POST /socias` - Create socia
- `PUT /socias/{id}` - Update socia
- `DELETE /socias/{id}` - Delete socia

**Dashboard:**
- `GET /socias/dashboard` - Get detailed dashboard
- `GET /socias/dashboard/summary` - Get summary statistics

**Contracts:**
- `GET /contratos/socia/{sociaId}` - Get contracts for socia
- `GET /contratos/socia/{sociaId}/contrato/{contratoId}` - Get contract details

**Transactions:**
- `GET /transacciones/contrato/{contratoId}` - Get transactions for contract

## Features

### Dashboard Summary
- Total number of socias
- Total benefit amount
- Average benefit per socia
- Beautiful gradient cards with animations

### Socias Table
- Display all socias with their benefits
- Expandable rows to view contracts
- Responsive design for mobile

### Date Filter
- Filter data by date range
- Optional start and end dates
- Clear button to reset filters

## Styling

The application uses:
- CSS3 (Flexbox and Grid)
- Gradient backgrounds
- Smooth animations and transitions
- Mobile-first responsive design
- Consistent color scheme

## Environment Variables

### Development (.env)
```
REACT_APP_API_URL=http://localhost:8080/api
```

### Production (.env.production)
```
REACT_APP_API_URL=http://api:8080/api
```

## Troubleshooting

### Frontend can't connect to API

1. **Check API is running:**
   ```bash
   curl http://localhost:8080/api/socias
   ```

2. **Verify environment variable:**
   - Check `.env` file has correct `REACT_APP_API_URL`
   - Restart dev server after changing env var

3. **Check browser console:**
   - Open DevTools (F12)
   - Check Network tab for failed requests
   - Check Console for error messages

### Port 3000 already in use

```bash
# Kill process on port 3000
lsof -ti:3000 | xargs kill -9  # macOS/Linux
netstat -ano | findstr :3000    # Windows
```

### CORS Issues

If you see CORS errors, the backend needs CORS configuration. This should already be configured in the Spring Boot app.

## npm Scripts

```bash
npm start      # Start development server
npm run build  # Create production build
npm test       # Run tests
npm eject      # Eject configuration (not recommended)
```

## Next Steps

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Start development:**
   ```bash
   npm start
   ```

3. **Or run with Docker:**
   ```bash
   docker-compose up -d
   ```

## Support

For issues or questions, check:
- `/frontend/README.md` - Frontend specific docs
- `/DOCKER_SETUP.md` - Docker configuration
- Backend API Swagger docs at http://localhost:8080/swagger-ui.html

