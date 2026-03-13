import { useState, useEffect, useCallback } from 'react';
import { fetchLicitaciones, fetchStats } from '../services/api';

const DEFAULT_FILTROS = {
  estado: 'PUB',
  tipoContrato: '2',
  cpv: '72',
  cpvExclusivo: false,
  texto: '',
  importeMin: '',
  importeMax: '',
  nutsCode: '',
  procedimiento: '',
  diasPlazo: '',
};

const DEFAULT_SORT = { field: 'fechaLimiteOfertas', direction: 'asc' };

export function useLicitaciones() {
  const [filtros, setFiltros] = useState({ ...DEFAULT_FILTROS });
  const [sort, setSort] = useState({ ...DEFAULT_SORT });
  const [resultados, setResultados] = useState(null);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const buscar = useCallback(async (page = 0, sortOverride, filtrosOverride) => {
    const currentSort = sortOverride || sort;
    const currentFiltros = filtrosOverride || filtros;
    const sortParam = `${currentSort.field},${currentSort.direction}`;
    setLoading(true);
    setError(null);
    try {
      const [pageData, statsData] = await Promise.all([
        fetchLicitaciones(currentFiltros, page, 20, sortParam),
        fetchStats(currentFiltros),
      ]);
      setResultados(pageData);
      setStats(statsData);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [filtros, sort]);

  const cambiarSort = useCallback((field) => {
    setSort((prev) => {
      const newSort = {
        field,
        direction: prev.field === field && prev.direction === 'asc' ? 'desc' : 'asc',
      };
      buscar(0, newSort);
      return newSort;
    });
  }, [buscar]);

  const resetFiltros = useCallback(() => {
    setFiltros({ ...DEFAULT_FILTROS });
  }, []);

  useEffect(() => {
    buscar(0);
  }, []);

  return { filtros, setFiltros, resultados, stats, loading, error, buscar, resetFiltros, sort, cambiarSort };
}
