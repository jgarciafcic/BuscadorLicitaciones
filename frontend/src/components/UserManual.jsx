import { useEffect } from 'react';
import './UserManual.css';

function UserManual({ onClose }) {
  useEffect(() => {
    const handleKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [onClose]);

  return (
    <>
      <div className="manual-overlay" onClick={onClose} />
      <div className="manual-modal">
        <div className="manual-header">
          <h2>Manual de uso</h2>
          <button className="manual-close" onClick={onClose}>&#x2715;</button>
        </div>
        <div className="manual-body">

          <section>
            <h3>1. Carga de datos (Ingesta del feed)</h3>
            <p>
              La aplicación obtiene las licitaciones del feed ATOM de la
              <strong> Plataforma de Contrataci&oacute;n del Sector P&uacute;blico (PLACSP)</strong>.
            </p>
            <ul>
              <li>
                <strong>Cargar feed</strong> &mdash; Descarga la primera p&aacute;gina del feed
                (las licitaciones m&aacute;s recientes, hasta 500 por p&aacute;gina).
              </li>
              <li>
                <strong>Cargar m&aacute;s</strong> &mdash; Tras la primera carga, aparece este
                bot&oacute;n para seguir descargando p&aacute;ginas anteriores e ir ampliando
                la base de datos.
              </li>
            </ul>
            <p>
              Las licitaciones se almacenan localmente. Si una licitaci&oacute;n ya existe, se actualiza
              con los datos m&aacute;s recientes del feed.
            </p>
          </section>

          <section>
            <h3>2. Filtros de b&uacute;squeda</h3>
            <p>El panel de filtros permite acotar las licitaciones mostradas:</p>
            <table className="manual-table">
              <thead>
                <tr><th>Filtro</th><th>Descripci&oacute;n</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td><strong>B&uacute;squeda</strong></td>
                  <td>Texto libre que busca en el t&iacute;tulo, &oacute;rgano de contrataci&oacute;n y n&uacute;mero de expediente.</td>
                </tr>
                <tr>
                  <td><strong>Importe m&iacute;n. / m&aacute;x.</strong></td>
                  <td>Rango de presupuesto base (sin impuestos) en euros.</td>
                </tr>
                <tr>
                  <td><strong>Estado</strong></td>
                  <td>
                    Estado de la licitaci&oacute;n: <em>En plazo (PUB)</em>, <em>Adjudicada (ADJ)</em>,
                    <em> Resuelta (RES)</em>, <em>Pendiente adjudicaci&oacute;n (EV)</em>,
                    <em> Anuncio previo (PRE)</em>, <em>Anulada (ANU)</em> o <em>Todos</em>.
                  </td>
                </tr>
                <tr>
                  <td><strong>Tipo contrato</strong></td>
                  <td>Servicios, Obras, Suministros, Concesiones, etc.</td>
                </tr>
                <tr>
                  <td><strong>CPV</strong></td>
                  <td>
                    Prefijo del c&oacute;digo CPV (Vocabulario Com&uacute;n de Contrataci&oacute;n P&uacute;blica).
                    Por defecto <code>72</code> (servicios TI). Puede introducir cualquier prefijo (p.&nbsp;ej.
                    <code>48</code> para paquetes de software).
                  </td>
                </tr>
                <tr>
                  <td><strong>Solo (CPV)</strong></td>
                  <td>
                    Checkbox junto al campo CPV. Cuando est&aacute; marcado, muestra
                    &uacute;nicamente licitaciones cuyos <strong>todos</strong> los c&oacute;digos CPV
                    empiecen por el prefijo indicado. Sin marcar, basta con que al menos uno coincida.
                  </td>
                </tr>
              </tbody>
            </table>
            <p>
              Pulsando <strong>&laquo;Ver m&aacute;s filtros&raquo;</strong> se despliegan filtros adicionales:
            </p>
            <table className="manual-table">
              <thead>
                <tr><th>Filtro</th><th>Descripci&oacute;n</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td><strong>Comunidad Aut&oacute;noma</strong></td>
                  <td>Filtra por lugar de ejecuci&oacute;n (c&oacute;digo NUTS).</td>
                </tr>
                <tr>
                  <td><strong>Procedimiento</strong></td>
                  <td>Tipo de procedimiento: Abierto, Restringido, Simplificado, etc.</td>
                </tr>
                <tr>
                  <td><strong>Plazo</strong></td>
                  <td>Filtra por proximidad de la fecha l&iacute;mite de ofertas: en plazo, menos de 7, 15 o 30 d&iacute;as.</td>
                </tr>
              </tbody>
            </table>
            <p>Pulse <strong>&laquo;Buscar licitaciones&raquo;</strong> para aplicar los filtros.</p>
          </section>

          <section>
            <h3>3. Tabla de resultados</h3>
            <p>La tabla muestra las licitaciones que cumplen los filtros:</p>
            <ul>
              <li><strong>Fecha pub.</strong> &mdash; Fecha de la &uacute;ltima actualizaci&oacute;n en el feed. Se puede ordenar pulsando en la cabecera.</li>
              <li><strong>L&iacute;mite ofertas</strong> &mdash; Fecha y hora l&iacute;mite de presentaci&oacute;n. En <span style={{color:'#d32f2f',fontWeight:700}}>rojo</span> si quedan menos de 7 d&iacute;as, en <span style={{color:'#e65100',fontWeight:600}}>naranja</span> si quedan menos de 15.</li>
              <li><strong>T&iacute;tulo</strong> &mdash; &Oacute;rgano de contrataci&oacute;n (en gris) y objeto del contrato (enlace a PLACSP).</li>
              <li><strong>CPV</strong> &mdash; C&oacute;digos CPV de la licitaci&oacute;n. Al pasar el rat&oacute;n sobre cada c&oacute;digo se muestra su descripci&oacute;n. Los que coinciden con el filtro activo se resaltan en azul.</li>
              <li><strong>CCAA</strong> &mdash; Comunidad Aut&oacute;noma del lugar de ejecuci&oacute;n.</li>
              <li><strong>Pliegos</strong> &mdash; Enlaces a los documentos PCAP (cl&aacute;usulas administrativas) y PPT (prescripciones t&eacute;cnicas) cuando est&aacute;n disponibles.</li>
              <li><strong>Importe</strong> &mdash; Presupuesto base sin impuestos. Se puede ordenar pulsando en la cabecera.</li>
            </ul>
            <p>La paginaci&oacute;n se realiza del lado servidor (20 resultados por p&aacute;gina).</p>
          </section>

          <section>
            <h3>4. Detalle de licitaci&oacute;n</h3>
            <p>
              Al hacer clic en cualquier fila se abre un panel lateral con todos los
              datos de la licitaci&oacute;n: expediente, estado, objeto, &oacute;rgano,
              importes, CPV, plazos, procedimiento, lugar de ejecuci&oacute;n y enlaces.
            </p>
            <p>
              En la parte inferior hay un enlace directo a la ficha completa en la PLACSP.
            </p>
          </section>

          <section>
            <h3>5. An&aacute;lisis de pliegos con IA</h3>
            <p>
              Cuando una licitaci&oacute;n tiene pliegos (PCAP y/o PPT), el panel de detalle
              muestra el bot&oacute;n <strong>&laquo;Analizar pliegos con IA&raquo;</strong>.
            </p>
            <ul>
              <li>El proceso descarga los PDFs, extrae el texto y genera un resumen estructurado.</li>
              <li>Puede tardar entre 15 y 60 segundos dependiendo del tama&ntilde;o de los documentos.</li>
              <li>El resultado se muestra como documento formateado con secciones: objeto, requisitos, criterios de adjudicaci&oacute;n, plazos, etc.</li>
              <li>Se puede <strong>exportar a PDF</strong> para archivarlo o compartirlo.</li>
              <li>Los an&aacute;lisis se cachean: si ya se ha analizado una licitaci&oacute;n, el resultado se recupera instant&aacute;neamente.</li>
            </ul>
            <p>
              <strong>Importante:</strong> si cierra el panel mientras el an&aacute;lisis est&aacute; en curso,
              se le pedir&aacute; confirmaci&oacute;n para detener el proceso.
            </p>
          </section>

          <section>
            <h3>6. Atajo &laquo;Licitaciones Cantabria&raquo;</h3>
            <p>
              El bot&oacute;n <strong>Licitaciones Cantabria</strong> en la cabecera aplica
              autom&aacute;ticamente los filtros: Servicios TI (CPV 72), En plazo (PUB),
              lugar de ejecuci&oacute;n Cantabria (NUTS ES13).
            </p>
          </section>

          <section>
            <h3>7. Exportaci&oacute;n CSV</h3>
            <p>
              Junto al resumen de resultados se muestra un enlace
              <strong> &laquo;Exportar CSV&raquo;</strong> que descarga un fichero con todas las
              licitaciones que cumplen los filtros actuales (m&aacute;ximo 5.000), separado por
              punto y coma, compatible con Excel.
            </p>
          </section>

          <section>
            <h3>8. C&oacute;digos CPV</h3>
            <p>
              Los c&oacute;digos CPV (Common Procurement Vocabulary) clasifican el objeto de los
              contratos p&uacute;blicos seg&uacute;n el est&aacute;ndar europeo CPV 2007. Los
              principales c&oacute;digos de inter&eacute;s para TI son:
            </p>
            <table className="manual-table">
              <thead><tr><th>CPV</th><th>Descripci&oacute;n</th></tr></thead>
              <tbody>
                <tr><td><code>72</code></td><td>Servicios TI: consultor&iacute;a, desarrollo de software, Internet y apoyo</td></tr>
                <tr><td><code>48</code></td><td>Paquetes de software y sistemas de informaci&oacute;n</td></tr>
                <tr><td><code>30</code></td><td>M&aacute;quinas, equipo y art&iacute;culos de oficina e inform&aacute;tica</td></tr>
                <tr><td><code>32</code></td><td>Equipos de radio, televisi&oacute;n, comunicaciones y telecomunicaciones</td></tr>
                <tr><td><code>64</code></td><td>Servicios de correos y telecomunicaciones</td></tr>
              </tbody>
            </table>
            <p>
              En la columna CPV de la tabla, pase el rat&oacute;n sobre cualquier c&oacute;digo
              para ver su descripci&oacute;n completa.
            </p>
          </section>

        </div>
      </div>
    </>
  );
}

export default UserManual;
