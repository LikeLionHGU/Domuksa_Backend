package org.example.emmm.service;

import org.example.emmm.domain.*;
import org.example.emmm.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.VoteDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteRepository voteRepository;
    private final AgendaRepository agendaRepository;
    private final AgendaConfigRepository agendaConfigRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteSelectionRepository voteSelectionRepository;

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

    @Transactional(readOnly = true)
    public List<VoteDto.DetailVoteResDto> getVote(Long agendaId) {
        if (!agendaRepository.existsByIdAndDeletedFalse(agendaId)) {
            throw new IllegalArgumentException("존재하지 않거나 삭제된 안건입니다.");
        }

        List<Vote> votes = voteRepository.findAllActiveByAgendaId(agendaId);

        return votes.stream()
                .map(VoteDto.DetailVoteResDto::from)
                .toList();
    }

    @Transactional
    public VoteDto.UpdateVoteResDto updateVote(Long voteId, VoteDto.UpdateVoteReqDto req){
        Vote v = voteRepository.findByIdAndDeletedFalse(voteId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        v.setTitle(req.getTitle());

        return VoteDto.UpdateVoteResDto.from(v);
    }

    @Transactional
    public String deleteVote(Long voteId) {
        Vote v = voteRepository.findByIdAndDeletedFalse(voteId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표입니다."));

        List<VoteOption> vos = voteOptionRepository.findAllActiveByVoteId(voteId);

        List<VoteSelection> vss = voteSelectionRepository.findAllByVoteIdAndDeletedFalse(voteId);

        v.setDeleted(true);

        for (VoteOption vo : vos) {
            vo.setDeleted(true);
        }

        for (VoteSelection vs : vss) {
            vs.setDeleted(true);
        }

        return "삭제되었습니다";
    }
}
