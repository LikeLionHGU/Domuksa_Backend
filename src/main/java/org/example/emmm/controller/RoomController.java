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

    //host가 방 만들기
    @PostMapping("/host")
    public ResponseEntity<RoomDto.CreateRoomResDto> create(@RequestBody RoomDto.CreateRoomReqDto req,
                                                           @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(roomService.create(req, reqId));
    }

    //password가 있는 방일 때 참가
    @PostMapping("/{roomId}/member/password")
    public ResponseEntity<RoomDto.ParticipatePasswordCreateResDto> createParticipatePassWord(@PathVariable Long roomId,
                                                                                             @RequestBody RoomDto.ParticipatePasswordCreateReqDto req,
                                                                                             @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(roomService.createParticipatePassword(req, roomId, reqId));
    }

    //password가 없는 방일 때 바로 참여
    @PostMapping("/{roomId}/member")
    public ResponseEntity<RoomDto.ParticipateCreateResDto> createParticipate(@PathVariable Long roomId,
                                                                             @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(roomService.createParticipate(roomId, reqId));
    }

    //code를 통해서 roomId, isPassword 정보 가져오기
    @GetMapping("/code")
    public ResponseEntity<RoomDto.CodeResDto> getRoomIdAndIsPassword (@RequestParam("code") String code) {
        return ResponseEntity.ok(roomService.getRoomIdAndIsPassword(code));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto.DetailRoomResDto> getRoom(@PathVariable Long roomId,
                                                            @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(roomService.getRoom(roomId, reqId));
    }

    @PatchMapping("/{roomId}")
    public ResponseEntity<RoomDto.UpdateRoomNameResDto> updateRoomName (@RequestBody RoomDto.UpdateRoomNameReqDto req,
                                                                        @PathVariable Long roomId,
                                                                        @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(roomService.updateRoomNameAndPassword(req, roomId, reqId));
    }

    @DeleteMapping("/{roomId}")
    public void delete(@PathVariable Long roomId,
                       @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        roomService.delete(roomId, reqId);
    }

    //room에대한 agenda의 모든 정보 다 가져오기
    @GetMapping("/{roomId}/agenda")
    public ResponseEntity<List<AgendaDto.DetailListAgendaResDto>> getAgendas(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getAgendas(roomId));
    }

    //해당 방의 웹소켓 적용해서 진행중/완료됨 상태 변환하기
    @PatchMapping("/{roomId}/state")
    public void updateRoomState(@AuthenticationPrincipal UserPrincipal principal,
                                @PathVariable Long roomId) {
        Long reqId = principal.getUserId();
        roomService.updateRoomState(roomId, reqId);
    }

}
