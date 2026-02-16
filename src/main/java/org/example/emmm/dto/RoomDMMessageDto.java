package org.example.emmm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.emmm.domain.RoomDMMessage;

import java.time.LocalDateTime;
import java.util.List;

public class RoomDMMessageDto {

    @Getter
    @AllArgsConstructor
    @Builder
    public static class CreateDmReqDto{//post
        private Long userRoomId;
        private String content;

    }
    @Getter
    @AllArgsConstructor
    @Builder
    public static class CreateDmResDto {
        private Message message;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class DetailDmResDto{//get
        private Long roomId;
        private List<Message> messages;

    }


    @Getter
    @AllArgsConstructor
    @Builder
    public static class Message{
        private Long id;
        private Long userRoomId;
        private String content;
        private LocalDateTime createdAt;

        public static Message from(RoomDMMessage roomDMMessage){
            return Message.builder()
                    .id(roomDMMessage.getId())
                    .userRoomId(roomDMMessage.getUserRoom().getId())
                    .content(roomDMMessage.getContent())
                    .createdAt(roomDMMessage.getCreatedAt())
                    .build();

        }
    }



}
