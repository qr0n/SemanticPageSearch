package com.sitewatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configuration for HTTP client used by crawlers.
 */
@Configuration
public class HttpClientConfig {

    /**
     * Creates a configured HttpClient bean for making HTTP requests.
     *
     * @return configured HttpClient instance
     */
    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }
}
