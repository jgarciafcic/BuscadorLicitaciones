import { useState, useCallback } from 'react';
import { useLicitaciones } from './hooks/useLicitaciones';
import { ejecutarIngesta } from './services/api';
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
  const [nextFeedUrl, setNextFeedUrl] = useState(null);
  const [paginasCargadas, setPaginasCargadas] = useState(0);

  const handleCantabria = useCallback(() => {
    const cantabriaFiltros = {
      ...filtros,
      estado: 'PUB',
      tipoContrato: '2',
      cpv: '72',
      nutsCode: 'ES13',
    };
    setFiltros(cantabriaFiltros);
    buscar(0, null, cantabriaFiltros);
  }, [filtros, setFiltros, buscar]);

  const handleIngesta = useCallback(async (fromUrl = null) => {
    setIngesting(true);
    const isLoadMore = fromUrl !== null;
    setToast({ message: isLoadMore ? 'Cargando siguiente página del feed...' : 'Cargando primera página del feed...', type: 'info' });
    try {
      const resumen = await ejecutarIngesta(1, fromUrl);
      const newTotal = isLoadMore ? paginasCargadas + resumen.paginasProcesadas : resumen.paginasProcesadas;
      setPaginasCargadas(newTotal);
      setNextFeedUrl(resumen.nextPageUrl || null);
      setToast({
        message: `Página ${newTotal} cargada: ${resumen.nuevas} nuevas, ${resumen.actualizadas} actualizadas` +
          (resumen.nextPageUrl ? '' : ' (no hay más páginas)'),
        type: 'success',
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
          <button className="btn-cantabria" onClick={handleCantabria}>Licitaciones Cantabria</button>
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
        {(loading || ingesting) && <LoadingSpinner />}
      </main>
      <LicitacionDetail licitacion={seleccionada} onClose={() => setSeleccionada(null)} />
      {showManual && <UserManual onClose={() => setShowManual(false)} />}
      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </div>
  );
}

export default App;
