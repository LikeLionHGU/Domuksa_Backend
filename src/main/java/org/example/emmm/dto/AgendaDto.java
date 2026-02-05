package org.example.emmm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.emmm.domain.Agenda;
import org.example.emmm.domain.AgendaConfig;

public class AgendaDto {
    @Getter
    public static class CreateAgendaReqDto {
        private String name;
        private int sequence;
    }

    @Getter
    @AllArgsConstructor
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
    public static class AgendaBlock {
        private Long id;
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
    public static class ConfigBlock {
        private Long agendaId;
        private boolean voteEnabled;
        private boolean commentEnabled;
        private boolean fileEnabled;

        public static ConfigBlock from(AgendaConfig config) {
            return new ConfigBlock(
                    config.getAgenda().getId(),
                    config.isVoteEnabled(),
                    config.isCommentEnabled(),
                    config.isFileEnabled()
            );
        }
    }
}
