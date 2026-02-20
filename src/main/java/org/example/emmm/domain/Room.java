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
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Room {

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

    @PrePersist
    public void onPrePersist() {
        if (this.deleted == null) this.deleted = false;
    }

    private String roomName;
    private String password;
    private Boolean isPassword;
    private int currentAgendaSequence;

    @Column(length = 10, nullable = false, unique = true)
    private String code;

    private String state;
    private Long currentAgendaId;

    // ✅ Room은 역방향(mappedBy). Room 테이블에 timer_id 컬럼이 생기면 안 됨
    @OneToOne(mappedBy = "room", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Timer timer;

    // (선택) 양방향 세팅 편의 메서드
    public void attachTimer(Timer timer) {
        this.timer = timer;
        if (timer != null) timer.setRoom(this);
    }
}
