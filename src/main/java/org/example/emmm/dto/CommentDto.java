package org.example.emmm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.emmm.domain.*;

import java.time.LocalDateTime;


public class CommentDto {
    @Getter
    public static class CreateCommentReqDto{
        private String content;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreateCommentResDto{
        private Long commentId;
        private String content;
        private LocalDateTime createdAt;

        public static CreateCommentResDto from(Comment c){
            return builder()
                    .commentId(c.getId())
                    .content(c.getContent())
                    .createdAt(c.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DetailCommentResDto {
        private Long commentId;
        private String content;
        private LocalDateTime createdAt;

        public static DetailCommentResDto from(Comment c){
            return builder()
                    .commentId(c.getId())
                    .content(c.getContent())
                    .createdAt(c.getCreatedAt())
                    .build();
        }
    }

    @Getter
    public static class UpdateCommentReqDto{
        private String content;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UpdateCommentResDto{
        private Long commentId;
        private String content;
        private LocalDateTime createdAt;

        public static UpdateCommentResDto from(Comment c){
            return builder()
                    .commentId(c.getId())
                    .content(c.getContent())
                    .createdAt(c.getCreatedAt())
                    .build();
        }
    }

}
