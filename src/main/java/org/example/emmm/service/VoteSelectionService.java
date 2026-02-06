package org.example.emmm.service;

import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.User;
import org.example.emmm.domain.Vote;
import org.example.emmm.domain.VoteOption;
import org.example.emmm.domain.VoteSelection;
import org.example.emmm.dto.VoteSelectionDto;
import org.example.emmm.repository.UserRepository;
import org.example.emmm.repository.VoteOptionRepository;
import org.example.emmm.repository.VoteRepository;
import org.example.emmm.repository.VoteSelectionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoteSelectionService {
    private final VoteSelectionRepository voteSelectionRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final VoteOptionRepository voteOptionRepository;

    public VoteSelectionDto.CreateSelectResDto createVoteSelection(Long voteId, Long userId, VoteSelectionDto.CreateSelectReqDto req) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Vote v = voteRepository.findByIdAndDeletedFalse(voteId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표입니다."));

        VoteOption vo = voteOptionRepository.findByIdAndDeletedFalse(req.getVoteOptionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표옵션입니다."));

        VoteSelection vs = VoteSelection.builder()
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .user(u)
                .vote(v)
                .voteOption(vo)
                .build();

        voteSelectionRepository.save(vs);

        return VoteSelectionDto.CreateSelectResDto.from(vs);

    }
}
