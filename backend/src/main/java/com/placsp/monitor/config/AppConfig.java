package com.placsp.monitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "placsp.feed")
public class AppConfig {

    private String url;
    private int maxPages;
    private String cron;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }
}
