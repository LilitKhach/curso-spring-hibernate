# Analizador Financiero Frontend

React TypeScript frontend for the Financial Analyzer application.

## Features

- **Dashboard Summary**: Quick overview of total socias, total benefit, and average benefit
- **Socias Table**: Detailed view of all socias with their benefits and contracts
- **Date Filtering**: Filter data by date range
- **Expandable Contracts**: View contract details for each socia
- **Responsive Design**: Mobile-friendly interface

## Environment Variables

Create a `.env` file in the frontend directory:

```
REACT_APP_API_URL=http://localhost:8080/api
```

For production (Docker):
```
REACT_APP_API_URL=http://api:8080/api
```

## Available Scripts

### Development

```bash
npm start
```

Runs the app in development mode at [http://localhost:3000](http://localhost:3000)

### Build

```bash
npm run build
```

Builds the app for production in the `build` folder.

### Test

```bash
npm test
```

Runs the test suite in interactive watch mode.

## Docker

### Build Image

```bash
docker build -t analizador-frontend .
```

### Run Container

```bash
docker run -p 3000:80 analizador-frontend
```

## Project Structure

```
frontend/
├── public/
├── src/
│   ├── api/
│   │   └── apiClient.ts          # API client for backend
│   ├── components/
│   │   ├── Dashboard/             # Main dashboard component
│   │   ├── DashboardSummary/      # Summary statistics cards
│   │   ├── SociasTable/           # Socias table with expandable rows
│   │   └── DateFilter/            # Date range filter
│   ├── App.tsx                    # Main app component
│   ├── App.css                    # Global styles
│   └── index.tsx                  # Entry point
├── Dockerfile                     # Docker configuration
├── .env                          # Environment variables
└── package.json                  # Dependencies
```

## Technologies

- **React 18**: UI library
- **TypeScript**: Type-safe JavaScript
- **CSS3**: Styling with flexbox and grid
- **Fetch API**: HTTP client for API calls

## Features Overview

### Dashboard Summary
- Total number of socias
- Total benefit across all socias
- Average benefit per socia
- Beautiful gradient cards with animations

### Socias Table
- Sortable and expandable rows
- Shows socia name, last name, and total benefit
- Expandable rows to view contracts
- Responsive design for mobile devices

### Date Filter
- Filter data by start and end date
- Clear filters to reset
- Disabled state during loading

## API Integration

The frontend integrates with the following API endpoints:

- `GET /socias` - Get all socias
- `GET /socias/dashboard` - Get detailed dashboard data
- `GET /socias/dashboard/summary` - Get summary statistics
- `GET /contratos/socia/{sociaId}` - Get contracts for a socia
- `GET /transacciones/contrato/{contratoId}` - Get transactions for a contract

## Styling

The application uses a modern design with:
- Gradient backgrounds
- Smooth animations and transitions
- Responsive grid layouts
- Mobile-first approach

## Future Enhancements

- [ ] Add transaction detail view
- [ ] Implement charts and graphs for analytics
- [ ] Add socia management (CRUD operations)
- [ ] Export data to CSV/PDF
- [ ] Add user authentication
- [ ] Implement dark mode

