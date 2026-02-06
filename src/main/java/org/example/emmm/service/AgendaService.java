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
                .aiSummaryEnabled(false)
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

    @Transactional
    public AgendaDto.DetailAgendaResDto getAgenda(Long agendaId) {
        Agenda a = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        AgendaConfig ac = a.getConfig();

        return AgendaDto.DetailAgendaResDto.from(a, ac);
    }

    @Transactional
    public AgendaDto.UpdateAgendaResDto updateAgenda(Long agendaId, AgendaDto.UpdateAgendaReqDto req) {
        Agenda a = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        a.setName(req.getName());

        agendaRepository.save(a);

        return AgendaDto.UpdateAgendaResDto.from(a);
    }

    @Transactional
    public AgendaDto.UpdateConfigResDto updateConfig(Long agendaId, AgendaDto.UpdateConfigReqDto req) {
        Agenda a = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        AgendaConfig ac = a.getConfig();

        ac.setVoteEnabled(req.isVoteEnabled());
        ac.setCommentEnabled(req.isCommentEnabled());
        ac.setFileEnabled(req.isFileEnabled());
        ac.setAiSummaryEnabled(req.isAiSummaryEnabled());

        agendaRepository.save(a);

        return AgendaDto.UpdateConfigResDto.from(a, ac);
    }

    @Transactional
    public void deleteAgenda(Long agendaId) {
        Agenda a = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        AgendaConfig ac = a.getConfig();

        ac.setDeleted(true);
        a.setDeleted(true);
    }

}
