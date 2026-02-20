package org.example.emmm.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.example.emmm.util.LlmClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiLlmClient implements LlmClient {

    private final WebClient geminiWebClient;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    // ✅ 인터페이스에서 요구하는 메서드 구현
    @Override
    public String generateText(String prompt) {
        GenerateContentRequest req = GenerateContentRequest.builder()
                .contents(List.of(Content.builder()
                        .parts(List.of(Part.builder().text(prompt).build()))
                        .build()))
                .build();

        String modelId = normalizeModelId(model);

        try {
            GenerateContentResponse res = geminiWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/{model}:generateContent")
                            .build(modelId))
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(GenerateContentResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(10))
                            .filter(t -> t instanceof WebClientResponseException.TooManyRequests))
                    .block();

            String text = (res == null) ? null : res.extractFirstText();
            if (text == null || text.isBlank()) return "요약본 생성에 실패했습니다.";
            return text.trim();

        } catch (WebClientResponseException e) {
            int status = e.getStatusCode().value();
            log.error("❌ Gemini API error. status={} body={}",
                    status, e.getResponseBodyAsString(), e);
            if (e instanceof WebClientResponseException.NotFound) {
                return "404 에러: 모델 경로가 잘못되었습니다.";
            }
            if (e instanceof WebClientResponseException.TooManyRequests) {
                return "429 에러: 잠시 후 다시 시도하세요.";
            }
            return "서비스 오류 발생: " + e.getMessage();
        } catch (Exception e) {
            log.error("❌ 일반 에러: {}", e.getMessage(), e);
            return "서비스 오류 발생: " + e.getMessage();
        }
    }

    private String normalizeModelId(String m) {
        if (m == null) return "";
        String s = m.trim();
        if (s.startsWith("models/")) s = s.substring("models/".length());
        return s;
    }

    // ---------- DTO 구조 ----------
    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class GenerateContentRequest {
        private List<Content> contents;
    }

    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class Part {
        private String text;
    }

    @Getter @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GenerateContentResponse {
        private List<Candidate> candidates;

        @Getter @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Candidate {
            private Content content;
        }

        @Getter @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Content {
            private List<Part> parts;
        }

        @Getter @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Part {
            private String text;
        }

        public String extractFirstText() {
            if (candidates == null || candidates.isEmpty()) return null;
            Candidate c = candidates.get(0);
            if (c == null || c.content == null || c.content.parts == null) return null;
            for (Part p : c.content.parts) {
                if (p != null && p.text != null && !p.text.isBlank()) return p.text;
            }
            return null;
        }
    }
}
