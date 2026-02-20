package org.example.emmm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.emmm.domain.Timer;

public class TimerDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DetailTimerResDto {
        private Long timerId;
        private Long roomId;
        private Long time;
        private String state;

        public static DetailTimerResDto from(Timer t) {
            return builder()
                    .timerId(t.getId())
                    .roomId(t.getRoom().getId())
                    .time(t.getTime())
                    .state(t.getStatus())
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateTimeReqDto {
        private Long time;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UpdateTimeResDto {
        private Long timerId;
        private Long roomId;
        private Long time;
        private String state;

        public static UpdateTimeResDto from(Timer t) {
            return builder()
                    .timerId(t.getId())
                    .roomId(t.getRoom().getId())
                    .time(t.getTime())
                    .state(t.getStatus())
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateStateReqDto {
        private String state;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UpdateStateResDto {
        private Long timerId;
        private Long roomId;
        private Long time;
        private String state;

        public static UpdateStateResDto from(Timer t) {
            return builder()
                    .timerId(t.getId())
                    .roomId(t.getRoom().getId())
                    .time(t.getTime())
                    .state(t.getStatus())
                    .build();
        }
    }

}
