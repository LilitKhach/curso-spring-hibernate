import './DashboardSummary.css';
import { DashboardSummary as DashboardSummaryType } from '../../api/apiClient';

interface Props {
  data: DashboardSummaryType | null;
  loading: boolean;
}

const DashboardSummary: React.FC<Props> = ({ data, loading }) => {
  if (loading) {
    return <div className="dashboard-summary loading">Cargando resumen...</div>;
  }

  if (!data) {
    return <div className="dashboard-summary error">No hay datos disponibles</div>;
  }

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
    }).format(value);
  };

  return (
    <div className="dashboard-summary">
      <div className="summary-card">
        <div className="card-header">
          <h3>Total de Socias</h3>
        </div>
        <div className="card-value">{data.totalSocias}</div>
      </div>

      <div className="summary-card">
        <div className="card-header">
          <h3>Beneficio Total</h3>
        </div>
        <div className="card-value">{formatCurrency(data.totalBeneficio)}</div>
      </div>

      <div className="summary-card">
        <div className="card-header">
          <h3>Beneficio Promedio</h3>
        </div>
        <div className="card-value">{formatCurrency(data.averageBeneficio)}</div>
      </div>
    </div>
  );
};

export default DashboardSummary;
