package org.example.emmm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.emmm.domain.*;

import java.util.List;

public class CommentOptionDto {
    @Getter
    public static class CreateCommentOptionReqDto{
        private String content;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreateCommentOptionResDto{
        private CommentOptionBlock commentOptionBlock;

        public static CreateCommentOptionResDto from(CommentOption co){
            return new CreateCommentOptionResDto(
                    CommentOptionBlock.from(co)
            );
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DetailCommentOptionResDto {
        private CommentDto.CommentBlock commentBlock;
        private List<CommentOptionBlock> commentOptionBlocks;

        public static DetailCommentOptionResDto from(Comment c, List<CommentOption> cos) {
            return new DetailCommentOptionResDto(
                    CommentDto.CommentBlock.from(c),
                    cos.stream()
                            .map(CommentOptionDto.CommentOptionBlock::from)
                            .toList()
            );
        }
    }

    @Getter
    public static class UpdateCommentOptionReqDto{
        private String content;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UpdateCommentOptionResDto{
        private CommentOptionBlock commentOptionBlock;

        public static UpdateCommentOptionResDto from(CommentOption co){
            return new UpdateCommentOptionResDto(
                    CommentOptionBlock.from(co)
            );
        }
    }


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentOptionBlock {
        private Long commentOptionId;
        private Long commentId;
        private String content;

        public static CommentOptionBlock from(CommentOption co) {
            return new CommentOptionBlock(
                    co.getId(),
                    co.getComment().getId(),
                    co.getContent()
            );
        }
    }

}
