import { useState } from 'react';
import './DateFilter.css';

interface Props {
  onFilter: (from?: string, to?: string) => void;
  loading?: boolean;
}

const DateFilter: React.FC<Props> = ({ onFilter, loading = false }) => {
  const [fromDate, setFromDate] = useState<string>('');
  const [toDate, setToDate] = useState<string>('');

  const handleFilter = () => {
    onFilter(fromDate || undefined, toDate || undefined);
  };

  const handleClear = () => {
    setFromDate('');
    setToDate('');
    onFilter(undefined, undefined);
  };

  return (
    <div className="date-filter">
      <div className="filter-group">
        <label htmlFor="fromDate">Desde:</label>
        <input
          id="fromDate"
          type="date"
          value={fromDate}
          onChange={(e) => setFromDate(e.target.value)}
          disabled={loading}
        />
      </div>

      <div className="filter-group">
        <label htmlFor="toDate">Hasta:</label>
        <input
          id="toDate"
          type="date"
          value={toDate}
          onChange={(e) => setToDate(e.target.value)}
          disabled={loading}
        />
      </div>

      <div className="filter-actions">
        <button
          className="btn-filter"
          onClick={handleFilter}
          disabled={loading}
        >
          {loading ? 'Filtrando...' : 'Filtrar'}
        </button>
        <button
          className="btn-clear"
          onClick={handleClear}
          disabled={loading}
        >
          Limpiar
        </button>
      </div>
    </div>
  );
};

export default DateFilter;


