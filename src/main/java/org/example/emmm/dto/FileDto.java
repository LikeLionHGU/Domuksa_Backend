package org.example.emmm.dto;

import lombok.*;
import org.example.emmm.domain.Room;
import org.example.emmm.domain.UserRoom;
import org.example.emmm.domain.File;

public class FileDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreateFileResDto{//post
        private Long roomId;
        private Long fileId;
        private Long agendaId;
        private String fileName;
        private String fileUrl;
        private Boolean isPdf;
        private String s3Key;

        public static FileDto.CreateFileResDto from(File file){
            return builder()
                    .roomId(file.getAgenda().getRoom().getId())
                    .fileId(file.getId())
                    .agendaId(file.getAgenda().getId())
                    .fileName(file.getFileName())
                    .fileUrl(file.getFileUrl())
                    .s3Key(file.getS3Key())
                    .isPdf(file.getIsPdf())
                    .build();
        }
    }
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FileListResDto{//get
        private Long fileId;
        private String fileName;
        private String fileUrl;
        private Boolean isPdf;

        public static FileDto.FileListResDto from(File file){
            return builder()
                    .fileId(file.getId())
                    .fileName(file.getFileName())
                    .fileUrl(file.getFileUrl())
                    .isPdf(file.getIsPdf())
                    .build();

        }
    }

}
