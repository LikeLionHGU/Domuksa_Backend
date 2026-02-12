package org.example.emmm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.emmm.domain.AgendaConfig;
import org.example.emmm.domain.Vote;

public class VoteDto {
    @Getter
    public static class CreateVoteReqDto{
        private String title;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateVoteResDto{
        private VoteDto.VoteBlock vote;
        private VoteDto.ConfigBlock config;

        public static VoteDto.CreateVoteResDto from(Vote vote, AgendaConfig config) {
            return new VoteDto.CreateVoteResDto(
                    VoteDto.VoteBlock.from(vote), VoteDto.ConfigBlock.from(config)
            );
        }
    }
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DetailVoteResDto{
        private Long VoteId;
        private Long agendaId;
        private String title;

        public static VoteDto.DetailVoteResDto from(Vote vote) {
            return builder()
                    .VoteId(vote.getId())
                    .agendaId(vote.getAgenda().getId())
                    .title(vote.getTitle())
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VoteBlock {
        private Long voteId;
        private Long agendaId;
        private String title;

        public static VoteDto.VoteBlock from(Vote v) {
            return new VoteDto.VoteBlock(
                    v.getId(),
                    v.getAgenda().getId(),
                    v.getTitle()
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

        public static VoteDto.ConfigBlock from(AgendaConfig config) {
            return new VoteDto.ConfigBlock(
                    config.getAgenda().getId(),
                    config.getVoteEnabled(),
                    config.getCommentEnabled(),
                    config.getFileEnabled(),
                    config.getAiSummaryEnabled()
            );
        }
    }
}
