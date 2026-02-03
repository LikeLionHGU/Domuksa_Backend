package org.example.emmm.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User{
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

    private String name;
    private String email;
    private String googleSub;
    private String profileUrl;

    public void updateName(String name) {
        this.name = name;
    }

    //  구글 로그인 시 사용
    public void updateOAuthProfile(String name) {
        this.name = name;
    }

    public void connectGoogle(String googleSub) {
        this.googleSub = googleSub;
    }
}
