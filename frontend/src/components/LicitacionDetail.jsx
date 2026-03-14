import { useEffect, useState, useRef, useCallback } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { analizarPliegos, checkApiKeyConfigured } from '../services/api';
import ConfirmModal from './ConfirmModal';
import './LicitacionDetail.css';

const TIPOS_CONTRATO = {
  '1': 'Obras', '2': 'Servicios', '3': 'Suministros',
  '7': 'Concesión de obras', '8': 'Concesión de servicios',
  '21': 'Privado', '31': 'Administrativo especial', '40': 'Patrimonial', '50': 'Otros',
};

const PROCEDIMIENTOS = {
  '1': 'Abierto', '2': 'Restringido', '3': 'Negociado con publicidad',
  '4': 'Negociado sin publicidad', '5': 'Diálogo competitivo',
  '6': 'Contrato menor', '100': 'Simplificado',
};

const ESTADOS = {
  'PRE': 'Anuncio previo', 'PUB': 'En plazo', 'EV': 'Pendiente adjudicación',
  'ADJ': 'Adjudicada', 'RES': 'Resuelta', 'ANU': 'Anulada',
};

const URGENCIAS = { '1': 'Ordinaria', '2': 'Urgente' };

const UNIDADES_DURACION = { 'DAY': 'días', 'MON': 'meses', 'ANN': 'años' };

function formatDuracion(medida, unidad, inicio, fin) {
  const partes = [];
  if (medida && unidad) {
    partes.push(`${medida} ${UNIDADES_DURACION[unidad] || unidad}`);
  }
  if (inicio || fin) {
    const rango = [inicio ? formatFecha(inicio) : '?', fin ? formatFecha(fin) : '?'].join(' — ');
    partes.push(`(${rango})`);
  }
  return partes.length > 0 ? partes.join(' ') : null;
}

function parseCriterios(json) {
  if (!json) return null;
  try { return JSON.parse(json); } catch { return null; }
}

function parseLotes(json) {
  if (!json) return null;
  try { return JSON.parse(json); } catch { return null; }
}

function formatImporte(valor) {
  if (valor == null) return '—';
  return new Intl.NumberFormat('es-ES', {
    style: 'currency', currency: 'EUR', minimumFractionDigits: 2,
  }).format(valor);
}

function formatFecha(fecha) {
  if (!fecha) return '—';
  const partes = fecha.split('-');
  return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

function formatFechaHora(fecha, hora) {
  if (!fecha) return '—';
  const f = formatFecha(fecha);
  return hora ? `${f} ${hora.substring(0, 5)}` : f;
}

function LicitacionDetail({ licitacion, onClose }) {
  const [analisis, setAnalisis] = useState(null);
  const [analizando, setAnalizando] = useState(false);
  const [analisisError, setAnalisisError] = useState(null);
  const [confirmClose, setConfirmClose] = useState(false);
  const [needsApiKey, setNeedsApiKey] = useState(null); // null=not checked, true/false
  const [manualApiKey, setManualApiKey] = useState('');
  const [progressMsg, setProgressMsg] = useState(0);
  const analisisRef = useRef(null);

  const PROGRESS_MESSAGES = [
    'Descargando pliegos desde PLACSP...',
    'Extrayendo texto de los documentos PDF...',
    'Enviando texto al modelo de IA para análisis...',
    'Analizando criterios de adjudicación y solvencia...',
    'Extrayendo tecnologías y requisitos del equipo...',
    'Estructurando el resumen... casi listo',
  ];

  useEffect(() => {
    if (!analizando) return;
    setProgressMsg(0);
    const interval = setInterval(() => {
      setProgressMsg(prev => prev < PROGRESS_MESSAGES.length - 1 ? prev + 1 : prev);
    }, 8000);
    return () => clearInterval(interval);
  }, [analizando]);

  const handleClose = useCallback(() => {
    if (analizando) {
      setConfirmClose(true);
      return;
    }
    onClose();
  }, [analizando, onClose]);

  const handleConfirmClose = useCallback(() => {
    setConfirmClose(false);
    onClose();
  }, [onClose]);

  useEffect(() => {
    const handleKey = (e) => {
      if (e.key === 'Escape' && !confirmClose) handleClose();
    };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [handleClose, confirmClose]);

  useEffect(() => {
    setAnalisis(null);
    setAnalisisError(null);
    setAnalizando(false);
    setNeedsApiKey(null);
    setManualApiKey('');
  }, [licitacion?.entryId]);

  if (!licitacion) return null;

  const lic = licitacion;
  const tienePliegos = lic.urlPcap || lic.urlPpt;

  const handleAnalizar = async (apiKeyOverride) => {
    if (needsApiKey === null) {
      // First click: check if server has API key configured
      try {
        const { configured } = await checkApiKeyConfigured();
        if (!configured) {
          setNeedsApiKey(true);
          return;
        }
      } catch {
        // If check fails, try to analyze anyway
      }
    }

    const keyToUse = apiKeyOverride || manualApiKey || undefined;
    if (needsApiKey && !keyToUse) {
      setAnalisisError('Introduce una API key para continuar.');
      return;
    }

    setAnalizando(true);
    setAnalisisError(null);
    try {
      const result = await analizarPliegos(lic.entryId, keyToUse);
      setAnalisis(result);
      setManualApiKey('');
      setTimeout(() => {
        analisisRef.current?.scrollIntoView({ behavior: 'smooth' });
      }, 100);
    } catch (err) {
      setAnalisisError(err.message);
    } finally {
      setAnalizando(false);
    }
  };

  const handleExportPdf = async () => {
    const element = analisisRef.current;
    if (!element) return;
    const html2pdf = (await import('html2pdf.js')).default;
    const title = lic.expediente || 'analisis-pliegos';
    html2pdf()
      .set({
        margin: [10, 10, 10, 10],
        filename: `${title}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' },
      })
      .from(element)
      .save();
  };

  return (
    <>
      <div className="drawer-overlay" onClick={handleClose} />
      {confirmClose && (
        <ConfirmModal
          title="Análisis en curso"
          message="El análisis de pliegos con IA está en curso. Si cierra la ventana, se detendrá el proceso y perderá el resultado parcial. ¿Desea cerrar de todos modos?"
          confirmLabel="Cerrar y detener"
          cancelLabel="Continuar análisis"
          danger
          onConfirm={handleConfirmClose}
          onCancel={() => setConfirmClose(false)}
        />
      )}
      <aside className="drawer">
        <div className="drawer-header">
          <h2>Detalle de licitación</h2>
          <button className="drawer-close" onClick={handleClose}>&#x2715;</button>
        </div>

        <div className="drawer-body">
          <dl className="detail-grid">
            <dt>Expediente</dt>
            <dd>{lic.expediente || '—'}</dd>

            <dt>Estado</dt>
            <dd>{ESTADOS[lic.estado] || lic.estado} ({lic.estado})</dd>

            <dt>Objeto</dt>
            <dd className="detail-objeto">{lic.objeto || '—'}</dd>

            <dt>Órgano de contratación</dt>
            <dd>{lic.organoContratacion || '—'}</dd>

            <dt>ID Órgano</dt>
            <dd>{lic.organoId || '—'}</dd>

            <dt>Tipo de contrato</dt>
            <dd>{TIPOS_CONTRATO[lic.tipoContrato] || lic.tipoContrato || '—'}</dd>

            <dt>Importe sin impuestos</dt>
            <dd className="detail-importe">{formatImporte(lic.importeSinImpuestos)}</dd>

            <dt>Importe con impuestos</dt>
            <dd className="detail-importe">{formatImporte(lic.importeConImpuestos)}</dd>

            <dt>Códigos CPV</dt>
            <dd>{lic.cpvCodes || '—'}</dd>

            <dt>Fecha límite de ofertas</dt>
            <dd>{formatFechaHora(lic.fechaLimiteOfertas, lic.horaLimiteOfertas)}</dd>

            <dt>Tipo de procedimiento</dt>
            <dd>{PROCEDIMIENTOS[lic.tipoProcedimiento] || lic.tipoProcedimiento || '—'}</dd>

            <dt>Lugar de ejecución</dt>
            <dd>{lic.lugarEjecucion || '—'}{lic.nutsCode ? ` (${lic.nutsCode})` : ''}</dd>

            <dt>Fecha actualización</dt>
            <dd>{lic.fechaActualizacion ? formatFecha(lic.fechaActualizacion.substring(0, 10)) : '—'}</dd>

            <dt>Pliegos</dt>
            <dd className="detail-pliegos">
              {lic.urlPcap && <a href={lic.urlPcap} target="_blank" rel="noopener noreferrer">PCAP</a>}
              {lic.urlPcap && lic.urlPpt && ' | '}
              {lic.urlPpt && <a href={lic.urlPpt} target="_blank" rel="noopener noreferrer">PPT</a>}
              {!tienePliegos && '—'}
            </dd>

            {formatDuracion(lic.duracionMedida, lic.duracionUnidad, lic.duracionInicio, lic.duracionFin) && <>
              <dt>Duración</dt>
              <dd>{formatDuracion(lic.duracionMedida, lic.duracionUnidad, lic.duracionInicio, lic.duracionFin)}</dd>
            </>}

            {lic.prorroga && <>
              <dt>Prórroga</dt>
              <dd className="detail-prorroga">{lic.prorroga}</dd>
            </>}

            {lic.urgencia && <>
              <dt>Urgencia</dt>
              <dd>{lic.urgencia === '2'
                ? <span className="badge-urgente">Urgente</span>
                : URGENCIAS[lic.urgencia] || lic.urgencia}</dd>
            </>}
          </dl>

          {/* Criterios de adjudicación */}
          {(() => {
            const criterios = parseCriterios(lic.criteriosAdjudicacion);
            if (!criterios || criterios.length === 0) return null;
            return (
              <div className="detail-section">
                <h3>Criterios de adjudicación</h3>
                <table className="detail-table">
                  <thead>
                    <tr><th>Tipo</th><th>Descripción</th><th>Peso</th></tr>
                  </thead>
                  <tbody>
                    {criterios.map((c, i) => (
                      <tr key={i}>
                        <td>{c.tipo === 'OBJ' ? 'Objetivo' : c.tipo === 'SUBJ' ? 'Subjetivo' : c.tipo}</td>
                        <td>{c.descripcion}</td>
                        <td className="detail-peso">{c.peso != null ? `${c.peso}%` : '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            );
          })()}

          {/* Lotes */}
          {(() => {
            const lotes = parseLotes(lic.lotes);
            if (!lotes || lotes.length === 0) return null;
            return (
              <div className="detail-section">
                <h3>Lotes ({lotes.length})</h3>
                <table className="detail-table">
                  <thead>
                    <tr><th>Lote</th><th>Objeto</th><th>Importe</th></tr>
                  </thead>
                  <tbody>
                    {lotes.map((l, i) => (
                      <tr key={i}>
                        <td>{l.id || i + 1}</td>
                        <td>{l.objeto || '—'}</td>
                        <td className="detail-importe">{l.importe != null ? formatImporte(l.importe) : '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            );
          })()}

          {/* Solvencia */}
          {(lic.solvenciaTecnica || lic.solvenciaEconomica) && (
            <div className="detail-section">
              <h3>Requisitos de solvencia</h3>
              {lic.solvenciaTecnica && (
                <div className="detail-solvencia">
                  <strong>Técnica:</strong> {lic.solvenciaTecnica}
                </div>
              )}
              {lic.solvenciaEconomica && (
                <div className="detail-solvencia">
                  <strong>Económica:</strong> {lic.solvenciaEconomica}
                </div>
              )}
            </div>
          )}

          {/* Botón de análisis */}
          {tienePliegos && !analisis && !analizando && (
            <div className="analisis-section">
              {needsApiKey && (
                <div className="apikey-prompt">
                  <p className="apikey-message">
                    Para usar el análisis con IA es necesario introducir una API key de Claude (Anthropic).
                    La key <strong>no se almacenará</strong> y solo se usará para este análisis en particular.
                  </p>
                  <input
                    type="password"
                    className="apikey-input"
                    placeholder="sk-ant-..."
                    value={manualApiKey}
                    onChange={(e) => setManualApiKey(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && manualApiKey && handleAnalizar(manualApiKey)}
                  />
                </div>
              )}
              <button
                className="btn-analizar"
                onClick={() => handleAnalizar()}
                disabled={needsApiKey && !manualApiKey}
              >
                Analizar pliegos con IA
              </button>
              {analisisError && <div className="analisis-error">Error: {analisisError}</div>}
            </div>
          )}

          {/* Spinner mientras analiza */}
          {analizando && (
            <div className="analisis-loading">
              <div className="analisis-loading-inner">
                <div className="spinner-large" />
                <div className="analisis-loading-text">
                  <strong>Analizando pliegos con IA...</strong>
                  <span className="progress-message">{PROGRESS_MESSAGES[progressMsg]}</span>
                </div>
              </div>
              <div className="analisis-loading-bar">
                <div className="analisis-loading-bar-fill" />
              </div>
            </div>
          )}

          {/* Resultado del análisis */}
          {analisis && (
            <div className="analisis-result" ref={analisisRef}>
              <div className="analisis-result-header">
                <div className="analisis-badge">
                  {analisis.fromCache ? 'Resultado en caché' : 'Analizado con IA'}
                </div>
                <button className="btn-export-pdf" onClick={handleExportPdf}>
                  Exportar PDF
                </button>
              </div>

              {analisis.resumenPcap && (
                <div className="analisis-document">
                  <h2 className="analisis-doc-title">PCAP — Cláusulas Administrativas</h2>
                  <div className="markdown-body">
                    <ReactMarkdown remarkPlugins={[remarkGfm]}>
                      {analisis.resumenPcap}
                    </ReactMarkdown>
                  </div>
                </div>
              )}

              {analisis.resumenPpt && (
                <div className="analisis-document">
                  <h2 className="analisis-doc-title">PPT — Prescripciones Técnicas</h2>
                  <div className="markdown-body">
                    <ReactMarkdown remarkPlugins={[remarkGfm]}>
                      {analisis.resumenPpt}
                    </ReactMarkdown>
                  </div>
                </div>
              )}

              {!analisis.resumenPcap && !analisis.resumenPpt && (
                <p className="analisis-empty">No se encontraron documentos para analizar.</p>
              )}
            </div>
          )}
        </div>

        <div className="drawer-footer">
          {lic.enlacePlacsp && (
            <a className="btn-placsp" href={lic.enlacePlacsp} target="_blank" rel="noopener noreferrer">
              Ver en PLACSP
            </a>
          )}
        </div>
      </aside>
    </>
  );
}

export default LicitacionDetail;
