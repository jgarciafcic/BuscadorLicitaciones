# PLACSP Monitor — Buscador de Licitaciones Públicas TIC

## Objetivo

Aplicación web para consumir el feed ATOM de sindicación de la Plataforma de Contratación del Sector Público (PLACSP), almacenar licitaciones en BBDD y ofrecer un buscador con filtros dinámicos.

**Filtros por defecto:**
- Tipo de contrato: Servicios (código `2`)
- Estado: En plazo / Publicada (código `PUB`)
- CPV: Códigos que empiecen por `72` (servicios IT)

## Stack tecnológico

### Backend
- **Java 21** (sin Lombok — usar records de Java donde sea posible)
- **Spring Boot 3.4+** (Web, Data JPA, Scheduling)
- **H2** en desarrollo / **PostgreSQL** en producción
- **Maven** como build tool
- **Módulos principales:**
  - Servicio de ingesta del feed ATOM (scheduled task)
  - Parseo XML con JAXP/StAX (sin dependencias externas pesadas)
  - API REST para el frontend
  - Repositorio JPA para persistencia

### Frontend
- **React 18+** con Vite
- **Componentes funcionales** con hooks
- UI inspirada en AdjudicacionesTIC (ver sección de referencia)
- Tabla de resultados paginada del lado servidor
- Panel de filtros con selects, inputs de texto y rango de importes

---

## Feed ATOM — Especificación técnica completa

### URL del feed (sindicación 643 — perfiles del contratante, sin contratos menores)

```
https://contrataciondelsectorpublico.gob.es/sindicacion/sindicacion_643/licitacionesPerfilesContratanteCompleto3.atom
```

### Estructura general

El feed sigue RFC 4287 (Atom Syndication Format). Cada `<entry>` es una licitación.

**Paginación (RFC 5005 Paged Feeds):**
- `<link rel="next" href="..."/>` → siguiente página (más antiguas)
- `<link rel="first" href="..."/>` → primera página (más reciente)
- `<link rel="last" href="..."/>` → última página
- Cada página contiene máximo **500 entries**
- Ordenadas por `<updated>` de más reciente a más antigua

### Namespaces XML

```java
public static final String NS_ATOM = "http://www.w3.org/2005/Atom";
public static final String NS_CAC = "urn:dgpe:names:draft:codice:schema:xsd:CommonAggregateComponents-2";
public static final String NS_CBC = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2";
public static final String NS_CAC_PLACE = "urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonAggregateComponents-2";
public static final String NS_CBC_PLACE = "urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonBasicComponents-2";
public static final String NS_TOMBSTONE = "http://purl.org/atompub/tombstones/1.0";
```

### Estructura de un entry — XPaths y campos clave

Todo va dentro de `<entry>` → `<cac-place-ext:ContractFolderStatus>`:

#### 1. Estado de la licitación
**XPath:** `cbc-place-ext:ContractFolderStatusCode`
```xml
<cbc-place-ext:ContractFolderStatusCode
  listURI="https://contrataciondelestado.es/codice/cl/2.07/SyndicationContractFolderStatusCode-2.07.gc">
  PUB
</cbc-place-ext:ContractFolderStatusCode>
```
**Valores:**
| Código | Descripción |
|--------|-------------|
| `PRE`  | Anuncio previo |
| `PUB`  | En plazo (publicada, plazo abierto) |
| `EV`   | Pendiente de adjudicación |
| `ADJ`  | Adjudicada |
| `RES`  | Resuelta |
| `ANU`  | Anulada |

#### 2. Número de expediente
**XPath:** `cbc:ContractFolderID`
```xml
<cbc:ContractFolderID>52/2013</cbc:ContractFolderID>
```

#### 3. Objeto del contrato
**XPath:** `cac:ProcurementProject/cbc:Name`
```xml
<cac:ProcurementProject>
  <cbc:Name>Descripción del contrato...</cbc:Name>
</cac:ProcurementProject>
```

#### 4. Tipo de contrato
**XPath:** `cac:ProcurementProject/cbc:TypeCode`
```xml
<cbc:TypeCode listURI="...SyndicationContractCode...">2</cbc:TypeCode>
```
**Valores:**
| Código | Tipo |
|--------|------|
| `1`    | Obras |
| `2`    | **Servicios** ← filtro por defecto |
| `3`    | Suministros |
| `7`    | Concesión de obras |
| `8`    | Concesión de servicios |
| `21`   | Privado |
| `31`   | Administrativo especial |
| `40`   | Patrimonial |
| `50`   | Otros |

#### 5. Clasificación CPV (puede haber múltiples)
**XPath:** `cac:ProcurementProject/cac:RequiredCommodityClassification/cbc:ItemClassificationCode`
```xml
<cac:RequiredCommodityClassification>
  <cbc:ItemClassificationCode listURI="...CPV2007...">72000000</cbc:ItemClassificationCode>
</cac:RequiredCommodityClassification>
```
**Filtro por defecto:** cualquier código que empiece por `72` (servicios IT).

#### 6. Presupuesto base de licitación
**XPath:** `cac:ProcurementProject/cac:BudgetAmount/cbc:TaxExclusiveAmount` (sin impuestos)
**XPath:** `cac:ProcurementProject/cac:BudgetAmount/cbc:TotalAmount` (con impuestos)
```xml
<cac:BudgetAmount>
  <cbc:TaxExclusiveAmount currencyID="EUR">4949909.74</cbc:TaxExclusiveAmount>
  <cbc:TotalAmount currencyID="EUR">5989390.79</cbc:TotalAmount>
</cac:BudgetAmount>
```

#### 7. Órgano de contratación
**XPath:** `cac-place-ext:LocatedContractingParty/cac:Party/cac:PartyName/cbc:Name`
**XPath (DIR3):** `cac-place-ext:LocatedContractingParty/cac:Party/cac:PartyIdentification/cbc:ID[@schemeName='DIR3']`
**XPath (NIF):** `cac-place-ext:LocatedContractingParty/cac:Party/cac:PartyIdentification/cbc:ID[@schemeName='NIF']`

#### 8. Plazo de presentación de ofertas
**XPath:** `cac:TenderingProcess/cac:TenderSubmissionDeadlinePeriod/cbc:EndDate`
**XPath:** `cac:TenderingProcess/cac:TenderSubmissionDeadlinePeriod/cbc:EndTime`
```xml
<cac:TenderSubmissionDeadlinePeriod>
  <cbc:EndDate>2024-03-15</cbc:EndDate>
  <cbc:EndTime>14:00:00</cbc:EndTime>
</cac:TenderSubmissionDeadlinePeriod>
```

#### 9. Tipo de procedimiento
**XPath:** `cac:TenderingProcess/cbc:ProcedureCode`
```xml
<cbc:ProcedureCode listURI="...SyndicationTenderingProcessCode...">1</cbc:ProcedureCode>
```
**Valores:**
| Código | Procedimiento |
|--------|---------------|
| `1`    | Abierto |
| `2`    | Restringido |
| `3`    | Negociado con publicidad |
| `4`    | Negociado sin publicidad |
| `5`    | Diálogo competitivo |
| `6`    | Contrato menor |
| `100`  | Simplificado |

#### 10. Lugar de ejecución (NUTS)
**XPath:** `cac:ProcurementProject/cac:RealizedLocation/cbc:CountrySubentityCode`
**XPath:** `cac:ProcurementProject/cac:RealizedLocation/cbc:CountrySubentity`
```xml
<cac:RealizedLocation>
  <cbc:CountrySubentityCode listURI="...NUTS-2016.gc">ES130</cbc:CountrySubentityCode>
  <cbc:CountrySubentity>Cantabria</cbc:CountrySubentity>
</cac:RealizedLocation>
```

#### 11. Enlace a la licitación en PLACSP
Del propio `<entry>`:
```xml
<link href="https://contrataciondelestado.es/wps/poc?uri=deeplink:detalle_licitacion&amp;idEvl=..."/>
```

#### 12. Fecha de actualización
Del propio `<entry>`:
```xml
<updated>2015-08-25T15:00:00+02:00</updated>
```

#### 13. Duración del contrato
**XPath:** `cac:ProcurementProject/cac:PlannedPeriod`
Puede contener `cbc:StartDate`, `cbc:EndDate` y/o `cbc:DurationMeasure` (con `unitCode` DAY/MON/ANN).

### Consideraciones importantes del feed

1. **Entries duplicadas:** Puede haber múltiples entries con el mismo `<id>` → solo procesar la más reciente (mayor `<updated>`).
2. **Entries eliminadas:** `<at:deleted-entry ref="..." when="..."/>` indica licitaciones retiradas.
3. **Tamaño:** El feed completo tiene miles de páginas. Para la ingesta inicial, recorrer N páginas configurables.
4. **Zona horaria:** Todas las fechas en horario peninsular español.
5. **Actualización incremental:** Guardar el último `<updated>` procesado para solo pedir novedades.

---

## Modelo de datos (entidad JPA)

```java
@Entity
@Table(name = "licitaciones")
public class Licitacion {
    @Id
    private String entryId;           // <id> del entry ATOM (URI única)
    private String expediente;         // ContractFolderID
    @Column(length = 2000)
    private String objeto;             // ProcurementProject/Name
    private String estado;             // PUB, ADJ, RES, etc.
    private String tipoContrato;       // 1=Obras, 2=Servicios, 3=Suministros...
    private BigDecimal importeSinImpuestos;
    private BigDecimal importeConImpuestos;
    private String organoContratacion; // Nombre del órgano
    private String organoId;           // DIR3 o NIF
    private String cpvCodes;           // Códigos CPV separados por coma
    private LocalDate fechaLimiteOfertas;
    private LocalTime horaLimiteOfertas;
    private String tipoProcedimiento;
    private String lugarEjecucion;     // Texto (ej. "Cantabria")
    private String nutsCode;           // Código NUTS (ej. "ES130")
    private String enlacePlacsp;       // URL a PLACSP
    private LocalDateTime fechaActualizacion; // <updated> del entry
    private LocalDateTime fechaIngesta;       // Cuándo se procesó
}
```

---

## API REST

### Endpoints

```
GET /api/licitaciones?estado=PUB&tipoContrato=2&cpv=72&importeMin=10000&importeMax=500000
                      &texto=desarrollo&organo=&ccaa=&procedimiento=
                      &page=0&size=20&sort=fechaLimiteOfertas,asc
```

**Parámetros de filtro:**
| Parámetro | Tipo | Descripción | Default |
|-----------|------|-------------|---------|
| `estado` | String | Código estado: PUB, ADJ, etc. | `PUB` |
| `tipoContrato` | String | Código tipo: 1, 2, 3... | `2` |
| `cpv` | String | Prefijo CPV (ej. "72") | `72` |
| `texto` | String | Búsqueda en objeto + órgano + expediente | (vacío) |
| `importeMin` | BigDecimal | Importe mínimo sin impuestos | (sin límite) |
| `importeMax` | BigDecimal | Importe máximo sin impuestos | (sin límite) |
| `nutsCode` | String | Código NUTS lugar ejecución | (todos) |
| `procedimiento` | String | Código tipo procedimiento | (todos) |
| `page` | int | Página (0-indexed) | `0` |
| `size` | int | Resultados por página | `20` |
| `sort` | String | Campo y dirección | `fechaActualizacion,desc` |

**Respuesta:** `Page<LicitacionDto>` de Spring Data (con totalElements, totalPages, etc.)

```
GET /api/licitaciones/stats
```
Devuelve contadores agrupados para gráficas (por tipo contrato, por CCAA, etc.)

```
POST /api/ingesta/ejecutar
```
Lanza la ingesta manual del feed (además del cron automático).

---

## Referencia UI — AdjudicacionesTIC

La interfaz se inspira en https://www.adjudicacionestic.com pero con estas diferencias:

### Panel de filtros (parte superior)
Disponer en fila, responsive, con los siguientes controles:

1. **Texto libre** — Input text, placeholder: "Título, licitador, nº expediente o CPV..."
2. **Importe** — Dos inputs numéricos: "Importe mín." e "Importe máx." (en euros). Diferencia clave con AdjudicacionesTIC que solo tiene superior/inferior.
3. **Estado** — Select: PUB (por defecto), ADJ, RES, EV, PRE, ANU, Todos
4. **Tipo contrato** — Select: Servicios (por defecto), Obras, Suministros, etc.
5. **CPV** — Input text, placeholder: "72" por defecto. El usuario puede poner "72" para IT o dejarlo vacío para todos.
6. **Comunidad Autónoma** — Select con las 17 CCAA + Ceuta + Melilla (mapeo NUTS)
7. **Tipo de procedimiento** — Select: Abierto, Restringido, Simplificado, etc.
8. **Plazo** — Select: "En plazo", "Expiran en < 7 días", "Expiran en < 15 días", "Expiran en < 30 días"

Botón "Buscar licitaciones" que lanza la consulta.
Opción "Ver más/menos filtros" para ocultar filtros secundarios (CCAA, procedimiento, plazo).

### Tabla de resultados
Columnas:
| Fecha pub. | Límite ofertas | Título licitación | Importe |
|------------|---------------|-------------------|---------|

- **Fecha pub.**: fecha de actualización, formato dd/MM/yyyy
- **Límite ofertas**: fecha + hora límite, formato dd/MM/yyyy HH:mm
- **Título**: incluye en texto pequeño el órgano de contratación, y el objeto del contrato en tamaño normal. Enlace a PLACSP.
- **Importe**: formateado con separador de miles y símbolo €

Paginación del lado servidor con navegación tipo "Mostrando del 1 al 20 de 347 licitaciones".

### Resumen / métricas
Un contador visible: "Se han encontrado X licitaciones por un importe total de Y €"

---

## Estructura del proyecto

```
placsp-monitor/
├── CLAUDE.md
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/placsp/monitor/
│       ├── PlacspMonitorApplication.java
│       ├── config/
│       │   └── AppConfig.java
│       ├── model/
│       │   └── Licitacion.java          # Entidad JPA (sin Lombok)
│       ├── dto/
│       │   └── LicitacionDto.java       # Record Java
│       ├── repository/
│       │   └── LicitacionRepository.java # Spring Data JPA + Specification
│       ├── service/
│       │   ├── FeedIngestaService.java   # Descarga y parseo del ATOM feed
│       │   ├── AtomParser.java           # Parseo XML StAX
│       │   └── LicitacionService.java    # Lógica de búsqueda con filtros
│       ├── controller/
│       │   ├── LicitacionController.java # API REST filtros + paginación
│       │   └── IngestaController.java    # Trigger manual ingesta
│       └── scheduler/
│           └── IngestaScheduler.java     # Cron para ingesta periódica
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── App.jsx
│       ├── components/
│       │   ├── SearchFilters.jsx    # Panel de filtros
│       │   ├── ResultsTable.jsx     # Tabla paginada
│       │   └── ResultsSummary.jsx   # Contador resultados
│       ├── hooks/
│       │   └── useLicitaciones.js   # Hook para fetch + estado
│       └── services/
│           └── api.js               # Llamadas al backend
└── README.md
```

---

## Plan de ejecución paso a paso con Claude Code

### Paso 1: Scaffold del backend
Pedir a Claude Code que genere el `pom.xml` con Spring Boot 3.4, Java 21, dependencias (Web, JPA, H2, Validation). Luego la clase Application, la entidad, el repositorio y un primer endpoint `/api/health`.

### Paso 2: Parser del feed ATOM
Implementar `AtomParser` con StAX para parsear el XML del feed. Implementar `FeedIngestaService` que descargue las primeras N páginas, parsee entries y guarde en BBDD.

### Paso 3: API REST con filtros
Implementar `LicitacionRepository` con JPA Specifications para filtrado dinámico. Implementar `LicitacionController` con todos los parámetros. Probar con H2 console.

### Paso 4: Scaffold del frontend
Generar proyecto React con Vite. Crear componentes base: `SearchFilters`, `ResultsTable`, `ResultsSummary`.

### Paso 5: Integración y estilo
Conectar frontend con backend. Aplicar estilos CSS inspirados en AdjudicacionesTIC: limpio, profesional, tabla con bordes sutiles, filtros en fila.

---

## Configuración Spring Boot (application.yml)

```yaml
placsp:
  feed:
    url: https://contrataciondelsectorpublico.gob.es/sindicacion/sindicacion_643/licitacionesPerfilesContratanteCompleto3.atom
    max-pages: 5        # Páginas a recorrer en cada ingesta
    cron: "0 0 7 * * *" # Cada día a las 7:00

spring:
  datasource:
    url: jdbc:h2:file:./data/placsp
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  h2:
    console:
      enabled: true
```

## Mapeo NUTS → Comunidad Autónoma (para el filtro de CCAA)

| NUTS  | Comunidad Autónoma |
|-------|--------------------|
| ES11  | Galicia |
| ES12  | Principado de Asturias |
| ES13  | Cantabria |
| ES21  | País Vasco |
| ES22  | Comunidad Foral de Navarra |
| ES23  | La Rioja |
| ES24  | Aragón |
| ES30  | Comunidad de Madrid |
| ES41  | Castilla y León |
| ES42  | Castilla-La Mancha |
| ES43  | Extremadura |
| ES51  | Cataluña |
| ES52  | Comunitat Valenciana |
| ES53  | Illes Balears |
| ES61  | Andalucía |
| ES62  | Región de Murcia |
| ES63  | Ciudad Autónoma de Ceuta |
| ES64  | Ciudad Autónoma de Melilla |
| ES70  | Canarias |
