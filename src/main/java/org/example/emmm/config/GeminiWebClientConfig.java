package org.example.emmm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiWebClientConfig {

    @Bean
    public WebClient geminiWebClient(
            @Value("${gemini.base-url}") String baseUrl,
            @Value("${gemini.api-key}") String apiKey
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // ✅ Gemini 인증 헤더
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }
}
