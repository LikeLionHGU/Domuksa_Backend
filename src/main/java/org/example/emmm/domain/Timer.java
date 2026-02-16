package org.example.emmm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Timer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean deleted;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @CreatedDate
    private LocalDateTime createdAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TimerStatus status;

    private Long totalSeconds;

    private Long remainingSeconds;

    // RUNNING 상태에서만 의미 있음 (끝나는 시각, epoch ms)
    private Long endAtEpochMs;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, unique = true)
    private Room room;



    @Builder
    private Timer(Room room) {
        this.room = room;
    }

    @PrePersist
    public void onPrePersist() {
        if (this.deleted == null) this.deleted = false;
        if (this.status == null) this.status = TimerStatus.STOPPED;
        if (this.totalSeconds == null) this.totalSeconds = 0L;
        if (this.remainingSeconds == null) this.remainingSeconds = this.totalSeconds;
        if (this.version == null) this.version = 0L;
    }

    /** SET: 총 시간 설정(보통 RUNNING일 때는 서비스에서 막는 걸 추천) */
    public void setTotalSeconds(long totalSeconds) {
        this.totalSeconds = totalSeconds;
        this.remainingSeconds = totalSeconds;
        this.endAtEpochMs = null;
        this.status = TimerStatus.STOPPED;
    }

    /** START or RESUME */
    public void start(long nowEpochMs) {
        // 서비스에서 상태 체크(예: RUNNING이면 409) 하는 걸 추천
        this.endAtEpochMs = nowEpochMs + this.remainingSeconds * 1000L;
        this.status = TimerStatus.RUNNING;
    }

    /** PAUSE */
    public void pause(long nowEpochMs) {
        if (this.endAtEpochMs == null) {
            // RUNNING이 아닌데 pause가 온 상황은 서비스에서 막는 게 정석
            this.status = TimerStatus.PAUSED;
            return;
        }
        long rem = calcRemainingSeconds(this.endAtEpochMs, nowEpochMs);
        this.remainingSeconds = rem;
        this.endAtEpochMs = null;
        this.status = TimerStatus.PAUSED;
    }

    /** RESET */
    public void reset() {
        this.remainingSeconds = this.totalSeconds;
        this.endAtEpochMs = null;
        this.status = TimerStatus.STOPPED;
    }

    /** 서버/프론트 공통 규칙: endAt 기반 남은 시간 계산 */
    public static long calcRemainingSeconds(long endAtEpochMs, long nowEpochMs) {
        long diffMs = endAtEpochMs - nowEpochMs;
        if (diffMs <= 0) return 0L;
        // ceil(diff/1000)
        return (diffMs + 999L) / 1000L;
    }
}
