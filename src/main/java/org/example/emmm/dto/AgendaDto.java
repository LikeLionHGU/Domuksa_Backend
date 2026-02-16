package org.example.emmm.dto;

import lombok.*;
import org.example.emmm.domain.Agenda;
import org.example.emmm.domain.AgendaConfig;

import java.time.LocalDateTime;

public class AgendaDto {
    @Getter
    public static class CreateAgendaReqDto {
        private String name;
        private int sequence;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateAgendaResDto {
        private AgendaBlock agenda;
        private ConfigBlock config;

        public static CreateAgendaResDto from(Agenda agenda, AgendaConfig config) {
            return new CreateAgendaResDto(
                AgendaBlock.from(agenda), ConfigBlock.from(config)
            );
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailAgendaResDto {
        private AgendaBlock agenda;
        private ConfigBlock config;
        private int currentAgendaSequence;

        public static DetailAgendaResDto from(Agenda agenda, AgendaConfig config, int currentAgendaSequence) {
            return new DetailAgendaResDto(
                    AgendaBlock.from(agenda), ConfigBlock.from(config), currentAgendaSequence
            );
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailListAgendaResDto {
        private AgendaBlock agenda;
        private ConfigBlock config;

        public static DetailListAgendaResDto from(Agenda agenda, AgendaConfig config) {
            return new DetailListAgendaResDto(
                    AgendaBlock.from(agenda), ConfigBlock.from(config)
            );
        }
    }

    @Getter
    @Setter
    public static class UpdateAgendaReqDto {
        private String name;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UpdateAgendaResDto {
        private Long agendaId;
        private String name;

        public static UpdateAgendaResDto from(Agenda agenda) {
            return AgendaDto.UpdateAgendaResDto.builder()
                    .agendaId(agenda.getId())
                    .name(agenda.getName())
                    .build();
        }
    }

    @Getter
    @Setter
    public static class UpdateConfigReqDto {
        private boolean voteEnabled;
        private boolean commentEnabled;
        private boolean fileEnabled;
        private boolean aiSummaryEnabled;

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UpdateConfigResDto {
        private Long agendaId;
        private LocalDateTime modifiedAt;
        private boolean voteEnabled;
        private boolean commentEnabled;
        private boolean fileEnabled;
        private boolean aiSummaryEnabled;

        public static UpdateConfigResDto from(Agenda agenda, AgendaConfig config) {
            return UpdateConfigResDto.builder()
                    .agendaId(agenda.getId())
                    .modifiedAt(LocalDateTime.now())
                    .voteEnabled(config.getVoteEnabled())
                    .commentEnabled(config.getCommentEnabled())
                    .fileEnabled(config.getFileEnabled())
                    .aiSummaryEnabled(config.getAiSummaryEnabled())
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AgendaBlock {
        private Long agendaId;
        private Long roomId;
        private String name;
        private int sequence;

        public static AgendaBlock from(Agenda agenda) {
            return new AgendaBlock(
                    agenda.getId(),
                    agenda.getRoom().getId(),
                    agenda.getName(),
                    agenda.getSequence()
            );
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConfigBlock {
        private Long agendaId;
        private boolean voteEnabled;
        private boolean commentEnabled;
        private boolean fileEnabled;
        private boolean aiSummaryEnabled;

        public static ConfigBlock from(AgendaConfig config) {
            return new ConfigBlock(
                    config.getAgenda().getId(),
                    config.getVoteEnabled(),
                    config.getCommentEnabled(),
                    config.getFileEnabled(),
                    config.getAiSummaryEnabled()
            );
        }
    }
}
