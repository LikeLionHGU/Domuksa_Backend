package org.example.emmm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.emmm.domain.Ai;

public class AiDto {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreateAiResDto {
        private Long id;
        private Long agendaId;
        private String summaryText;

        public static CreateAiResDto from(Ai ai) {
            return builder()
                    .id(ai.getId())
                    .agendaId(ai.getAgenda().getId())
                    .summaryText(ai.getSummaryText())
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DetailAiResDto {
        private Long id;
        private Long agendaId;
        private String summaryText;

        public static DetailAiResDto from(Ai ai) {
            return builder()
                    .id(ai.getId())
                    .agendaId(ai.getAgenda().getId())
                    .summaryText(ai.getSummaryText())
                    .build();
        }
    }
}
