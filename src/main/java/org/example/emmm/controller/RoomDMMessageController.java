package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.RoomDMMessageDto;
import org.example.emmm.security.UserPrincipal;
import org.example.emmm.service.RoomDMMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/dm")
public class RoomDMMessageController {

    private final RoomDMMessageService roomDMMessageService;
    private final SimpMessagingTemplate template; //webSocket

    @PostMapping("/{roomId}")
    public ResponseEntity<RoomDMMessageDto.CreateDmResDto> createDm(@RequestBody RoomDMMessageDto.CreateDmReqDto req,
                                                                    @PathVariable Long roomId,
                                                                    @AuthenticationPrincipal UserPrincipal principal){
        String wsRes = "update webSocket";
        template.convertAndSend("/topic/dm/" + roomId, wsRes);
        Long userId = principal.getUserId();
        return ResponseEntity.ok(roomDMMessageService.createDm(req, roomId, userId));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDMMessageDto.DetailDmResDto> getDmList(@PathVariable Long roomId,
                                                                     @AuthenticationPrincipal UserPrincipal principal){
        Long userId = principal.getUserId();
        return ResponseEntity.ok(roomDMMessageService.getDmList(roomId, userId));
    }




}
