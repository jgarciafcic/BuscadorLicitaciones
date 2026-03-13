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

    public record ParseResult(List<Licitacion> licitaciones, List<String> deletedEntryRefs) {}

    public ParseResult parse(InputStream xml) {
        List<Licitacion> licitaciones = new ArrayList<>();
        List<String> deletedRefs = new ArrayList<>();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        try {
            XMLStreamReader reader = factory.createXMLStreamReader(xml);
            Deque<Element> path = new ArrayDeque<>();
            Licitacion current = null;
            StringBuilder textBuffer = new StringBuilder();
            StringJoiner cpvJoiner = null;

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

                    // entry start
                    if (NS_ATOM.equals(ns) && "entry".equals(local)) {
                        inEntry = true;
                        current = new Licitacion();
                        cpvJoiner = new StringJoiner(",");
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
                        // ProcurementProject
                        if (NS_CAC.equals(ns) && "ProcurementProject".equals(local)) {
                            inProcurementProject = true;
                        }
                        // BudgetAmount
                        if (inProcurementProject && NS_CAC.equals(ns) && "BudgetAmount".equals(local)) {
                            inBudgetAmount = true;
                        }
                        // RequiredCommodityClassification
                        if (inProcurementProject && NS_CAC.equals(ns) && "RequiredCommodityClassification".equals(local)) {
                            inCommodityClassification = true;
                        }
                        // RealizedLocation
                        if (inProcurementProject && NS_CAC.equals(ns) && "RealizedLocation".equals(local)) {
                            inRealizedLocation = true;
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
                            if (NS_CBC.equals(ns) && "ContractFolderID".equals(local) && !inProcurementProject) {
                                current.setExpediente(text);
                            }

                            // ProcurementProject fields
                            if (inProcurementProject) {
                                // Name (objeto) — only direct child of ProcurementProject
                                if (NS_CBC.equals(ns) && "Name".equals(local)
                                        && !inBudgetAmount && !inRealizedLocation && !inCommodityClassification) {
                                    current.setObjeto(text);
                                }

                                // TypeCode (tipoContrato)
                                if (NS_CBC.equals(ns) && "TypeCode".equals(local)) {
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

                            // Document reference URIs (PCAP / PPT)
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
                                if (inDeadlinePeriod) {
                                    if (NS_CBC.equals(ns) && "EndDate".equals(local)) {
                                        current.setFechaLimiteOfertas(parseDate(text));
                                    }
                                    if (NS_CBC.equals(ns) && "EndTime".equals(local)) {
                                        current.setHoraLimiteOfertas(parseTime(text));
                                    }
                                }
                            }
                        }

                        // Close context flags
                        if (NS_CAC.equals(ns) && "BudgetAmount".equals(local)) inBudgetAmount = false;
                        if (NS_CAC.equals(ns) && "RequiredCommodityClassification".equals(local)) inCommodityClassification = false;
                        if (NS_CAC.equals(ns) && "RealizedLocation".equals(local)) inRealizedLocation = false;
                        if (NS_CAC.equals(ns) && "PartyName".equals(local)) inPartyName = false;
                        if (NS_CAC.equals(ns) && "PartyIdentification".equals(local)) inPartyIdentification = false;
                        if (NS_CAC.equals(ns) && "Party".equals(local) && inLocatedContractingParty) inParty = false;
                        if (NS_CAC_PLACE.equals(ns) && "LocatedContractingParty".equals(local)) inLocatedContractingParty = false;
                        if (NS_CAC.equals(ns) && "TenderSubmissionDeadlinePeriod".equals(local)) inDeadlinePeriod = false;
                        if (NS_CAC.equals(ns) && "TenderingProcess".equals(local)) inTenderingProcess = false;
                        if (NS_CAC.equals(ns) && "ProcurementProject".equals(local)) inProcurementProject = false;
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
                            current.setFechaIngesta(LocalDateTime.now());
                            licitaciones.add(current);
                            current = null;
                            cpvJoiner = null;
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
        return new ParseResult(licitaciones, deletedRefs);
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
