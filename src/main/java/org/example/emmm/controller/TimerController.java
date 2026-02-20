package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.TimerDto;
import org.example.emmm.security.UserPrincipal;
import org.example.emmm.service.TimerService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/timer")
public class TimerController {
    private final TimerService timerService;
    private final SimpMessagingTemplate template;

    public String createWsRes(String text){
        return text + LocalDateTime.now();
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<TimerDto.DetailTimerResDto> getTimer(@PathVariable Long roomId){
        return ResponseEntity.ok(timerService.getTimer(roomId));
    }

    @PatchMapping("/{roomId}/time")
    public ResponseEntity<TimerDto.UpdateTimeResDto> updateTime(@PathVariable Long roomId,
                                                               @RequestBody TimerDto.UpdateTimeReqDto req,
                                                               @AuthenticationPrincipal UserPrincipal principal) {

        Long reqId = principal.getUserId();
        TimerDto.UpdateTimeResDto res = timerService.updateTime(roomId,req,reqId);
        String wsRes = createWsRes("update");
        template.convertAndSend("/topic/timer/" + roomId, wsRes);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{roomId}/status")
    public ResponseEntity<TimerDto.UpdateStateResDto> updateState(@PathVariable Long roomId,
                                                              @RequestBody TimerDto.UpdateStateReqDto req,
                                                              @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        TimerDto.UpdateStateResDto res = timerService.updateState(roomId, req, reqId);
        String wsRes = createWsRes("update");
        template.convertAndSend("/topic/timer/" + roomId, wsRes);
        return ResponseEntity.ok(res);
    }



}
