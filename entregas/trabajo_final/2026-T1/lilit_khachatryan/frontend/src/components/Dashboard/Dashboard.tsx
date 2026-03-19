import { useState, useEffect } from 'react';
import './Dashboard.css';
import DashboardSummary from '../DashboardSummary/DashboardSummary';
import SociasTable from '../SociasTable/SociasTable';
import DateFilter from '../DateFilter/DateFilter';
import apiClient, { DashboardSummary as DashboardSummaryType, SociaBeneficio } from '../../api/apiClient';

const Dashboard: React.FC = () => {
  const [summary, setSummary] = useState<DashboardSummaryType | null>(null);
  const [socias, setSocias] = useState<SociaBeneficio[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async (from?: string, to?: string) => {
    setLoading(true);
    setError(null);
    try {
      const [summaryData, sociasData] = await Promise.all([
        apiClient.getDashboardSummary(from, to),
        apiClient.getDashboard(from, to),
      ]);
      setSummary(summaryData);
      setSocias(sociasData);
    } catch (err) {
      setError('Error al cargar los datos del dashboard');
      console.error('Dashboard error:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleFilter = (from?: string, to?: string) => {
    fetchData(from, to);
  };

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div className="header-content">
          <h1>Analizador Financiero</h1>
          <p>Panel de control para gestionar socias, contratos y transacciones</p>
        </div>
      </header>

      <div className="dashboard-container">
        {error && <div className="error-banner">{error}</div>}

        <section className="dashboard-section">
          <h2>Filtrar por fecha</h2>
          <DateFilter onFilter={handleFilter} loading={loading} />
        </section>

        <section className="dashboard-section">
          <h2>Resumen General</h2>
          <DashboardSummary data={summary} loading={loading} />
        </section>

        <section className="dashboard-section">
          <h2>Detalle de Socias</h2>
          <SociasTable data={socias} loading={loading} />
        </section>

        {socias.length === 0 && !loading && (
          <section className="empty-state">
            <div className="empty-content">
              <h3>No hay datos disponibles</h3>
              <p>Asegúrate de que hay socias registradas en el sistema</p>
            </div>
          </section>
        )}
      </div>
    </div>
  );
};

export default Dashboard;



