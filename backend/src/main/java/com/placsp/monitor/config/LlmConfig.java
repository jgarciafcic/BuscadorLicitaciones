package com.placsp.monitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "placsp.llm")
public class LlmConfig {

    private String claudeApiKey;
    private String claudeModel = "claude-sonnet-4-20250514";
    private int maxPdfChars = 80000;

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
