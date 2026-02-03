package org.example.emmm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class UserDto {
    @Getter
    public static class GoogleLoginReqDto {
        @NotBlank
        private String code;   // 프론트 Loading에서 추출한 code
    }

    @Getter
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String profileUrl;
    }

    @Getter
    @AllArgsConstructor
    public static class GoogleLoginResDto {
         private String accessToken; // JWT
        private UserInfo user;
    }

}
