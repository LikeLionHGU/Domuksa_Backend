package org.example.emmm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    boolean deleted;

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

    @ManyToOne(fetch = FetchType.LAZY)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String role;
    private String state;

}

