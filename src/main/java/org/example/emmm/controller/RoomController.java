package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.RoomDto;
import org.example.emmm.service.RoomService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
public class RoomController {
    private final RoomService roomService;

    @PostMapping("/{hostId}")
    public RoomDto.CreateResDto create(@RequestBody RoomDto.CreateReqDto req, @PathVariable Long hostId) {
        return roomService.create(req, hostId);
    }

    @PostMapping("/{userId}/member")
    public RoomDto.ParticipateCreateResDto createParticipate(@RequestBody RoomDto.ParticipateCreateReqDto req, @PathVariable Long userId) {
        return roomService.createParticipate(req, userId);
    }

    @GetMapping("/{roomId}/{userId}")
    public RoomDto.DetailResDto getRoom(@PathVariable Long roomId, @PathVariable Long userId) {
        return roomService.getRoom(roomId, userId);
    }

    @DeleteMapping("/{roomId}/{userId}")
    public void delete(@PathVariable Long roomId, @PathVariable Long userId) {
        roomService.delete(roomId, userId);
    }

}
