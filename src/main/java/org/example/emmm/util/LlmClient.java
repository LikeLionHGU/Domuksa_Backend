package org.example.emmm.util;

public interface LlmClient {
    String generateText(String prompt);

    default String summarizeText(String prompt) {
        return generateText(prompt);
    }
}

