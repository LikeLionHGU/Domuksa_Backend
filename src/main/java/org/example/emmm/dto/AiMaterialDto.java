package org.example.emmm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class AiMaterialDto {

    @Getter
    @AllArgsConstructor
    public static class VoteOptionItem {
        private String contents;
        private Integer selectCount;
    }

    @Getter @AllArgsConstructor
    public static class VoteItem {
        private String title;
        private String voteStatus;
        private List<VoteOptionItem> options;
    }

    @Getter @AllArgsConstructor
    public static class CommentItem {
        private String title; // Comment.title (안건 토론 주제 같은 느낌이면)
        private List<String> contents; // CommentOption.contents 전부
    }

    @Getter @AllArgsConstructor
    public static class FileItem {
        private String fileName;
        private String fileUrl;
    }

    @Getter @AllArgsConstructor
    public static class AgendaMaterials {
        private Long agendaId;
        private String agendaName;
        private Integer sequence;

        private VoteItem vote;                 // 없으면 null
        private List<CommentItem> comments;    // 없으면 빈 리스트
        private List<FileItem> files;          // 없으면 빈 리스트

        // (확장) 파일에서 추출한 텍스트들
        private List<String> fileExtractedTexts;
    }
}
