// En producción (Netlify), las llamadas largas van directo a Railway para evitar
// el timeout de 26s del proxy. En desarrollo, todo va al proxy de Vite.
const BACKEND_DIRECT = import.meta.env.PROD
  ? 'https://meticulous-prosperity-production.up.railway.app'
  : '';

const DEFAULT_FILTERS = {
  estado: 'PUB',
  tipoContrato: '2',
  cpv: '72',
};

function buildParams(filtros = {}, page, size, sort) {
  const merged = { ...DEFAULT_FILTERS, ...filtros };
  const params = new URLSearchParams();

  for (const [key, value] of Object.entries(merged)) {
    if (value === null || value === undefined || value === '') continue;
    // cpvExclusivo: solo enviar si es true
    if (key === 'cpvExclusivo') {
      if (value === true || value === 'true') params.append(key, 'true');
      continue;
    }
    params.append(key, value);
  }

  if (page !== undefined) params.append('page', page);
  if (size !== undefined) params.append('size', size);
  if (sort) params.append('sort', sort);

  return params.toString();
}

export async function fetchLicitaciones(filtros = {}, page = 0, size = 20, sort = 'fechaLimiteOfertas,asc') {
  const query = buildParams(filtros, page, size, sort);
  const response = await fetch(`/api/licitaciones?${query}`);
  if (!response.ok) {
    throw new Error(`Error ${response.status}: ${response.statusText}`);
  }
  return response.json();
}

export async function fetchStats(filtros = {}) {
  const query = buildParams(filtros);
  const response = await fetch(`/api/licitaciones/stats?${query}`);
  if (!response.ok) {
    throw new Error(`Error ${response.status}: ${response.statusText}`);
  }
  return response.json();
}

export async function ejecutarIngesta(pages = 1, fromUrl = null) {
  const params = new URLSearchParams({ pages });
  if (fromUrl) params.append('fromUrl', fromUrl);
  const response = await fetch(`${BACKEND_DIRECT}/api/ingesta/ejecutar?${params}`, { method: 'POST' });
  if (!response.ok) {
    throw new Error(`Error ${response.status}: ${response.statusText}`);
  }
  return response.json();
}

export function getExportUrl(filtros = {}) {
  const query = buildParams(filtros);
  return `/api/licitaciones/export?${query}`;
}

export async function analizarPliegos(entryId) {
  const response = await fetch(`${BACKEND_DIRECT}/api/licitaciones/analizar-pliegos?entryId=${encodeURIComponent(entryId)}`, {
    method: 'POST',
  });
  if (!response.ok) {
    throw new Error(`Error ${response.status}: ${response.statusText}`);
  }
  return response.json();
}
