package org.example.emmm.dto;

import lombok.*;
import org.example.emmm.domain.Room;
import org.example.emmm.domain.UserRoom;

public class RoomDto {
    @Getter
    public static class CreateReqDto {
        private String roomName;
        private String password;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CreateResDto {
        private Long roomId;
        private String code;
        private String roomName;
    }

    @Getter
    public static class ParticipateCreateReqDto {
        private String code;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ParticipateCreateResDto {
        private Long roomId;
        private Long userRoomId;
        private String role;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class DetailResDto {
        private Long roomId;
        private String roomName;
        private String code;
        private String state;
        private String role;

        public static RoomDto.DetailResDto from(Room room, UserRoom userRoom) {
            return builder()
                    .roomId(room.getId())
                    .roomName(room.getRoomName())
                    .code(room.getCode())
                    .state(room.getState())
                    .role(userRoom.getRole())
                    .build();
        }

    }
}
