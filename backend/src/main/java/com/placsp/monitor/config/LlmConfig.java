package com.placsp.monitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "placsp.llm")
public class LlmConfig {

    private String provider = "ollama"; // "ollama" o "claude"

    // Ollama
    private String ollamaUrl = "http://localhost:11434";
    private String ollamaModel = "mistral";

    // Claude
    private String claudeApiKey;
    private String claudeModel = "claude-sonnet-4-20250514";

    // Común
    private int maxPdfChars = 80000;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getOllamaUrl() {
        return ollamaUrl;
    }

    public void setOllamaUrl(String ollamaUrl) {
        this.ollamaUrl = ollamaUrl;
    }

    public String getOllamaModel() {
        return ollamaModel;
    }

    public void setOllamaModel(String ollamaModel) {
        this.ollamaModel = ollamaModel;
    }

    public String getClaudeApiKey() {
        return claudeApiKey;
    }

    public void setClaudeApiKey(String claudeApiKey) {
        this.claudeApiKey = claudeApiKey;
    }

    public String getClaudeModel() {
        return claudeModel;
    }

    public void setClaudeModel(String claudeModel) {
        this.claudeModel = claudeModel;
    }

    public int getMaxPdfChars() {
        return maxPdfChars;
    }

    public void setMaxPdfChars(int maxPdfChars) {
        this.maxPdfChars = maxPdfChars;
    }
}
