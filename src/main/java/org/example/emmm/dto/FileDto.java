package org.example.emmm.dto;

import lombok.*;
import org.example.emmm.domain.Room;
import org.example.emmm.domain.UserRoom;
import org.example.emmm.domain.File;

public class FileDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class CreateFileResDto{//post
        private Long fileId;
        private Long agendaId;
        private String fileName;
        private String fileUrl;
        private String s3Key;

        public static FileDto.CreateFileResDto from(File file){
            return builder()
                    .fileId(file.getFileId())
                    .agendaId(file.getAgendaId())
                    .fileName(file.getFileName())
                    .fileUrl(file.getFileUrl())
                    .s3Key(file.getS3Key())
                    .build();
        }
    }
    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class FileListResDto{//get
        private Long id;
        private String fileName;
        private String fileUrl;
        public static FileDto.FileListResDto from(File file){
            return builder()
                    .id(file.getFileId())
                    .fileName(file.getFileName())
                    .fileUrl(file.getFileUrl())
                    .build();

        }
    }

}
