import { getCpvDescription } from '../data/cpvCodes';
import './ResultsTable.css';

const SORTABLE_COLUMNS = [
  { field: 'fechaActualizacion', label: 'Fecha pub.', className: 'col-fecha' },
  { field: 'fechaLimiteOfertas', label: 'Límite ofertas', className: 'col-plazo' },
  { field: null, label: 'Título licitación', className: 'col-titulo' },
  { field: null, label: 'CPV', className: 'col-cpv' },
  { field: null, label: 'CCAA', className: 'col-ccaa' },
  { field: null, label: 'Pliegos', className: 'col-pliegos' },
  { field: 'importeSinImpuestos', label: 'Importe', className: 'col-importe' },
];

function SortArrow({ field, sort }) {
  if (!sort || sort.field !== field) return <span className="sort-arrow inactive">⇅</span>;
  return <span className="sort-arrow active">{sort.direction === 'asc' ? '↑' : '↓'}</span>;
}

function formatFecha(dateStr) {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  return d.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function formatFechaHora(fecha, hora) {
  if (!fecha) return '—';
  const partes = fecha.split('-');
  const fechaFmt = `${partes[2]}/${partes[1]}/${partes[0]}`;
  if (!hora) return fechaFmt;
  return `${fechaFmt} ${hora.substring(0, 5)}`;
}

function plazoClass(fecha) {
  if (!fecha) return '';
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  const limite = new Date(fecha + 'T00:00:00');
  const dias = Math.ceil((limite - hoy) / (1000 * 60 * 60 * 24));
  if (dias < 7) return 'plazo-urgente';
  if (dias < 15) return 'plazo-proximo';
  return '';
}

function formatImporte(valor) {
  if (valor == null) return '—';
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 2,
  }).format(valor);
}

function CpvBadges({ cpvCodes, cpvPrefix }) {
  if (!cpvCodes) return <span>—</span>;
  const codes = cpvCodes.split(',').map((c) => c.trim()).filter(Boolean);
  return (
    <div className="cpv-badges">
      {codes.map((code) => {
        const desc = getCpvDescription(code);
        const matches = cpvPrefix && code.startsWith(cpvPrefix);
        return (
          <span
            key={code}
            className={`cpv-badge${matches ? ' cpv-match' : ''}`}
            title={desc ? `${code} — ${desc}` : code}
          >
            {code}
          </span>
        );
      })}
    </div>
  );
}

function ResultsTable({ resultados, loading, onPageChange, sort, onSortChange, onRowClick, cpvPrefix }) {
  if (loading) {
    return <div className="table-loading">Cargando resultados...</div>;
  }

  if (!resultados || resultados.content.length === 0) {
    return <div className="table-empty">No se han encontrado licitaciones con los filtros seleccionados.</div>;
  }

  const { content, number, size, totalElements, totalPages } = resultados;
  const desde = number * size + 1;
  const hasta = Math.min((number + 1) * size, totalElements);

  return (
    <div className="results-table-wrapper">
      <table className="results-table">
        <thead>
          <tr>
            {SORTABLE_COLUMNS.map((col) => (
              <th
                key={col.label}
                className={`${col.className}${col.field ? ' sortable' : ''}`}
                onClick={col.field ? () => onSortChange(col.field) : undefined}
              >
                {col.label}
                {col.field && <SortArrow field={col.field} sort={sort} />}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {content.map((lic) => (
            <tr key={lic.entryId} className="clickable-row" onClick={() => onRowClick(lic)}>
              <td className="col-fecha">{formatFecha(lic.fechaActualizacion)}</td>
              <td className={`col-plazo ${plazoClass(lic.fechaLimiteOfertas)}`}>{formatFechaHora(lic.fechaLimiteOfertas, lic.horaLimiteOfertas)}</td>
              <td className="col-titulo">
                <span className="organo">{lic.organoContratacion}</span>
                <span className="titulo-objeto">
                  {lic.objeto}
                  {lic.enlacePlacsp && (
                    <a href={lic.enlacePlacsp} target="_blank" rel="noopener noreferrer"
                       className="titulo-link-placsp" title="Ver en Plataforma de Contratación"
                       onClick={(e) => e.stopPropagation()}>
                      &#x2197;
                    </a>
                  )}
                </span>
              </td>
              <td className="col-cpv">
                <CpvBadges cpvCodes={lic.cpvCodes} cpvPrefix={cpvPrefix} />
              </td>
              <td className="col-ccaa">{lic.lugarEjecucion || '—'}</td>
              <td className="col-pliegos">
                {lic.urlPcap && <a href={lic.urlPcap} target="_blank" rel="noopener noreferrer" title="Pliego cláusulas administrativas" onClick={(e) => e.stopPropagation()}>PCAP</a>}
                {lic.urlPcap && lic.urlPpt && ' | '}
                {lic.urlPpt && <a href={lic.urlPpt} target="_blank" rel="noopener noreferrer" title="Pliego prescripciones técnicas" onClick={(e) => e.stopPropagation()}>PPT</a>}
                {(lic.urlPcap || lic.urlPpt) && lic.urlAnuncio && ' | '}
                {lic.urlAnuncio && <a href={lic.urlAnuncio} target="_blank" rel="noopener noreferrer" title="Anuncio de licitación" onClick={(e) => e.stopPropagation()}>Anuncio</a>}
                {!lic.urlPcap && !lic.urlPpt && !lic.urlAnuncio && '—'}
              </td>
              <td className="col-importe">{formatImporte(lic.importeSinImpuestos)}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="pagination">
        <button
          disabled={number === 0}
          onClick={() => onPageChange(number - 1)}
        >
          Anterior
        </button>
        <span className="pagination-info">
          Mostrando del {desde} al {hasta} de {totalElements.toLocaleString('es-ES')} licitaciones
        </span>
        <button
          disabled={number >= totalPages - 1}
          onClick={() => onPageChange(number + 1)}
        >
          Siguiente
        </button>
      </div>
    </div>
  );
}

export default ResultsTable;
