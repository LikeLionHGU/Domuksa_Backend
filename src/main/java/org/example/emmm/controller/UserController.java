package org.example.emmm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.User;
import org.example.emmm.dto.UserDto;
import org.example.emmm.repository.UserRepository;
import org.example.emmm.security.AuthService;
import org.example.emmm.security.GoogleTokenExchangeService;
import org.example.emmm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final GoogleTokenExchangeService googleTokenExchangeService;
    private final UserService userService;
    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * 프론트 방식 유지:
     * - 프론트가 code를 보냄
     * - 백엔드가 code -> id_token 교환
     * - id_token 검증 및 유저 생성/조회
     * - JWT 발급 후 반환
     */
    @PostMapping("/google")
    public ResponseEntity<UserDto.GoogleLoginResDto> googleLogin(@Valid @RequestBody UserDto.GoogleLoginReqDto req) {

        // 1) code -> id_token
        String idToken = googleTokenExchangeService.exchangeCodeForIdToken(req.getCode());

        // 2) id_token -> user
        User u = userService.loginOrCreateByGoogleIdToken(idToken);

        // 3) JWT 발급
        String accessToken = authService.createAccessToken(u.getId());

        // 4) 응답(명세서)
        UserDto.UserInfo userInfo =
                new UserDto.UserInfo(u.getId(), u.getName(), u.getEmail(), u.getProfileUrl());

        return ResponseEntity.ok(new UserDto.GoogleLoginResDto(accessToken, userInfo));
    }

    /**
     * Authorization: Bearer {accessToken}
     * -> 토큰 검증 -> user 반환
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto.UserInfo> me(
            @RequestHeader(value = "Authorization", required = false) String auth
    ) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = auth.substring("Bearer ".length());
        Long userId;
        try {
            userId = authService.verifyAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        return ResponseEntity.ok(new UserDto.UserInfo(u.getId(), u.getName(), u.getEmail(), u.getProfileUrl()));
    }

    /**
     * 최소 로그아웃(프론트에서 토큰 삭제하면 사실상 로그아웃)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().body("{\"text\":\"로그아웃 되었습니다.\"}");
    }
}

