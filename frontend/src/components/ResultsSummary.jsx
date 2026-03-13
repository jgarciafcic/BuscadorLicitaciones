import { getExportUrl } from '../services/api';
import './ResultsSummary.css';

function formatImporte(valor) {
  if (valor == null) return '0,00 €';
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 2,
  }).format(valor);
}

function ResultsSummary({ stats, loading, filtros }) {
  if (loading) {
    return <div className="results-summary loading">Buscando...</div>;
  }

  if (!stats) return null;

  return (
    <div className="results-summary">
      <span>
        Se han encontrado <strong>{stats.totalLicitaciones.toLocaleString('es-ES')}</strong> licitaciones
        por un importe total de <strong>{formatImporte(stats.sumaImportes)}</strong>
      </span>
      <a className="btn-export" href={getExportUrl(filtros)} download="licitaciones.csv">
        Exportar CSV
      </a>
    </div>
  );
}

export default ResultsSummary;
