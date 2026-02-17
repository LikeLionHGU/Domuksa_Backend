package org.example.emmm.service;

import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.Agenda;
import org.example.emmm.domain.AgendaConfig;
import org.example.emmm.domain.Comment;
import org.example.emmm.dto.CommentDto;
import org.example.emmm.repository.AgendaConfigRepository;
import org.example.emmm.repository.AgendaRepository;
import org.example.emmm.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final AgendaRepository agendaRepository;
    private final AgendaConfigRepository agendaConfigRepository;

    @Transactional
    public CommentDto.CreateCommentResDto createComment(Long agendaId, CommentDto.CreateCommentReqDto req) {
        Agenda a = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        Comment c = Comment.builder()
                .createdAt(LocalDateTime.now())
                .content(req.getContent())
                .agenda(a)
                .build();

        commentRepository.save(c);

        AgendaConfig ac = agendaConfigRepository.findByIdAndDeletedFalse(a.getConfig().getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건설정입니다."));

        if(ac.getCommentEnabled().equals(false)) {
            ac.setCommentEnabled(true);
        }

        return CommentDto.CreateCommentResDto.from(c);
    }

    @Transactional(readOnly = true)
    public List<CommentDto.DetailCommentResDto> getComment(Long agendaId) {
        Agenda a = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));


        List<Comment> cs = commentRepository.findAllActiveByCommentId(a.getId());

        return cs.stream().map(CommentDto.DetailCommentResDto::from).toList();
    }

    @Transactional
    public CommentDto.UpdateCommentResDto updateComment(Long commentId, CommentDto.UpdateCommentReqDto req) {
        Comment c = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코멘트입니다."));

        c.setContent(req.getContent());
        commentRepository.save(c);

        return CommentDto.UpdateCommentResDto.from(c);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment c = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코멘트입니다."));

        c.setDeleted(true);
    }
}