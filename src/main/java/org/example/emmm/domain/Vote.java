package org.example.emmm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vote{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Boolean deleted;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @CreatedDate
    LocalDateTime createdAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @LastModifiedDate
    LocalDateTime modifiedAt;

    @PrePersist
    public void onPrePersist() {
        this.deleted = false;
    }

    private String title;

    //확정(완료)된 투표인지 아닌지 상태를 저장
    private String voteStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private Agenda agenda;

}
