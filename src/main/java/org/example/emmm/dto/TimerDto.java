package org.example.emmm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.emmm.domain.Timer;
import org.example.emmm.domain.TimerStatus;

public class TimerDto {
    @Getter
    @NoArgsConstructor
    public static class TimerReqDto {

        @NotNull
        @Min(1)
        private Long totalSeconds;
    }

    @Getter
    @Builder
    public static class TimerResDto {
        private Long roomId;
        private TimerStatus status;
        private Long totalSeconds;
        private Long remainingSeconds;
        private Long endAtEpochMs;
        private Long serverNowEpochMs;
        private Long version;

        public static TimerResDto from(Timer timer, long nowEpochMs) {
            long remaining;
            if (timer.getStatus() == TimerStatus.RUNNING && timer.getEndAtEpochMs() != null) {
                remaining = Timer.calcRemainingSeconds(timer.getEndAtEpochMs(), nowEpochMs);
            } else {
                remaining = timer.getRemainingSeconds() == null ? 0L : timer.getRemainingSeconds();
            }

            return TimerResDto.builder()
                    .roomId(timer.getRoom().getId())
                    .status(timer.getStatus())
                    .totalSeconds(timer.getTotalSeconds())
                    .remainingSeconds(remaining)
                    .endAtEpochMs(timer.getEndAtEpochMs())
                    .serverNowEpochMs(nowEpochMs)
                    .version(timer.getVersion())
                    .build();
        }
    }

}
