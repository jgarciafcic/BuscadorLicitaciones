package com.placsp.monitor.service;

import com.placsp.monitor.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public InputStream download(String url) throws IOException, InterruptedException {
        log.info("Descargando feed: {}", url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Accept", "application/atom+xml, application/xml, text/xml")
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " al descargar " + url);
        }

        log.info("Descargados {} bytes desde {}", response.body().length, url);
        return new ByteArrayInputStream(response.body());
    }

    public List<InputStream> downloadPages() throws IOException, InterruptedException {
        List<InputStream> pages = new ArrayList<>();
        String url = appConfig.getUrl();
        int maxPages = appConfig.getMaxPages();

        for (int page = 0; page < maxPages && url != null; page++) {
            byte[] body = downloadAsBytes(url);
            pages.add(new ByteArrayInputStream(body));

            Optional<String> nextUrl = extractNextLink(new ByteArrayInputStream(body));
            if (nextUrl.isPresent()) {
                url = nextUrl.get();
                log.debug("Siguiente página: {}", url);
            } else {
                url = null;
                log.info("No hay más páginas tras la página {}", page + 1);
            }
        }

        log.info("Descargadas {} páginas del feed", pages.size());
        return pages;
    }

    private byte[] downloadAsBytes(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Accept", "application/atom+xml, application/xml, text/xml")
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " al descargar " + url);
        }

        log.info("Descargados {} bytes desde {}", response.body().length, url);
        return response.body();
    }

    static Optional<String> extractNextLink(InputStream xml) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        try {
            XMLStreamReader reader = factory.createXMLStreamReader(xml);
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && "link".equals(reader.getLocalName())) {
                    String rel = reader.getAttributeValue(null, "rel");
                    if ("next".equals(rel)) {
                        String href = reader.getAttributeValue(null, "href");
                        reader.close();
                        return Optional.ofNullable(href);
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException e) {
            LoggerFactory.getLogger(FeedClient.class).warn("Error extrayendo link next: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
