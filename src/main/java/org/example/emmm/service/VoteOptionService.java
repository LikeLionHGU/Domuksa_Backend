package org.example.emmm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.User;
import org.example.emmm.domain.Vote;
import org.example.emmm.domain.VoteOption;
import org.example.emmm.domain.VoteSelection;
import org.example.emmm.dto.VoteOptionDto;
import org.example.emmm.repository.UserRepository;
import org.example.emmm.repository.VoteOptionRepository;
import org.example.emmm.repository.VoteRepository;
import org.example.emmm.repository.VoteSelectionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoteOptionService {
    private final VoteOptionRepository voteOptionRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final VoteSelectionRepository voteSelectionRepository;

    @Transactional
    public VoteOptionDto.CreateOptionResDto createVoteOption(Long voteId, VoteOptionDto.CreateOptionReqDto req) {
        Vote v = voteRepository.findByIdAndDeletedFalse(voteId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표입니다."));

        VoteOption vo = VoteOption.builder()
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .content(req.getContent())
                .selectCount(0)
                .vote(v)
                .build();

        voteOptionRepository.save(vo);

        return VoteOptionDto.CreateOptionResDto.from(vo);
    }
    @Transactional
    public VoteOptionDto.DetailOptionResDto getVoteOption(Long voteId, Long userId) {
        User u = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Vote v = voteRepository.findByIdAndDeletedFalse(voteId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표입니다."));

        List<VoteOption> vos = voteOptionRepository.findAllActiveByVoteId(v.getId());
        if (vos.isEmpty()) {
            throw new IllegalArgumentException("투표 옵션이 존재하지 않습니다.");
        }

        VoteSelection vs = voteSelectionRepository.findActiveVoteSelection(u.getId(), v.getId()).orElse(null);

        for(VoteOption vo : vos) {
            int sc = voteSelectionRepository.countByVoteOptionIdAndDeletedFalse(vo.getId());
            vo.setSelectCount(sc);
        }

        return VoteOptionDto.DetailOptionResDto.from(v, vos, vs);
    }

    @Transactional
    public VoteOptionDto.UpdateOptionResDto updateOption (Long voteOptionId, VoteOptionDto.UpdateOptionReqDto req) {
        VoteOption vo = voteOptionRepository.findByIdAndDeletedFalse(voteOptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표옵션입니다."));

        vo.setContent(req.getContent());

        return VoteOptionDto.UpdateOptionResDto.from(vo);
    }

    @Transactional
    public void deleteOption(Long voteOptionId) {
        VoteOption vo = voteOptionRepository.findByIdAndDeletedFalse(voteOptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표옵션입니다."));

        vo.setDeleted(true);
    }

    public List<VoteOptionDto.DetailVoteResultResDto> getVoteResult(Long voteId) {
        Vote v = voteRepository.findByIdAndDeletedFalse(voteId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표입니다."));

        List<VoteOption> vos = voteOptionRepository.findAllActiveByVoteId(v.getId());

        if (vos.isEmpty()) {
            throw new IllegalArgumentException("투표옵션이 없습니다.");
        }

        for (VoteOption vo : vos) {
            int count = voteSelectionRepository.countByVoteOptionIdAndDeletedFalse(vo.getId());
            vo.setSelectCount(count);
        }

        int maxCount = vos.stream()
                .mapToInt(VoteOption::getSelectCount)
                .max()
                .orElse(0);

        return vos.stream()
                .filter(vo -> vo.getSelectCount() == maxCount && maxCount > 0)
                .map(VoteOptionDto.DetailVoteResultResDto::from)
                .toList();
    }

}
