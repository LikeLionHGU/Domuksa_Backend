package org.example.emmm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.emmm.domain.Room;

import java.util.List;

public class UserDto {
    @Getter
    public static class GoogleLoginReqDto {
        @NotBlank
        private String code;   // 프론트 Loading에서 추출한 code
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String profileUrl;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GoogleLoginResDto {
         private String accessToken; // JWT
        private UserInfo user;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailResDto {
        private List<Room> rooms;
    }

}
