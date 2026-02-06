package org.example.emmm.service;

import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.Agenda;
import org.example.emmm.domain.AgendaConfig;
import org.example.emmm.domain.Vote;
import org.example.emmm.dto.VoteDto;
import org.example.emmm.repository.AgendaRepository;
import org.example.emmm.repository.VoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteRepository voteRepository;
    private final AgendaRepository agendaRepository;

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

        voteRepository.save(v);
        return VoteDto.CreateVoteResDto.from(v, ac);
    }

    public VoteDto.DetailVoteResDto getVoteTemplate(Long agendaId){
        agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        Vote v = voteRepository.findByAgendaIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표입니다."));

        return VoteDto.DetailVoteResDto.from(v);
    }
}
