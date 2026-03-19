import { useState } from 'react';
import './SociasTable.css';
import { SociaBeneficio } from '../../api/apiClient';

interface Props {
  data: SociaBeneficio[];
  loading: boolean;
  onSelectSocia?: (socia: SociaBeneficio) => void;
}

const SociasTable: React.FC<Props> = ({ data, loading, onSelectSocia }) => {
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
    }).format(value);
  };

  if (loading) {
    return <div className="socias-table loading">Cargando datos de socias...</div>;
  }

  if (data.length === 0) {
    return <div className="socias-table empty">No hay socias disponibles</div>;
  }

  const toggleExpand = (id: number) => {
    setExpandedId(expandedId === id ? null : id);
  };

  return (
    <div className="socias-table">
      <div className="table-header">
        <h2>Socias y sus Beneficios</h2>
      </div>
      <div className="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Apellido</th>
              <th>Beneficio Total</th>
              <th>Contratos</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {data.map((socia) => (
              <> key={socia.sociaId}
                <tr className="socia-row">
                  <td>{socia.nombre}</td>
                  <td>{socia.apellido}</td>
                  <td className="beneficio">{formatCurrency(socia.beneficioTotal)}</td>
                  <td className="contratos-count">{socia.contratos.length}</td>
                  <td className="actions">
                    <button
                      className="expand-btn"
                      onClick={() => toggleExpand(socia.sociaId)}
                      title={expandedId === socia.sociaId ? 'Contraer' : 'Expandir'}
                    >
                      {expandedId === socia.sociaId ? '−' : '+'}
                    </button>
                    {onSelectSocia && (
                      <button
                        className="view-btn"
                        onClick={() => onSelectSocia(socia)}
                      >
                        Ver
                      </button>
                    )}
                  </td>
                </tr>
                {expandedId === socia.sociaId && (
                  <tr className="expansion-row">
                    <td colSpan={5}>
                      <div className="contratos-detail">
                        <h4>Contratos ({socia.contratos.length})</h4>
                        {socia.contratos.length > 0 ? (
                          <ul>
                            {socia.contratos.map((contrato) => (
                              <li key={contrato.contratoId}>
                                <span className="contrato-numero">{contrato.contratoNumero}</span>
                                <span className="contrato-beneficio">{formatCurrency(contrato.beneficio)}</span>
                              </li>
                            ))}
                          </ul>
                        ) : (
                          <p>No hay contratos para esta socia</p>
                        )}
                      </div>
                    </td>
                  </tr>
                )}
              </>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default SociasTable;




