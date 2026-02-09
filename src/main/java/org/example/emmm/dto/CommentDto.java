package org.example.emmm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.emmm.domain.AgendaConfig;
import org.example.emmm.domain.Comment;

public class CommentDto {
    @Getter
    public static class CreateCommentReqDto{
        private String title;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class CreateCommentResDto{
        private CommentBlock commentBlock;
        private ConfigBlock configBlock;

        public static CreateCommentResDto from(Comment comment, AgendaConfig agendaConfig){
            return new CreateCommentResDto(
                    CommentBlock.from(comment),
                    ConfigBlock.from(agendaConfig)
            );
        }
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class DetailCommentResDto{
        private Long commentId;
        private String title;

        public static DetailCommentResDto from(Comment comment){
            return builder()
                    .commentId(comment.getId())
                    .title(comment.getTitle())
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class CommentBlock {
        private Long commentId;
        private Long agendaId;
        private String title;

        public static CommentBlock from(Comment comment) {
            return new CommentBlock(
                    comment.getId(),
                    comment.getAgenda().getId(),
                    comment.getTitle()
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
        private boolean aiSummaryEnabled;

        public static CommentDto.ConfigBlock from(AgendaConfig config) {
            return new CommentDto.ConfigBlock(
                    config.getAgenda().getId(),
                    config.isVoteEnabled(),
                    config.isCommentEnabled(),
                    config.isFileEnabled(),
                    config.isAiSummaryEnabled()
            );
        }
    }
}
