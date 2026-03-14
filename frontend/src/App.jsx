import { useState, useCallback, useEffect, useRef } from 'react';
import { useLicitaciones } from './hooks/useLicitaciones';
import { ejecutarIngesta, fetchIngestaEstado } from './services/api';
import SearchFilters from './components/SearchFilters';
import ResultsSummary from './components/ResultsSummary';
import ResultsTable from './components/ResultsTable';
import LicitacionDetail from './components/LicitacionDetail';
import LoadingSpinner from './components/LoadingSpinner';
import UserManual from './components/UserManual';
import Toast from './components/Toast';
import './App.css';

function App() {
  const { filtros, setFiltros, resultados, stats, loading, error, buscar, sort, cambiarSort } = useLicitaciones();
  const [seleccionada, setSeleccionada] = useState(null);
  const [showManual, setShowManual] = useState(false);
  const [toast, setToast] = useState(null);
  const [ingesting, setIngesting] = useState(false);
  const [initialIngesting, setInitialIngesting] = useState(false);
  const [nextFeedUrl, setNextFeedUrl] = useState(null);
  const [paginasCargadas, setPaginasCargadas] = useState(0);
  const [ingestaProgreso, setIngestaProgreso] = useState(null);
  const pollingRef = useRef(null);
  const progresoRef = useRef(null);

  useEffect(() => {
    let cancelled = false;
    const checkEstado = async () => {
      try {
        const estado = await fetchIngestaEstado();
        if (cancelled) return;
        if (estado.ingesting) {
          setInitialIngesting(true);
          pollingRef.current = setTimeout(checkEstado, 3000);
        } else if (initialIngesting) {
          setInitialIngesting(false);
          buscar(0);
        }
      } catch {
        // backend not ready yet, retry
        if (!cancelled) pollingRef.current = setTimeout(checkEstado, 3000);
      }
    };
    checkEstado();
    return () => { cancelled = true; clearTimeout(pollingRef.current); };
  }, [initialIngesting, buscar]);

  // Polling de progreso durante ingesta
  useEffect(() => {
    if (!ingesting && !initialIngesting) {
      setIngestaProgreso(null);
      return;
    }
    const poll = async () => {
      try {
        const estado = await fetchIngestaEstado();
        if (estado.progreso) setIngestaProgreso(estado.progreso);
      } catch { /* ignore */ }
    };
    poll();
    progresoRef.current = setInterval(poll, 2000);
    return () => clearInterval(progresoRef.current);
  }, [ingesting, initialIngesting]);

  const isCantabriaActive = filtros.nutsCode === 'ES13';

  const handleCantabria = useCallback(() => {
    if (isCantabriaActive) {
      const sinCantabria = { ...filtros, nutsCode: '' };
      setFiltros(sinCantabria);
      buscar(0, null, sinCantabria);
    } else {
      const cantabriaFiltros = {
        ...filtros,
        estado: 'PUB',
        tipoContrato: '2',
        cpv: '72',
        nutsCode: 'ES13',
      };
      setFiltros(cantabriaFiltros);
      buscar(0, null, cantabriaFiltros);
    }
  }, [filtros, isCantabriaActive, setFiltros, buscar]);

  const handleIngesta = useCallback(async (fromUrl = null) => {
    setIngesting(true);
    const isLoadMore = fromUrl !== null;
    setToast({ message: isLoadMore ? 'Cargando siguientes páginas del feed...' : 'Cargando páginas del feed...', type: 'info' });
    try {
      const resumen = await ejecutarIngesta(5, fromUrl);
      const newTotal = isLoadMore ? paginasCargadas + resumen.paginasProcesadas : resumen.paginasProcesadas;
      setPaginasCargadas(newTotal);
      setNextFeedUrl(resumen.nextPageUrl || null);
      const solapaMsg = resumen.solapaConBbdd
        ? ` — Alcanzadas licitaciones ya cargadas (${resumen.yaExistentes} sin cambios)`
        : ' — No se alcanzaron licitaciones previas, puede haber huecos';
      setToast({
        message: `${resumen.paginasProcesadas} págs. cargadas: ${resumen.nuevas} nuevas, ${resumen.actualizadas} actualizadas${solapaMsg}` +
          (resumen.nextPageUrl ? '' : ' (no hay más páginas)'),
        type: resumen.solapaConBbdd ? 'success' : 'warning',
      });
      buscar(0);
    } catch (err) {
      setToast({ message: `Error en ingesta: ${err.message}`, type: 'error' });
    } finally {
      setIngesting(false);
    }
  }, [buscar, paginasCargadas]);

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-brand">
          <img src="/logo.svg" alt="PLACSP Monitor" className="header-logo" />
          <h1>PLACSP Monitor — Buscador de Licitaciones</h1>
        </div>
        <div className="header-actions">
          <button className="btn-manual" onClick={() => setShowManual(true)} title="Manual de uso">?</button>
          <button className={`btn-cantabria${isCantabriaActive ? ' active' : ''}`} onClick={handleCantabria}>
            {isCantabriaActive ? 'Cantabria ✕' : 'Licitaciones Cantabria'}
          </button>
          <button className="btn-ingesta" onClick={() => { setNextFeedUrl(null); setPaginasCargadas(0); handleIngesta(null); }} disabled={ingesting}>
            {ingesting ? 'Cargando...' : 'Cargar de origen'}
          </button>
          {nextFeedUrl && (
            <button className="btn-ingesta btn-load-more" onClick={() => handleIngesta(nextFeedUrl)} disabled={ingesting}>
              {ingesting ? 'Cargando...' : 'Cargar +'}
            </button>
          )}
        </div>
      </header>
      <main style={{ position: 'relative', minHeight: '400px' }}>
        <SearchFilters filtros={filtros} setFiltros={setFiltros} onBuscar={buscar} />
        <ResultsSummary stats={stats} loading={loading} filtros={filtros} />
        {error && <div className="error-msg">Error: {error}</div>}
        <ResultsTable resultados={resultados} loading={loading} onPageChange={buscar} sort={sort} onSortChange={cambiarSort} onRowClick={setSeleccionada} cpvPrefix={filtros.cpv} />
        {(loading || ingesting || initialIngesting) && <LoadingSpinner progreso={ingestaProgreso} />}
      </main>
      <LicitacionDetail licitacion={seleccionada} onClose={() => setSeleccionada(null)} />
      {showManual && <UserManual onClose={() => setShowManual(false)} />}
      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </div>
  );
}

export default App;
