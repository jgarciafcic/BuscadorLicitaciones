package com.placsp.monitor.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.ContentBlock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placsp.monitor.config.LlmConfig;
import com.placsp.monitor.model.AnalisisPliego;
import com.placsp.monitor.model.Licitacion;
import com.placsp.monitor.repository.AnalisisPliegoRepository;
import com.placsp.monitor.repository.LicitacionRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PliegoAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(PliegoAnalysisService.class);

    private final LlmConfig llmConfig;
    private final LicitacionRepository licitacionRepository;
    private final AnalisisPliegoRepository analisisRepository;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PliegoAnalysisService(LlmConfig llmConfig,
                                  LicitacionRepository licitacionRepository,
                                  AnalisisPliegoRepository analisisRepository) {
        this.llmConfig = llmConfig;
        this.licitacionRepository = licitacionRepository;
        this.analisisRepository = analisisRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public record AnalisisResult(String resumenPcap, String resumenPpt, boolean fromCache) {}

    public AnalisisResult analizar(String entryId) {
        // Check cache
        Optional<AnalisisPliego> cached = analisisRepository.findByEntryId(entryId);
        if (cached.isPresent()) {
            AnalisisPliego c = cached.get();
            log.info("Análisis de pliegos para {} recuperado de caché", entryId);
            return new AnalisisResult(c.getResumenPcap(), c.getResumenPpt(), true);
        }

        Licitacion lic = licitacionRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Licitación no encontrada: " + entryId));

        String resumenPcap = null;
        String resumenPpt = null;

        if (lic.getUrlPcap() != null && !lic.getUrlPcap().isBlank()) {
            resumenPcap = downloadAndAnalyze(lic.getUrlPcap(), buildPcapPrompt());
        }

        if (lic.getUrlPpt() != null && !lic.getUrlPpt().isBlank()) {
            resumenPpt = downloadAndAnalyze(lic.getUrlPpt(), buildPptPrompt());
        }

        // Save to cache
        AnalisisPliego analisis = new AnalisisPliego();
        analisis.setEntryId(entryId);
        analisis.setResumenPcap(resumenPcap);
        analisis.setResumenPpt(resumenPpt);
        analisis.setFechaAnalisis(LocalDateTime.now());
        analisisRepository.save(analisis);

        return new AnalisisResult(resumenPcap, resumenPpt, false);
    }

    private String downloadAndAnalyze(String url, String prompt) {
        try {
            String pdfText = downloadAndExtractText(url);
            if (pdfText == null || pdfText.isBlank()) {
                return "No se pudo extraer texto del documento.";
            }

            int maxChars = llmConfig.getMaxPdfChars();
            if (pdfText.length() > maxChars) {
                pdfText = pdfText.substring(0, maxChars) + "\n\n[... texto truncado por tamaño ...]";
            }

            return callLlm(prompt, pdfText);
        } catch (Exception e) {
            log.error("Error analizando documento {}: {}", url, e.getMessage(), e);
            return "Error al analizar el documento: " + e.getMessage();
        }
    }

    private String downloadAndExtractText(String url) throws IOException, InterruptedException {
        log.info("Descargando PDF: {}", url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " descargando PDF");
        }

        byte[] pdfBytes = response.body();
        log.info("PDF descargado: {} bytes", pdfBytes.length);

        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    // ── Dispatcher ──

    private String callLlm(String systemPrompt, String documentText) {
        String provider = llmConfig.getProvider();
        return switch (provider) {
            case "claude" -> callClaude(systemPrompt, documentText);
            case "ollama" -> callOllama(systemPrompt, documentText);
            default -> throw new IllegalArgumentException("Provider LLM no soportado: " + provider);
        };
    }

    // ── Ollama ──

    private String callOllama(String systemPrompt, String documentText) {
        String model = llmConfig.getOllamaModel();
        log.info("Llamando a Ollama ({}) con {} caracteres de texto", model, documentText.length());

        try {
            String jsonBody = objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
                put("model", model);
                put("stream", false);
                put("options", java.util.Map.of("num_ctx", 8192));
                put("messages", java.util.List.of(
                        java.util.Map.of("role", "system", "content", systemPrompt),
                        java.util.Map.of("role", "user", "content", documentText)
                ));
            }});

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(llmConfig.getOllamaUrl() + "/api/chat"))
                    .timeout(Duration.ofMinutes(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Ollama HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("message").path("content").asText("");
            log.info("Respuesta Ollama recibida: {} caracteres", content.length());
            return content;

        } catch (Exception e) {
            log.error("Error llamando a Ollama: {}", e.getMessage(), e);
            throw new RuntimeException("Error llamando a Ollama: " + e.getMessage(), e);
        }
    }

    // ── Claude ──

    private String callClaude(String systemPrompt, String documentText) {
        log.info("Llamando a Claude ({}) con {} caracteres de texto", llmConfig.getClaudeModel(), documentText.length());

        AnthropicClient client = AnthropicOkHttpClient.builder()
                .apiKey(llmConfig.getClaudeApiKey())
                .build();

        Message message = client.messages().create(
                MessageCreateParams.builder()
                        .model(llmConfig.getClaudeModel())
                        .maxTokens(8192L)
                        .system(systemPrompt)
                        .addUserMessage(documentText)
                        .build()
        );

        StringBuilder result = new StringBuilder();
        for (ContentBlock block : message.content()) {
            try {
                result.append(block.asText().text());
            } catch (Exception ignored) {
                // Skip non-text blocks
            }
        }
        return result.toString();
    }

    // ── Prompts ──

    private String buildPcapPrompt() {
        return """
                Eres un experto en licitaciones públicas españolas. Analiza el siguiente Pliego de Cláusulas \
                Administrativas Particulares (PCAP) y extrae un resumen exhaustivo y estructurado.

                ## Criterios de Puntuación (indicar puntuación total)

                ### Juicio de valor
                Detalla TODOS los criterios evaluables mediante juicio de valor con su puntuación máxima. \
                Incluye subcriterios si existen, con sus puntos desglosados. Indica umbrales mínimos si los hay.

                ### Criterios por fórmula
                Detalla TODOS los criterios evaluables automáticamente:
                - **Oferta económica**: desglose por lotes/conceptos con puntuación de cada uno
                - **Experiencia y equipo**: puntuación por perfil, certificaciones valorables, experiencia extra
                - **Certificaciones de empresa**: lista cada certificación con sus puntos (ISO, ENS, CMMI, etc.)
                - **Otros**: presencialidad, soporte adicional, mejoras, etc.

                ## Solvencia Técnica y Profesional

                ### Habilitación obligatoria
                Certificaciones o habilitaciones obligatorias para licitar (ENS, etc.)

                ### Solvencia económica
                Facturación mínima exigida, seguros de responsabilidad civil, garantías.

                ### Solvencia técnica (experiencia empresa)
                Número de contratos similares, importes mínimos, tecnologías exigidas, años de experiencia, duración mínima.

                ### Requisitos del equipo
                Para CADA perfil requerido, presenta en formato tabla:
                | Perfil | Titulación mínima | Experiencia mínima |
                Incluye notas sobre equivalencias de titulación si las hay.

                ### Certificaciones valorables del equipo
                Certificaciones que puntúan extra (PMP, PRINCE2, ITIL, PMI-ACP, CSM, etc.) con sus puntos.

                ## Datos Económicos
                Presenta en tabla: presupuesto base por lotes/conceptos, total sin IVA, total con IVA, valor estimado.

                ### Duración
                Duración inicial del contrato y prórrogas posibles.

                ## Otros requisitos relevantes
                Garantías, seguros, clasificación empresarial, UTE, subcontratación, penalidades.

                INSTRUCCIONES DE FORMATO:
                - Responde en español
                - Usa Markdown con ## para secciones y ### para subsecciones
                - Usa tablas Markdown para perfiles y datos económicos
                - Usa listas con viñetas e indentación para desglose de criterios
                - Incluye TODAS las puntuaciones numéricas que aparezcan en el documento
                - Si algún apartado no aparece en el documento, indícalo como "No especificado en el documento"
                - Sé exhaustivo: no omitas ningún criterio, certificación o requisito""";
    }

    private String buildPptPrompt() {
        return """
                Eres un experto en licitaciones públicas españolas. Analiza el siguiente Pliego de Prescripciones \
                Técnicas (PPT) y extrae un resumen exhaustivo y estructurado.

                ## Resumen de Alcance
                Descripción del objeto del contrato y alcance funcional. Indica bloques o lotes si los hay.

                ## Tecnologías
                ### Stack actual (si se menciona migración)
                Presenta en tabla: | Componente | Versión actual | Objetivo |

                ### Integraciones y herramientas
                Lista TODAS las tecnologías, plataformas, lenguajes, frameworks, herramientas, \
                sistemas externos y protocolos mencionados (SAP, LDAP, APIs, Docker, Git, CI/CD, etc.)

                ### Módulos / Componentes
                Si se mencionan módulos, componentes o sistemas específicos, listarlos.

                ## Fases y Entregables
                Para cada bloque/fase del proyecto:
                - Identificador del hito (A.1, B.1, etc.)
                - Descripción
                - Porcentaje de pago o importe asociado si se indica

                ## Perfiles requeridos
                Si se mencionan perfiles profesionales, indicar requisitos de cada uno.

                ## Mantenimiento
                Si hay un bloque de mantenimiento:
                - Horas/año
                - Tipos (correctivo, adaptativo, perfectivo, evolutivo, preventivo)
                - Horario y modalidad
                - SLAs por criticidad: tabla con tiempos de respuesta y resolución
                - Clasificación de solicitudes por tamaño/esfuerzo

                ## Garantías
                Periodos de garantía por fase o tipo de trabajo.

                INSTRUCCIONES DE FORMATO:
                - Responde en español
                - Usa Markdown con ## para secciones y ### para subsecciones
                - Usa tablas Markdown donde sea apropiado (stack tecnológico, SLAs, perfiles)
                - Sé exhaustivo: no omitas tecnologías, fases ni requisitos
                - Si algún apartado no aparece en el documento, indícalo como "No especificado en el documento" """;
    }
}
