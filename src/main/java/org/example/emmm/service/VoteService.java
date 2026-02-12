package org.example.emmm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.Agenda;
import org.example.emmm.domain.AgendaConfig;
import org.example.emmm.domain.Vote;
import org.example.emmm.dto.VoteDto;
import org.example.emmm.repository.AgendaConfigRepository;
import org.example.emmm.repository.AgendaRepository;
import org.example.emmm.repository.VoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteRepository voteRepository;
    private final AgendaRepository agendaRepository;
    private final AgendaConfigRepository agendaConfigRepository;

    @Transactional
    public VoteDto.CreateVoteResDto createVoteTemplate(Long agendaId,VoteDto.CreateVoteReqDto req){
        Agenda a = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        Vote v = Vote.builder()
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .title(req.getTitle())
                .voteStatus("running")
                .agenda(a)
                .build();

        AgendaConfig ac = v.getAgenda().getConfig();
        ac.setVoteEnabled(true);
        agendaConfigRepository.save(ac);

        voteRepository.save(v);
        return VoteDto.CreateVoteResDto.from(v, ac);
    }

    @Transactional
    public VoteDto.DetailVoteResDto getVoteTemplate(Long agendaId){
        agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        Vote v = voteRepository.findActiveByAgendaId(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표입니다."));

        return VoteDto.DetailVoteResDto.from(v);
    }
}
