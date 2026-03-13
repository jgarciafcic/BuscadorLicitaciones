// Diccionario CPV 2007 — Divisiones principales + detalle 72xxx (Servicios TI)
// Fuente: Reglamento (CE) nº 213/2008
const CPV_CODES = {
  // === Divisiones principales — textos oficiales PLACSP ===
  '03000000': 'Productos de la agricultura, ganadería, pesca, silvicultura y productos afines',
  '09000000': 'Derivados del petróleo, combustibles, electricidad y otras fuentes de energía',
  '14000000': 'Productos de la minería, de metales de base y productos afines',
  '15000000': 'Alimentos, bebidas, tabaco y productos afines',
  '16000000': 'Maquinaria agrícola',
  '18000000': 'Prendas de vestir, calzado, artículos de viaje y accesorios',
  '19000000': 'Piel y textiles, materiales de plástico y caucho',
  '22000000': 'Impresos y productos relacionados',
  '24000000': 'Productos químicos',
  '30000000': 'Máquinas, equipo y artículos de oficina y de informática, excepto mobiliario y paquetes de software',
  '31000000': 'Máquinas, aparatos, equipo y productos consumibles eléctricos; iluminación',
  '32000000': 'Equipos de radio, televisión, comunicaciones y telecomunicaciones y equipos conexos',
  '33000000': 'Equipamiento y artículos médicos, farmacéuticos y de higiene personal',
  '34000000': 'Equipos de transporte y productos auxiliares',
  '35000000': 'Equipo de seguridad, extinción de incendios, policía y defensa',
  '37000000': 'Instrumentos musicales, artículos deportivos, juegos, juguetes, artículos de artesanía, materiales artísticos y accesorios',
  '38000000': 'Equipo de laboratorio, óptico y de precisión (excepto gafas)',
  '39000000': 'Mobiliario (incluido el de oficina), complementos de mobiliario, aparatos electrodomésticos (excluida la iluminación) y productos de limpieza',
  '41000000': 'Agua recogida y depurada',
  '42000000': 'Maquinaria industrial',
  '43000000': 'Maquinaria para la minería y la explotación de canteras y equipo de construcción',
  '44000000': 'Estructuras y materiales de construcción; productos auxiliares para la construcción (excepto aparatos eléctricos)',
  '45000000': 'Trabajos de construcción',
  '48000000': 'Paquetes de software y sistemas de información',
  '50000000': 'Servicios de reparación y mantenimiento',
  '51000000': 'Servicios de instalación (excepto software)',
  '55000000': 'Servicios comerciales al por menor de hostelería y restauración',
  '60000000': 'Servicios de transporte (excluido el transporte de residuos)',
  '63000000': 'Servicios de transporte complementarios y auxiliares; servicios de agencias de viajes',
  '64000000': 'Servicios de correos y telecomunicaciones',
  '65000000': 'Servicios públicos',
  '66000000': 'Servicios financieros y de seguros',
  '70000000': 'Servicios inmobiliarios',
  '71000000': 'Servicios de arquitectura, construcción, ingeniería e inspección',
  '72000000': 'Servicios TI: consultoría, desarrollo de software, Internet y apoyo',
  '73000000': 'Servicios de investigación y desarrollo y servicios de consultoría conexos',
  '75000000': 'Servicios de administración pública, defensa y servicios de seguridad social',
  '76000000': 'Servicios relacionados con la industria del gas y del petróleo',
  '77000000': 'Servicios agrícolas, forestales, hortícolas, acuícolas y apícolas',
  '79000000': 'Servicios a empresas: legislación, mercadotecnia, asesoría, selección de personal, imprenta y seguridad',
  '80000000': 'Servicios de enseñanza y formación',
  '85000000': 'Servicios de salud y asistencia social',
  '90000000': 'Servicios de alcantarillado, basura, limpieza y medio ambiente',
  '92000000': 'Servicios de esparcimiento, culturales y deportivos',
  '98000000': 'Otros servicios comunitarios, sociales o personales',

  // === Detalle 30xxx — Equipo informático ===
  '30200000': 'Equipo y material informático',
  '30210000': 'Máquinas de tratamiento automático de datos',
  '30213000': 'Ordenadores personales',
  '30213100': 'Ordenadores portátiles',
  '30213200': 'Tabletas',
  '30213300': 'Ordenadores de sobremesa',
  '30230000': 'Equipo informático',
  '30231000': 'Pantallas y consolas de ordenador',
  '30232000': 'Periféricos',
  '30233000': 'Dispositivos de almacenamiento y lectura',
  '30236000': 'Equipo informático diverso',

  // === Detalle 48xxx — Software ===
  '48100000': 'Paquetes de software específicos para la industria',
  '48200000': 'Paquetes de software para redes, Internet e intranet',
  '48300000': 'Software para documentos, dibujo, imágenes y productividad',
  '48400000': 'Software de transacciones comerciales y personales',
  '48440000': 'Software para análisis financiero y contabilidad',
  '48445000': 'Paquetes de software CRM',
  '48450000': 'Software de planificación y recursos humanos',
  '48500000': 'Software para comunicación y multimedia',
  '48600000': 'Software para bases de datos y operaciones',
  '48610000': 'Sistemas de bases de datos',
  '48620000': 'Sistemas operativos',
  '48700000': 'Utilidades de paquetes de software',
  '48800000': 'Sistemas y servidores de información',
  '48900000': 'Paquetes de software y sistemas informáticos diversos',

  // === Detalle 72xxx — Servicios TI (completo) ===
  '72100000': 'Servicios de consultoría en hardware',
  '72110000': 'Servicios de consultoría en selección de hardware',
  '72120000': 'Consultoría en recuperación de hardware ante desastres',
  '72130000': 'Consultoría en planificación de salas informáticas',
  '72140000': 'Consultoría en pruebas de aceptación de hardware',
  '72150000': 'Consultoría en auditoría informática y hardware',
  '72200000': 'Servicios de programación y consultoría de software',
  '72210000': 'Programación de paquetes de software',
  '72211000': 'Programación de software de sistemas y de usuario',
  '72212000': 'Programación de software de aplicación',
  '72220000': 'Consultoría de sistemas y técnica',
  '72221000': 'Consultoría en análisis de negocio',
  '72222000': 'Revisión estratégica y planificación de SI/TI',
  '72222300': 'Servicios de tecnologías de la información',
  '72223000': 'Revisión de requisitos de TI',
  '72224000': 'Consultoría en gestión de proyectos',
  '72224100': 'Planificación de implantación de sistemas',
  '72224200': 'Planificación de aseguramiento de calidad',
  '72225000': 'Evaluación y revisión del aseguramiento de calidad',
  '72226000': 'Consultoría en pruebas de aceptación de software',
  '72227000': 'Consultoría en integración de software',
  '72228000': 'Consultoría en integración de hardware',
  '72230000': 'Desarrollo de software personalizado',
  '72240000': 'Análisis de sistemas y programación',
  '72250000': 'Servicios de sistemas y soporte',
  '72253000': 'Helpdesk y soporte',
  '72253100': 'Servicios de helpdesk',
  '72253200': 'Soporte de sistemas',
  '72254000': 'Pruebas de software',
  '72254100': 'Pruebas de sistemas',
  '72260000': 'Servicios relacionados con software',
  '72261000': 'Soporte de software',
  '72262000': 'Desarrollo de software',
  '72263000': 'Implementación de software',
  '72264000': 'Reproducción de software',
  '72265000': 'Configuración de software',
  '72266000': 'Consultoría de software',
  '72267000': 'Mantenimiento y reparación de software',
  '72267100': 'Mantenimiento de software de TI',
  '72268000': 'Suministro de software',
  '72300000': 'Servicios de datos',
  '72310000': 'Tratamiento de datos',
  '72311000': 'Tabulación informática',
  '72311100': 'Conversión de datos',
  '72311200': 'Procesamiento por lotes',
  '72312000': 'Entrada electrónica de datos',
  '72312100': 'Preparación de datos',
  '72313000': 'Captura de datos',
  '72314000': 'Recopilación y cotejo de datos',
  '72315000': 'Gestión y soporte de redes de datos',
  '72315100': 'Soporte de redes de datos',
  '72315200': 'Gestión de redes de datos',
  '72316000': 'Análisis de datos',
  '72317000': 'Almacenamiento de datos',
  '72318000': 'Transmisión de datos',
  '72319000': 'Suministro de datos',
  '72320000': 'Servicios de bases de datos',
  '72321000': 'Bases de datos de valor añadido',
  '72322000': 'Gestión de datos',
  '72330000': 'Normalización y clasificación de contenidos o datos',
  '72400000': 'Servicios de Internet',
  '72410000': 'Servicios de proveedor',
  '72411000': 'Proveedor de servicios de Internet (ISP)',
  '72412000': 'Proveedor de correo electrónico',
  '72413000': 'Diseño de sitios web',
  '72414000': 'Proveedor de motores de búsqueda web',
  '72415000': 'Alojamiento de sitios web',
  '72416000': 'Proveedores de servicios de aplicaciones (ASP)',
  '72417000': 'Nombres de dominio de Internet',
  '72420000': 'Desarrollo de Internet',
  '72421000': 'Desarrollo de aplicaciones cliente Internet/intranet',
  '72422000': 'Desarrollo de aplicaciones servidor Internet/intranet',
  '72500000': 'Servicios informáticos',
  '72510000': 'Gestión informática',
  '72511000': 'Software de gestión de redes',
  '72512000': 'Gestión de documentos',
  '72513000': 'Automatización de oficina',
  '72514000': 'Gestión de instalaciones informáticas',
  '72514100': 'Gestión de instalaciones con operación informática',
  '72514200': 'Gestión de instalaciones de ampliación de sistemas',
  '72514300': 'Gestión de instalaciones de mantenimiento de sistemas',
  '72520000': 'Soporte y consultoría informáticos',
  '72530000': 'Redes informáticas',
  '72531000': 'Red de área local (LAN)',
  '72532000': 'Red de área extensa (WAN)',
  '72540000': 'Actualización informática',
  '72541000': 'Ampliación informática',
  '72541100': 'Ampliación de memoria',
  '72550000': 'Auditoría informática',
  '72590000': 'Servicios profesionales informáticos',
  '72591000': 'Desarrollo de acuerdos de nivel de servicio (SLA)',
  '72600000': 'Soporte y asesoramiento informáticos',
  '72610000': 'Soporte informático',
  '72611000': 'Soporte informático técnico',
  '72700000': 'Servicios de redes informáticas',
  '72710000': 'Redes de área local',
  '72720000': 'Redes de área extensa',
  '72800000': 'Auditoría y pruebas informáticas',
  '72810000': 'Auditoría informática',
  '72820000': 'Pruebas informáticas',
  '72900000': 'Respaldo y conversión de catálogos informáticos',
  '72910000': 'Respaldo informático (backup)',
  '72920000': 'Conversión de catálogos informáticos',
};

/**
 * Busca la descripción de un código CPV.
 * Si no se encuentra exactamente, busca progresivamente por código padre
 * (truncando a ceros desde la derecha).
 * Ejemplo: 72212345 → intenta 72212345, 72212000, 72210000, 72200000, 72000000
 */
export function getCpvDescription(code) {
  if (!code) return null;
  const clean = code.trim();

  // Búsqueda exacta
  if (CPV_CODES[clean]) return CPV_CODES[clean];

  // Búsqueda por código padre (reducir dígitos significativos)
  for (let len = 7; len >= 2; len--) {
    const prefix = clean.substring(0, len).padEnd(8, '0');
    if (CPV_CODES[prefix]) return CPV_CODES[prefix];
  }

  return null;
}

/**
 * Devuelve la descripción corta (primeras palabras) para mostrar como badge.
 */
export function getCpvShortLabel(code) {
  const desc = getCpvDescription(code);
  if (!desc) return code;
  return desc.length > 50 ? desc.substring(0, 47) + '...' : desc;
}

export default CPV_CODES;
