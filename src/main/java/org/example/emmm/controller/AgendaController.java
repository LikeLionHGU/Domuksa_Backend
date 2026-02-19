package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.AgendaDto;
import org.example.emmm.service.AgendaService;
import org.example.emmm.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/agenda")
public class AgendaController {
    private final AgendaService agendaService;
    private final RoomService roomService;
    private final SimpMessagingTemplate template;

    @PostMapping("/{roomId}")
    public ResponseEntity<AgendaDto.CreateAgendaResDto> createAgenda(@PathVariable Long roomId,
                                                                     @RequestBody AgendaDto.CreateAgendaReqDto req) {

        AgendaDto.CreateAgendaResDto res = agendaService.createAgenda(roomId, req);

        List<AgendaDto.DetailListAgendaResDto> wsRes = roomService.getAgendas(roomId);

        template.convertAndSend("/topic/agenda/list/" + roomId, wsRes);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{agendaId}")
    public ResponseEntity<AgendaDto.DetailAgendaResDto> getAgenda(@PathVariable Long agendaId) {
        AgendaDto.DetailAgendaResDto res = agendaService.getAgenda(agendaId);

        template.convertAndSend("/topic/agenda/current/" + res.getAgenda().getRoomId(), res);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{agendaId}")
    public ResponseEntity<AgendaDto.UpdateAgendaResDto> updateAgenda(@PathVariable Long agendaId,
                                                                     @RequestBody AgendaDto.UpdateAgendaReqDto req) {
        AgendaDto.UpdateAgendaResDto res = agendaService.updateAgenda(agendaId, req);

        List<AgendaDto.DetailListAgendaResDto> wsRes = roomService.getAgendas(res.getRoomId());

        template.convertAndSend("/topic/agenda/list/" + res.getRoomId(), wsRes);

        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{agendaId}/config")
    public ResponseEntity<AgendaDto.UpdateConfigResDto> updateConfig(@PathVariable Long agendaId, @RequestBody AgendaDto.UpdateConfigReqDto req) {
        return ResponseEntity.ok(agendaService.updateConfig(agendaId, req));
    }

    @DeleteMapping("/{agendaId}")
    public ResponseEntity<Void> deleteAgenda(@PathVariable Long agendaId) {

        Long res = agendaService.deleteAgenda(agendaId);

        List<AgendaDto.DetailListAgendaResDto> wsRes = roomService.getAgendas(res);

        template.convertAndSend("/topic/agenda/list/" + res, wsRes);

        return ResponseEntity.ok().build();
    }

}
