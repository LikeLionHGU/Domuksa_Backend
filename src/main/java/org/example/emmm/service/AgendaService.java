package org.example.emmm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.Agenda;
import org.example.emmm.domain.AgendaConfig;
import org.example.emmm.domain.Room;
import org.example.emmm.dto.AgendaDto;
import org.example.emmm.repository.AgendaRepository;
import org.example.emmm.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AgendaService {
    private final AgendaRepository agendaRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public AgendaDto.CreateAgendaResDto createAgenda(Long roomId, AgendaDto.CreateAgendaReqDto req) {
        Room r = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        AgendaConfig ac = AgendaConfig.builder()
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .voteEnabled(false)
                .commentEnabled(false)
                .fileEnabled(false)
                .build();

        Agenda a = Agenda.builder()
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .name(req.getName())
                .sequence(req.getSequence())
                .room(r)
                .config(ac)
                .build();

        ac.setAgenda(a);

        agendaRepository.save(a);

        return AgendaDto.CreateAgendaResDto.from(a, ac);
    }
}
