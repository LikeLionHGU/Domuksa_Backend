package org.example.emmm.dto;

import lombok.*;
import org.example.emmm.domain.Room;
import org.example.emmm.domain.UserRoom;

public class RoomDto {
    @Getter
    public static class CreateRoomReqDto {
        private String roomName;
        private String password;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateRoomResDto {
        private Long roomId;
        private String code;
        private String roomName;
    }

    @Getter
    public static class ParticipatePasswordCreateReqDto {
        private String password;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParticipatePasswordCreateResDto {
        private Long roomId;
        private Long userRoomId;
        private String role;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParticipateCreateResDto {
        private Long roomId;
        private Long userRoomId;
        private String role;
    }


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DetailRoomResDto {
        private Long roomId;
        private String roomName;
        private String code;
        private String state;
        private int currentAgendaSequence;
        private String role;

        public static RoomDto.DetailRoomResDto from(Room room, UserRoom userRoom) {
            return builder()
                    .roomId(room.getId())
                    .roomName(room.getRoomName())
                    .code(room.getCode())
                    .state(room.getState())
                    .currentAgendaSequence(room.getCurrentAgendaSequence())
                    .role(userRoom.getRole())
                    .build();
        }
    }

    @Getter
    public static class CodeReqDto {
        private String code;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CodeResDto {
        private Long roomId;
        private boolean isPassword;

        public static CodeResDto from(Room room) {
            return CodeResDto.builder()
                    .roomId(room.getId())
                    .isPassword(room.getIsPassword())
                    .build();
        }

    }

    @Getter
    @Setter
    public static class UpdateRoomNameReqDto {
        private String name;
        private String password;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UpdateRoomNameResDto {
        private Long roomId;
        private String roomName;
        private String password;

        public static UpdateRoomNameResDto from(Room room) {
            return UpdateRoomNameResDto.builder()
                    .roomId(room.getId())
                    .roomName(room.getRoomName())
                    .password(room.getPassword())
                    .build();
        }

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoomStateChangedMessage {
        private Long roomId;
        private String state;
    }

}
