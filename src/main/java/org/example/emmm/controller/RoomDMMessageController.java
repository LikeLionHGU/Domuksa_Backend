package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.RoomDMMessageDto;
import org.example.emmm.service.RoomDMMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
public class RoomDMMessageController {

    private final RoomDMMessageService roomDMMessageService;

    @PostMapping("/dm")
    public ResponseEntity<RoomDMMessageDto.CreateDmResDto> createDm(@RequestBody RoomDMMessageDto.CreateDmReqDto req){
        RoomDMMessageDto.CreateDmResDto response = roomDMMessageService.createDm(req);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dm")
    public ResponseEntity<RoomDMMessageDto.DetailDmResDto> getDmList(@PathVariable Long roomId, @RequestParam Long userRoomId){
        RoomDMMessageDto.DetailDmResDto res = roomDMMessageService.getDmList(roomId,userRoomId);
        return ResponseEntity.ok(res);
    }
}
