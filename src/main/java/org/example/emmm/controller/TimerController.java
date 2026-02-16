package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.TimerDto;
import org.example.emmm.security.UserPrincipal;
import org.example.emmm.service.TimerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/timer")
public class TimerController {
    private final TimerService timerService;

    @PostMapping("/{roomId}/start")
    public ResponseEntity<TimerDto.TimerResDto> start(@PathVariable Long roomId,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(timerService.start(roomId, reqId));
    }

    @PostMapping("/{roomId}/pause")
    public ResponseEntity<TimerDto.TimerResDto> pause(@PathVariable Long roomId,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(timerService.pause(roomId, reqId));
    }

    @PostMapping("/{roomId}/reset")
    public ResponseEntity<TimerDto.TimerResDto> reset(@PathVariable Long roomId,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(timerService.reset(roomId, reqId));
    }

    @PostMapping("/{roomId}/set")
    public ResponseEntity<TimerDto.TimerResDto> set(@PathVariable Long roomId,
                                                      @RequestBody TimerDto.TimerReqDto req,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(timerService.set(roomId, reqId, req));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<TimerDto.TimerResDto> get(@PathVariable Long roomId,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(timerService.get(roomId, reqId));
    }
}
