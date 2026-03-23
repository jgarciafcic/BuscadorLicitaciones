package com.placsp.monitor.service;

import com.placsp.monitor.model.Licitacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.StringJoiner;

@Component
public class AtomParser {

    private static final Logger log = LoggerFactory.getLogger(AtomParser.class);

    private static final String NS_ATOM = "http://www.w3.org/2005/Atom";
    private static final String NS_CAC = "urn:dgpe:names:draft:codice:schema:xsd:CommonAggregateComponents-2";
    private static final String NS_CBC = "urn:dgpe:names:draft:codice:schema:xsd:CommonBasicComponents-2";
    private static final String NS_CAC_PLACE = "urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonAggregateComponents-2";
    private static final String NS_CBC_PLACE = "urn:dgpe:names:draft:codice-place-ext:schema:xsd:CommonBasicComponents-2";
    private static final String NS_TOMBSTONE = "http://purl.org/atompub/tombstones/1.0";

    public record ParseResult(List<Licitacion> licitaciones, List<String> deletedEntryRefs, String nextLink) {}

    public ParseResult parse(InputStream xml) {
        List<Licitacion> licitaciones = new ArrayList<>();
        List<String> deletedRefs = new ArrayList<>();
        String nextLink = null;

        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        try {
            XMLStreamReader reader = factory.createXMLStreamReader(xml);
            Deque<Element> path = new ArrayDeque<>();
            Licitacion current = null;
            StringBuilder textBuffer = new StringBuilder();
            StringJoiner cpvJoiner = null;

            // Collectors for JSON array fields
            StringBuilder criteriosBuilder = null;
            StringBuilder lotesBuilder = null;
            StringBuilder solvTecBuilder = null;
            StringBuilder solvEcoBuilder = null;

            // Current awarding criteria fields
            String criterioTipo = null;
            String criterioDescripcion = null;
            String criterioPeso = null;

            // Current lot fields
            String loteId = null;
            String loteObjeto = null;
            String loteImporte = null;

            // Tracking nested context
            boolean inEntry = false;
            boolean inContractFolder = false;
            boolean inProcurementProject = false;
            boolean inBudgetAmount = false;
            boolean inLocatedContractingParty = false;
            boolean inParty = false;
            boolean inPartyName = false;
            boolean inPartyIdentification = false;
            boolean inTenderingProcess = false;
            boolean inDeadlinePeriod = false;
            boolean inRealizedLocation = false;
            boolean inCommodityClassification = false;
            boolean inLegalDocRef = false;
            boolean inTechnicalDocRef = false;
            boolean inAttachment = false;
            boolean inExternalRef = false;
            boolean inPlannedPeriod = false;
            boolean inContractExtension = false;
            boolean inOptionValidityPeriod = false;
            boolean inTenderingTerms = false;
            boolean inAwardingTerms = false;
            boolean inAwardingCriteria = false;
            boolean inTendererQualification = false;
            boolean inTechnicalEvaluation = false;
            boolean inFinancialEvaluation = false;
            boolean inProcurementProjectLot = false;
            boolean inLotProcurementProject = false;
            boolean inLotBudgetAmount = false;

            // DurationMeasure attribute
            String durationUnitCode = null;

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT) {
                    String ns = reader.getNamespaceURI();
                    String local = reader.getLocalName();
                    path.push(new Element(ns, local));
                    textBuffer.setLength(0);

                    // deleted-entry
                    if (NS_TOMBSTONE.equals(ns) && "deleted-entry".equals(local)) {
                        String ref = reader.getAttributeValue(null, "ref");
                        if (ref != null) {
                            deletedRefs.add(ref);
                        }
                    }

                    // feed-level next link (outside any entry)
                    if (!inEntry && NS_ATOM.equals(ns) && "link".equals(local)) {
                        String rel = reader.getAttributeValue(null, "rel");
                        if ("next".equals(rel)) {
                            nextLink = reader.getAttributeValue(null, "href");
                        }
                    }

                    // entry start
                    if (NS_ATOM.equals(ns) && "entry".equals(local)) {
                        inEntry = true;
                        current = new Licitacion();
                        cpvJoiner = new StringJoiner(",");
                        criteriosBuilder = new StringBuilder("[");
                        lotesBuilder = new StringBuilder("[");
                        solvTecBuilder = new StringBuilder();
                        solvEcoBuilder = new StringBuilder();
                    }

                    if (!inEntry) continue;

                    // entry > link (ATOM namespace, direct child of entry)
                    if (NS_ATOM.equals(ns) && "link".equals(local) && !inContractFolder) {
                        String href = reader.getAttributeValue(null, "href");
                        if (href != null && current.getEnlacePlacsp() == null) {
                            current.setEnlacePlacsp(href);
                        }
                    }

                    // ContractFolderStatus
                    if (NS_CAC_PLACE.equals(ns) && "ContractFolderStatus".equals(local)) {
                        inContractFolder = true;
                    }

                    if (inContractFolder) {
                        // ProcurementProject (main, not inside lot)
                        if (NS_CAC.equals(ns) && "ProcurementProject".equals(local)) {
                            if (inProcurementProjectLot) {
                                inLotProcurementProject = true;
                            } else {
                                inProcurementProject = true;
                            }
                        }
                        // BudgetAmount
                        if (NS_CAC.equals(ns) && "BudgetAmount".equals(local)) {
                            if (inLotProcurementProject) {
                                inLotBudgetAmount = true;
                            } else if (inProcurementProject) {
                                inBudgetAmount = true;
                            }
                        }
                        // RequiredCommodityClassification
                        if (inProcurementProject && NS_CAC.equals(ns) && "RequiredCommodityClassification".equals(local)) {
                            inCommodityClassification = true;
                        }
                        // RealizedLocation
                        if (inProcurementProject && NS_CAC.equals(ns) && "RealizedLocation".equals(local)) {
                            inRealizedLocation = true;
                        }
                        // PlannedPeriod
                        if (inProcurementProject && !inProcurementProjectLot && NS_CAC.equals(ns) && "PlannedPeriod".equals(local)) {
                            inPlannedPeriod = true;
                        }
                        // ContractExtension
                        if (inProcurementProject && !inProcurementProjectLot && NS_CAC.equals(ns) && "ContractExtension".equals(local)) {
                            inContractExtension = true;
                        }
                        // OptionValidityPeriod (inside ContractExtension)
                        if (inContractExtension && NS_CAC.equals(ns) && "OptionValidityPeriod".equals(local)) {
                            inOptionValidityPeriod = true;
                        }
                        // LocatedContractingParty
                        if (NS_CAC_PLACE.equals(ns) && "LocatedContractingParty".equals(local)) {
                            inLocatedContractingParty = true;
                        }
                        // Party (inside LocatedContractingParty)
                        if (inLocatedContractingParty && NS_CAC.equals(ns) && "Party".equals(local)) {
                            inParty = true;
                        }
                        // PartyName
                        if (inParty && NS_CAC.equals(ns) && "PartyName".equals(local)) {
                            inPartyName = true;
                        }
                        // PartyIdentification
                        if (inParty && NS_CAC.equals(ns) && "PartyIdentification".equals(local)) {
                            inPartyIdentification = true;
                        }
                        // TenderingProcess
                        if (NS_CAC.equals(ns) && "TenderingProcess".equals(local)) {
                            inTenderingProcess = true;
                        }
                        // TenderSubmissionDeadlinePeriod
                        if (inTenderingProcess && NS_CAC.equals(ns) && "TenderSubmissionDeadlinePeriod".equals(local)) {
                            inDeadlinePeriod = true;
                        }
                        // TenderingTerms
                        if (NS_CAC.equals(ns) && "TenderingTerms".equals(local)) {
                            inTenderingTerms = true;
                        }
                        // AwardingTerms > AwardingCriteria
                        if (inTenderingTerms && NS_CAC.equals(ns) && "AwardingTerms".equals(local)) {
                            inAwardingTerms = true;
                        }
                        if (inAwardingTerms && NS_CAC.equals(ns) && "AwardingCriteria".equals(local)) {
                            inAwardingCriteria = true;
                            criterioTipo = null;
                            criterioDescripcion = null;
                            criterioPeso = null;
                        }
                        // TendererQualificationRequest
                        if (inTenderingTerms && NS_CAC.equals(ns) && "TendererQualificationRequest".equals(local)) {
                            inTendererQualification = true;
                        }
                        if (inTendererQualification && NS_CAC.equals(ns) && "TechnicalEvaluationCriteria".equals(local)) {
                            inTechnicalEvaluation = true;
                        }
                        if (inTendererQualification && NS_CAC.equals(ns) && "FinancialEvaluationCriteria".equals(local)) {
                            inFinancialEvaluation = true;
                        }
                        // ProcurementProjectLot
                        if (NS_CAC.equals(ns) && "ProcurementProjectLot".equals(local)) {
                            inProcurementProjectLot = true;
                            loteId = null;
                            loteObjeto = null;
                            loteImporte = null;
                        }
                        // LegalDocumentReference (PCAP)
                        if (NS_CAC.equals(ns) && "LegalDocumentReference".equals(local)) {
                            inLegalDocRef = true;
                        }
                        // TechnicalDocumentReference (PPT)
                        if (NS_CAC.equals(ns) && "TechnicalDocumentReference".equals(local)) {
                            inTechnicalDocRef = true;
                        }
                        // Attachment > ExternalReference (inside doc refs)
                        if ((inLegalDocRef || inTechnicalDocRef) && NS_CAC.equals(ns) && "Attachment".equals(local)) {
                            inAttachment = true;
                        }
                        if (inAttachment && NS_CAC.equals(ns) && "ExternalReference".equals(local)) {
                            inExternalRef = true;
                        }

                        // DurationMeasure — capture unitCode attribute
                        if (inPlannedPeriod && NS_CBC.equals(ns) && "DurationMeasure".equals(local)) {
                            durationUnitCode = reader.getAttributeValue(null, "unitCode");
                        }
                    }

                } else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                    textBuffer.append(reader.getText());

                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    String ns = reader.getNamespaceURI();
                    String local = reader.getLocalName();
                    String text = textBuffer.toString().trim();

                    if (inEntry && current != null) {

                        // entry > id
                        if (NS_ATOM.equals(ns) && "id".equals(local) && !inContractFolder) {
                            current.setEntryId(text);
                        }

                        // entry > updated
                        if (NS_ATOM.equals(ns) && "updated".equals(local) && !inContractFolder) {
                            current.setFechaActualizacion(parseDateTime(text));
                        }

                        if (inContractFolder) {
                            // ContractFolderStatusCode
                            if (NS_CBC_PLACE.equals(ns) && "ContractFolderStatusCode".equals(local)) {
                                current.setEstado(text);
                            }

                            // ContractFolderID
                            if (NS_CBC.equals(ns) && "ContractFolderID".equals(local) && !inProcurementProject && !inLotProcurementProject) {
                                current.setExpediente(text);
                            }

                            // ProcurementProject fields (main, not lot)
                            if (inProcurementProject && !inProcurementProjectLot) {
                                // Name (objeto)
                                if (NS_CBC.equals(ns) && "Name".equals(local)
                                        && !inBudgetAmount && !inRealizedLocation && !inCommodityClassification
                                        && !inPlannedPeriod && !inContractExtension) {
                                    current.setObjeto(text);
                                }

                                // TypeCode (tipoContrato)
                                if (NS_CBC.equals(ns) && "TypeCode".equals(local)
                                        && !inBudgetAmount && !inRealizedLocation && !inCommodityClassification) {
                                    current.setTipoContrato(text);
                                }

                                // BudgetAmount fields
                                if (inBudgetAmount) {
                                    if (NS_CBC.equals(ns) && "TaxExclusiveAmount".equals(local)) {
                                        current.setImporteSinImpuestos(parseBigDecimal(text));
                                    }
                                    if (NS_CBC.equals(ns) && "TotalAmount".equals(local)) {
                                        current.setImporteConImpuestos(parseBigDecimal(text));
                                    }
                                }

                                // CPV codes
                                if (inCommodityClassification && NS_CBC.equals(ns) && "ItemClassificationCode".equals(local)) {
                                    cpvJoiner.add(text);
                                }

                                // RealizedLocation
                                if (inRealizedLocation) {
                                    if (NS_CBC.equals(ns) && "CountrySubentityCode".equals(local)) {
                                        current.setNutsCode(text);
                                    }
                                    if (NS_CBC.equals(ns) && "CountrySubentity".equals(local)) {
                                        current.setLugarEjecucion(text);
                                    }
                                }

                                // PlannedPeriod (duración)
                                if (inPlannedPeriod) {
                                    if (NS_CBC.equals(ns) && "DurationMeasure".equals(local)) {
                                        current.setDuracionMedida(text);
                                        current.setDuracionUnidad(durationUnitCode);
                                        durationUnitCode = null;
                                    }
                                    if (NS_CBC.equals(ns) && "StartDate".equals(local)) {
                                        current.setDuracionInicio(parseDate(text));
                                    }
                                    if (NS_CBC.equals(ns) && "EndDate".equals(local)) {
                                        current.setDuracionFin(parseDate(text));
                                    }
                                }

                                // ContractExtension > OptionValidityPeriod > Description (prórroga)
                                if (inOptionValidityPeriod && NS_CBC.equals(ns) && "Description".equals(local)) {
                                    current.setProrroga(text);
                                }
                            }

                            // Lot fields
                            if (inProcurementProjectLot) {
                                if (NS_CBC.equals(ns) && "ProcurementProjectLotID".equals(local)) {
                                    loteId = text;
                                }
                                if (inLotProcurementProject && NS_CBC.equals(ns) && "Name".equals(local)
                                        && !inLotBudgetAmount) {
                                    loteObjeto = text;
                                }
                                if (inLotBudgetAmount && NS_CBC.equals(ns) && "TaxExclusiveAmount".equals(local)) {
                                    loteImporte = text;
                                }
                            }

                            // LocatedContractingParty > Party > PartyName > Name
                            if (inPartyName && NS_CBC.equals(ns) && "Name".equals(local)) {
                                current.setOrganoContratacion(text);
                            }

                            // LocatedContractingParty > Party > PartyIdentification > ID
                            if (inPartyIdentification && NS_CBC.equals(ns) && "ID".equals(local)) {
                                if (current.getOrganoId() == null) {
                                    current.setOrganoId(text);
                                }
                            }

                            // Document reference URIs (PCAP / PPT / Anuncio)
                            if (inExternalRef && NS_CBC.equals(ns) && "URI".equals(local)) {
                                if (inLegalDocRef && current.getUrlPcap() == null) {
                                    current.setUrlPcap(text);
                                } else if (inTechnicalDocRef && current.getUrlPpt() == null) {
                                    current.setUrlPpt(text);
                                }
                            }

                            // TenderingProcess fields
                            if (inTenderingProcess) {
                                if (NS_CBC.equals(ns) && "ProcedureCode".equals(local)) {
                                    current.setTipoProcedimiento(text);
                                }
                                if (NS_CBC.equals(ns) && "UrgencyCode".equals(local)) {
                                    current.setUrgencia(text);
                                }
                                if (inDeadlinePeriod) {
                                    if (NS_CBC.equals(ns) && "EndDate".equals(local)) {
                                        current.setFechaLimiteOfertas(parseDate(text));
                                    }
                                    if (NS_CBC.equals(ns) && "EndTime".equals(local)) {
                                        current.setHoraLimiteOfertas(parseTime(text));
                                    }
                                }
                            }

                            // AwardingCriteria fields
                            if (inAwardingCriteria) {
                                if (NS_CBC.equals(ns) && "AwardingCriteriaTypeCode".equals(local)) {
                                    criterioTipo = text;
                                }
                                if (NS_CBC.equals(ns) && "Description".equals(local)) {
                                    criterioDescripcion = text;
                                }
                                if (NS_CBC.equals(ns) && "WeightNumeric".equals(local)) {
                                    criterioPeso = text;
                                }
                            }

                            // Solvencia técnica
                            if (inTechnicalEvaluation && NS_CBC.equals(ns) && "Description".equals(local)) {
                                if (!text.isEmpty()) {
                                    if (solvTecBuilder.length() > 0) solvTecBuilder.append(" | ");
                                    solvTecBuilder.append(text);
                                }
                            }

                            // Solvencia económica
                            if (inFinancialEvaluation && NS_CBC.equals(ns) && "Description".equals(local)) {
                                if (!text.isEmpty()) {
                                    if (solvEcoBuilder.length() > 0) solvEcoBuilder.append(" | ");
                                    solvEcoBuilder.append(text);
                                }
                            }
                        }

                        // Close context flags — order matters: close inner before outer
                        if (NS_CAC.equals(ns) && "BudgetAmount".equals(local)) {
                            inBudgetAmount = false;
                            inLotBudgetAmount = false;
                        }
                        if (NS_CAC.equals(ns) && "RequiredCommodityClassification".equals(local)) inCommodityClassification = false;
                        if (NS_CAC.equals(ns) && "RealizedLocation".equals(local)) inRealizedLocation = false;
                        if (NS_CAC.equals(ns) && "PlannedPeriod".equals(local)) inPlannedPeriod = false;
                        if (NS_CAC.equals(ns) && "OptionValidityPeriod".equals(local)) inOptionValidityPeriod = false;
                        if (NS_CAC.equals(ns) && "ContractExtension".equals(local)) inContractExtension = false;
                        if (NS_CAC.equals(ns) && "PartyName".equals(local)) inPartyName = false;
                        if (NS_CAC.equals(ns) && "PartyIdentification".equals(local)) inPartyIdentification = false;
                        if (NS_CAC.equals(ns) && "Party".equals(local) && inLocatedContractingParty) inParty = false;
                        if (NS_CAC_PLACE.equals(ns) && "LocatedContractingParty".equals(local)) inLocatedContractingParty = false;
                        if (NS_CAC.equals(ns) && "TenderSubmissionDeadlinePeriod".equals(local)) inDeadlinePeriod = false;
                        if (NS_CAC.equals(ns) && "TenderingProcess".equals(local)) inTenderingProcess = false;

                        // Close AwardingCriteria — serialize to JSON
                        if (inAwardingCriteria && NS_CAC.equals(ns) && "AwardingCriteria".equals(local)) {
                            if (criteriosBuilder.length() > 1) criteriosBuilder.append(",");
                            criteriosBuilder.append("{\"tipo\":\"").append(jsonEscape(criterioTipo))
                                    .append("\",\"descripcion\":\"").append(jsonEscape(criterioDescripcion))
                                    .append("\",\"peso\":").append(criterioPeso != null ? criterioPeso : "null")
                                    .append("}");
                            inAwardingCriteria = false;
                        }
                        if (NS_CAC.equals(ns) && "AwardingTerms".equals(local)) inAwardingTerms = false;
                        if (NS_CAC.equals(ns) && "TechnicalEvaluationCriteria".equals(local)) inTechnicalEvaluation = false;
                        if (NS_CAC.equals(ns) && "FinancialEvaluationCriteria".equals(local)) inFinancialEvaluation = false;
                        if (NS_CAC.equals(ns) && "TendererQualificationRequest".equals(local)) inTendererQualification = false;
                        if (NS_CAC.equals(ns) && "TenderingTerms".equals(local)) inTenderingTerms = false;

                        // Close ProcurementProjectLot — serialize lot to JSON
                        if (inProcurementProjectLot && NS_CAC.equals(ns) && "ProcurementProjectLot".equals(local)) {
                            if (lotesBuilder.length() > 1) lotesBuilder.append(",");
                            lotesBuilder.append("{\"id\":\"").append(jsonEscape(loteId))
                                    .append("\",\"objeto\":\"").append(jsonEscape(loteObjeto))
                                    .append("\",\"importe\":").append(loteImporte != null ? loteImporte : "null")
                                    .append("}");
                            inProcurementProjectLot = false;
                            inLotProcurementProject = false;
                            inLotBudgetAmount = false;
                        }

                        if (NS_CAC.equals(ns) && "ProcurementProject".equals(local)) {
                            if (inLotProcurementProject) {
                                inLotProcurementProject = false;
                            } else {
                                inProcurementProject = false;
                            }
                        }
                        if (NS_CAC.equals(ns) && "ExternalReference".equals(local)) inExternalRef = false;
                        if (NS_CAC.equals(ns) && "Attachment".equals(local)) inAttachment = false;
                        if (NS_CAC.equals(ns) && "LegalDocumentReference".equals(local)) { inLegalDocRef = false; inAttachment = false; inExternalRef = false; }
                        if (NS_CAC.equals(ns) && "TechnicalDocumentReference".equals(local)) { inTechnicalDocRef = false; inAttachment = false; inExternalRef = false; }
                        if (NS_CAC_PLACE.equals(ns) && "ContractFolderStatus".equals(local)) inContractFolder = false;

                        // entry end
                        if (NS_ATOM.equals(ns) && "entry".equals(local)) {
                            String cpvResult = cpvJoiner.toString();
                            if (!cpvResult.isEmpty()) {
                                current.setCpvCodes(cpvResult);
                            }

                            // Finalize JSON arrays
                            criteriosBuilder.append("]");
                            String criteriosJson = criteriosBuilder.toString();
                            if (!"[]".equals(criteriosJson)) {
                                current.setCriteriosAdjudicacion(criteriosJson);
                            }

                            lotesBuilder.append("]");
                            String lotesJson = lotesBuilder.toString();
                            if (!"[]".equals(lotesJson)) {
                                current.setLotes(lotesJson);
                            }

                            if (solvTecBuilder.length() > 0) {
                                current.setSolvenciaTecnica(solvTecBuilder.toString());
                            }
                            if (solvEcoBuilder.length() > 0) {
                                current.setSolvenciaEconomica(solvEcoBuilder.toString());
                            }

                            current.setFechaIngesta(LocalDateTime.now());
                            licitaciones.add(current);
                            current = null;
                            cpvJoiner = null;
                            criteriosBuilder = null;
                            lotesBuilder = null;
                            solvTecBuilder = null;
                            solvEcoBuilder = null;
                            inEntry = false;
                        }
                    }

                    if (!path.isEmpty()) path.pop();
                    textBuffer.setLength(0);
                }
            }

            reader.close();
        } catch (XMLStreamException e) {
            log.error("Error parseando feed ATOM: {}", e.getMessage(), e);
        }

        log.info("Parseadas {} licitaciones, {} deleted-entries", licitaciones.size(), deletedRefs.size());
        return new ParseResult(licitaciones, deletedRefs, nextLink);
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static LocalDateTime parseDateTime(String text) {
        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static LocalTime parseTime(String text) {
        try {
            return LocalTime.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static BigDecimal parseBigDecimal(String text) {
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record Element(String ns, String localName) {}
}
