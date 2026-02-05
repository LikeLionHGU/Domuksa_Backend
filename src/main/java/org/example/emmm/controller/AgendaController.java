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
}
