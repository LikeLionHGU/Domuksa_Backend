package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.AgendaDto;
import org.example.emmm.dto.RoomDto;
import org.example.emmm.security.UserPrincipal;
import org.example.emmm.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
public class RoomController {
    private final RoomService roomService;

    @PostMapping("/host")
    public ResponseEntity<RoomDto.CreateRoomResDto> create(@RequestBody RoomDto.CreateRoomReqDto req,
                                                           @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(roomService.create(req, reqId));
    }

    @PostMapping("/member")
    public ResponseEntity<RoomDto.ParticipateCreateResDto> createParticipate(@RequestBody RoomDto.ParticipateCreateReqDto req,
                                                                             @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(roomService.createParticipate(req, reqId));
    }

    @GetMapping("/{roomId}/{reqId}")
    public ResponseEntity<RoomDto.DetailRoomResDto> getRoom(@PathVariable Long roomId,
                                                            @PathVariable Long reqId) {
        //Long reqId = principal.getUserId();
        return ResponseEntity.ok(roomService.getRoom(roomId, reqId));
    }

    @DeleteMapping("/{roomId}")
    public void delete(@PathVariable Long roomId,
                       @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        roomService.delete(roomId, reqId);
    }

    //room에대한 agenda의 모든 정보 다 가져오기
    @GetMapping("/{roomId}/agenda")
    public ResponseEntity<List<AgendaDto.DetailAgendaResDto>> getAgendas(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getAgendas(roomId));
    }

    //해당 방의 웹소켓 적용해서 진행중/완료됨 상태 변환하기
    @PatchMapping("/{roomId}/state")
    public void updateRoomState(@RequestBody RoomDto.UpdateRoomReqDto req,
                                @AuthenticationPrincipal UserPrincipal principal,
                                @PathVariable Long roomId) {
        Long reqId = principal.getUserId();
        roomService.updateRoomState(req, roomId, reqId);
    }

}
