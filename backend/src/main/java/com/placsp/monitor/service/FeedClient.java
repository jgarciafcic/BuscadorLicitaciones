package com.placsp.monitor.service;

import com.placsp.monitor.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class FeedClient {

    private static final Logger log = LoggerFactory.getLogger(FeedClient.class);

    private final AppConfig appConfig;
    private final HttpClient httpClient;

    public FeedClient(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Descarga la URL y devuelve el InputStream del cuerpo directamente,
     * sin bufferizar en memoria. El caller es responsable de cerrar el stream.
     */
    public InputStream download(String url) throws IOException, InterruptedException {
        log.info("Descargando feed: {}", url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(120))
                .header("Accept", "application/atom+xml, application/xml, text/xml")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            response.body().close();
            throw new IOException("HTTP " + response.statusCode() + " al descargar " + url);
        }

        log.info("Conexión establecida con {}", url);
        return response.body();
    }

    public String getStartUrl() {
        return appConfig.getUrl();
    }
}
