package org.example.emmm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.User;
import org.example.emmm.dto.RoomDto;
import org.example.emmm.dto.UserDto;
import org.example.emmm.repository.UserRepository;
import org.example.emmm.security.AuthService;
import org.example.emmm.security.GoogleTokenExchangeService;
import org.example.emmm.security.UserPrincipal;
import org.example.emmm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final GoogleTokenExchangeService googleTokenExchangeService;
    private final UserService userService;
    private final AuthService authService;
    private final UserRepository userRepository;


    @PostMapping("/google")
    public ResponseEntity<UserDto.GoogleLoginResDto> googleLogin(@Valid @RequestBody UserDto.GoogleLoginReqDto req) {

        String idToken = googleTokenExchangeService.exchangeCodeForIdToken(req.getCode());
        User u = userService.loginOrCreateByGoogleIdToken(idToken);
        String accessToken = authService.createAccessToken(u.getId());
        UserDto.UserInfo userInfo =
                new UserDto.UserInfo(u.getId(), u.getName(), u.getEmail(), u.getProfileUrl());

        return ResponseEntity.ok(new UserDto.GoogleLoginResDto(accessToken, userInfo));
    }


    @GetMapping("/me")
    public ResponseEntity<UserDto.UserInfo> me(
            @AuthenticationPrincipal org.example.emmm.security.UserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Long userId = principal.getUserId();

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        return ResponseEntity.ok(new UserDto.UserInfo(u.getId(), u.getName(), u.getEmail(), u.getProfileUrl()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().body("{\"text\":\"로그아웃 되었습니다.\"}");
    }


    @GetMapping("/me/running")
    public ResponseEntity<List<RoomDto.DetailRoomResDto>> getRunningRoom(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(userService.getRunningRooms(reqId));
    }

    @GetMapping("/me/complete")
    public ResponseEntity<List<RoomDto.DetailRoomResDto>> getCompleteRoom(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(userService.getCompleteRooms(reqId));
    }

}

