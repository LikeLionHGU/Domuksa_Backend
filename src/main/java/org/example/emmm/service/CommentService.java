package org.example.emmm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.Agenda;
import org.example.emmm.domain.AgendaConfig;
import org.example.emmm.domain.Comment;
import org.example.emmm.dto.CommentDto;
import org.example.emmm.repository.AgendaConfigRepository;
import org.example.emmm.repository.AgendaRepository;
import org.example.emmm.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final AgendaRepository agendaRepository;
    private final AgendaConfigRepository agendaConfigRepository;

    @Transactional
    public CommentDto.CreateCommentResDto createCommentTemplate(Long agendaId, CommentDto.CreateCommentReqDto req) {
        Agenda a = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));
        Comment c = Comment.builder()
                .title(req.getTitle())
                .createdAt(LocalDateTime.now())
                .agenda(a)
                .build();

        AgendaConfig ac = c.getAgenda().getConfig();
        ac.setCommentEnabled(true);
        agendaConfigRepository.save(ac);

        commentRepository.save(c);

        return CommentDto.CreateCommentResDto.from(c, ac);
    }

    public CommentDto.DetailCommentResDto getCommentTemplate(Long agendaId) {
        Agenda a = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        Comment c = commentRepository.findByAgendaAndDeletedFalse(a)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코멘트입니다."));

        return CommentDto.DetailCommentResDto.from(c);
    }
}
