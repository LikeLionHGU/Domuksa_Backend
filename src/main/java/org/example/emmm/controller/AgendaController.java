package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.AgendaDto;
import org.example.emmm.service.AgendaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/agenda")
public class AgendaController {
    private final AgendaService agendaService;

    @PostMapping("/{roomId}")
    public ResponseEntity<AgendaDto.CreateAgendaResDto> createAgenda(@PathVariable Long roomId,
                                                                     @RequestBody AgendaDto.CreateAgendaReqDto req) {
        return ResponseEntity.ok(agendaService.createAgenda(roomId, req));
    }

    @GetMapping("/{agendaId}")
    public ResponseEntity<AgendaDto.DetailAgendaResDto> getAgenda(@PathVariable Long agendaId) {
        return ResponseEntity.ok(agendaService.getAgenda(agendaId));
    }

    @PatchMapping("/{agendaId}")
    public ResponseEntity<AgendaDto.UpdateAgendaResDto> updateAgenda(@PathVariable Long agendaId, @RequestBody AgendaDto.UpdateAgendaReqDto req) {
        return ResponseEntity.ok(agendaService.updateAgenda(agendaId, req));
    }

    @PatchMapping("/{agendaId}/config")
    public ResponseEntity<AgendaDto.UpdateConfigResDto> updateConfig(@PathVariable Long agendaId, @RequestBody AgendaDto.UpdateConfigReqDto req) {
        return ResponseEntity.ok(agendaService.updateConfig(agendaId, req));
    }

    @DeleteMapping("/{agendaId}")
    public void deleteAgenda(@PathVariable Long agendaId) {
        agendaService.deleteAgenda(agendaId);
    }

}
