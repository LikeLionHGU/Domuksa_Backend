package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.AgendaDto;
import org.example.emmm.service.AgendaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/agenda")
public class AgendaController {
    private final AgendaService agendaService;

    @PostMapping("/{roomId}")
    public AgendaDto.CreateAgendaResDto createAgenda(@PathVariable Long roomId, @RequestBody AgendaDto.CreateAgendaReqDto req) {
        return agendaService.createAgenda(roomId, req);
    }

    @GetMapping("/{agendaId}")
    public AgendaDto.DetailAgendaResDto getAgenda(@PathVariable Long agendaId) {
        return agendaService.getAgenda(agendaId);
    }

    @PatchMapping("/{agendaId}")
    public AgendaDto.UpdateAgendaResDto updateAgenda(@PathVariable Long agendaId, @RequestBody AgendaDto.UpdateAgendaReqDto req) {
        return agendaService.updateAgenda(agendaId, req);
    }

    @PatchMapping("/{agendaId}/config")
    public AgendaDto.UpdateConfigResDto updateConfig(@PathVariable Long agendaId, @RequestBody AgendaDto.UpdateConfigReqDto req) {
        return agendaService.updateConfig(agendaId, req);
    }

    @DeleteMapping("/{agendaId}")
    public void deleteAgenda(@PathVariable Long agendaId) {
        agendaService.deleteAgenda(agendaId);
    }

}
