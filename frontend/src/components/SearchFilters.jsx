import { useState } from 'react';
import './SearchFilters.css';

const ESTADOS = [
  { value: '', label: 'Todos' },
  { value: 'PUB', label: 'En plazo (PUB)' },
  { value: 'PRE', label: 'Anuncio previo (PRE)' },
  { value: 'EV', label: 'Pendiente adjudicación (EV)' },
  { value: 'ADJ', label: 'Adjudicada (ADJ)' },
  { value: 'RES', label: 'Resuelta (RES)' },
  { value: 'ANU', label: 'Anulada (ANU)' },
];

const TIPOS_CONTRATO = [
  { value: '', label: 'Todos' },
  { value: '2', label: 'Servicios' },
  { value: '1', label: 'Obras' },
  { value: '3', label: 'Suministros' },
  { value: '7', label: 'Concesión de obras' },
  { value: '8', label: 'Concesión de servicios' },
  { value: '21', label: 'Privado' },
  { value: '31', label: 'Administrativo especial' },
  { value: '40', label: 'Patrimonial' },
  { value: '50', label: 'Otros' },
];

const CCAA = [
  { value: '', label: 'Todas' },
  { value: 'ES61', label: 'Andalucía' },
  { value: 'ES24', label: 'Aragón' },
  { value: 'ES70', label: 'Canarias' },
  { value: 'ES13', label: 'Cantabria' },
  { value: 'ES42', label: 'Castilla-La Mancha' },
  { value: 'ES41', label: 'Castilla y León' },
  { value: 'ES51', label: 'Cataluña' },
  { value: 'ES63', label: 'Ceuta' },
  { value: 'ES52', label: 'Comunitat Valenciana' },
  { value: 'ES43', label: 'Extremadura' },
  { value: 'ES11', label: 'Galicia' },
  { value: 'ES53', label: 'Illes Balears' },
  { value: 'ES23', label: 'La Rioja' },
  { value: 'ES30', label: 'Comunidad de Madrid' },
  { value: 'ES64', label: 'Melilla' },
  { value: 'ES62', label: 'Región de Murcia' },
  { value: 'ES22', label: 'Comunidad Foral de Navarra' },
  { value: 'ES21', label: 'País Vasco' },
  { value: 'ES12', label: 'Principado de Asturias' },
];

const PROCEDIMIENTOS = [
  { value: '', label: 'Todos' },
  { value: '1', label: 'Abierto' },
  { value: '2', label: 'Restringido' },
  { value: '3', label: 'Negociado con publicidad' },
  { value: '4', label: 'Negociado sin publicidad' },
  { value: '5', label: 'Diálogo competitivo' },
  { value: '6', label: 'Contrato menor' },
  { value: '100', label: 'Simplificado' },
];

const PLAZOS = [
  { value: '', label: 'Sin filtro' },
  { value: '0', label: 'En plazo' },
  { value: '7', label: 'Expiran en < 7 días' },
  { value: '15', label: 'Expiran en < 15 días' },
  { value: '30', label: 'Expiran en < 30 días' },
];

function SearchFilters({ filtros, setFiltros, onBuscar }) {
  const [mostrarMas, setMostrarMas] = useState(false);
  const [collapsed, setCollapsed] = useState(false);

  const handleChange = (campo) => (e) => {
    setFiltros((prev) => ({ ...prev, [campo]: e.target.value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onBuscar(0);
  };

  return (
    <form className={`search-filters${collapsed ? ' filters-collapsed' : ''}`} onSubmit={handleSubmit}>
      <div className="filters-mobile-header">
        <button type="button" className="btn-collapse" onClick={() => setCollapsed(!collapsed)}>
          {collapsed ? 'Mostrar filtros ▼' : 'Ocultar filtros ▲'}
        </button>
      </div>

      <div className={`filters-content${collapsed ? ' hidden' : ''}`}>
        <div className="filters-row">
          <div className="filter-group filter-texto">
            <label htmlFor="texto">Búsqueda</label>
            <input
              id="texto"
              type="text"
              placeholder="Título, licitador, nº expediente o CPV..."
              value={filtros.texto}
              onChange={handleChange('texto')}
            />
          </div>

          <div className="filter-group">
            <label htmlFor="importeMin">Importe mín. (€)</label>
            <input
              id="importeMin"
              type="number"
              placeholder="0"
              min="0"
              value={filtros.importeMin}
              onChange={handleChange('importeMin')}
            />
          </div>

          <div className="filter-group">
            <label htmlFor="importeMax">Importe máx. (€)</label>
            <input
              id="importeMax"
              type="number"
              placeholder="Sin límite"
              min="0"
              value={filtros.importeMax}
              onChange={handleChange('importeMax')}
            />
          </div>

          <div className="filter-group">
            <label htmlFor="estado">Estado</label>
            <select id="estado" value={filtros.estado} onChange={handleChange('estado')}>
              {ESTADOS.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label htmlFor="tipoContrato">Tipo contrato</label>
            <select id="tipoContrato" value={filtros.tipoContrato} onChange={handleChange('tipoContrato')}>
              {TIPOS_CONTRATO.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>

          <div className="filter-group filter-cpv-group">
            <label htmlFor="cpv">CPV</label>
            <div className="cpv-input-row">
              <input
                id="cpv"
                type="text"
                placeholder="72 (servicios IT)"
                value={filtros.cpv}
                onChange={handleChange('cpv')}
              />
              <label className="cpv-exclusivo-label" title="Mostrar solo licitaciones cuyos CPV empiecen todos por este prefijo">
                <input
                  type="checkbox"
                  checked={!!filtros.cpvExclusivo}
                  onChange={(e) => setFiltros((prev) => ({ ...prev, cpvExclusivo: e.target.checked }))}
                />
                Solo
              </label>
            </div>
          </div>
        </div>

        {mostrarMas && (
          <div className="filters-row filters-extra">
            <div className="filter-group">
              <label htmlFor="nutsCode">Comunidad Autónoma</label>
              <select id="nutsCode" value={filtros.nutsCode} onChange={handleChange('nutsCode')}>
                {CCAA.map((o) => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>

            <div className="filter-group">
              <label htmlFor="procedimiento">Procedimiento</label>
              <select id="procedimiento" value={filtros.procedimiento} onChange={handleChange('procedimiento')}>
                {PROCEDIMIENTOS.map((o) => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>

            <div className="filter-group">
              <label htmlFor="diasPlazo">Plazo</label>
              <select id="diasPlazo" value={filtros.diasPlazo} onChange={handleChange('diasPlazo')}>
                {PLAZOS.map((o) => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>
          </div>
        )}

        <div className="filters-actions">
          <button type="button" className="btn-toggle" onClick={() => setMostrarMas(!mostrarMas)}>
            {mostrarMas ? 'Ver menos filtros' : 'Ver más filtros'}
          </button>
          <button type="submit" className="btn-buscar">Filtrar cargadas</button>
        </div>
      </div>
    </form>
  );
}

export default SearchFilters;
