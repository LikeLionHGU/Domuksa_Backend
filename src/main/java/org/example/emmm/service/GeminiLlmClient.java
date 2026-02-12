package org.example.emmm.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.example.emmm.util.LlmClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service // ✅ Bean 등록
@RequiredArgsConstructor
public class GeminiLlmClient implements LlmClient {

    private final WebClient geminiWebClient;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    @Override
    public String summarizeText(String prompt) {
        // Gemini generateContent 요청 형식: contents -> parts(text)
        // 공식 문서: generateContent :contentReference[oaicite:2]{index=2}
        GenerateContentRequest req = GenerateContentRequest.builder()
                .contents(List.of(
                        Content.builder()
                                .parts(List.of(Part.builder().text(prompt).build()))
                                .build()
                ))
                .build();

        GenerateContentResponse res = geminiWebClient.post()
                .uri("/v1beta/models/{model}:generateContent", model)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(GenerateContentResponse.class)
                .block();

        if (res == null) throw new IllegalStateException("Gemini 응답이 null 입니다.");
        String text = res.extractFirstText();
        if (text == null || text.isBlank()) throw new IllegalStateException("Gemini가 요약 텍스트를 반환하지 않았습니다.");
        return text.trim();
    }

    // ---------- Request DTO ----------
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

    // ---------- Response DTO ----------
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
