package org.example.emmm.service;

import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.Comment;
import org.example.emmm.domain.CommentOption;
import org.example.emmm.dto.CommentOptionDto;
import org.example.emmm.repository.CommentOptionRepository;
import org.example.emmm.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentOptionService {
    private final CommentOptionRepository commentOptionRepository;
    private final CommentRepository commentRepository;

    public CommentOptionDto.CreateCommentOptionResDto createCommentOption(Long commentId,
                                                                          CommentOptionDto.CreateCommentOptionReqDto req) {
        Comment c = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코멘트입니다."));

        CommentOption co = CommentOption.builder()
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .content(req.getContent())
                .comment(c)
                .build();

        commentOptionRepository.save(co);

        return CommentOptionDto.CreateCommentOptionResDto.from(co);
    }

    public CommentOptionDto.DetailCommentOptionResDto getCommentOption(Long commentId) {
        Comment c = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코멘트입니다."));

        List<CommentOption> cos = commentOptionRepository.findAllActiveByCommentId(commentId);

        return CommentOptionDto.DetailCommentOptionResDto.from(c, cos);
    }

    public CommentOptionDto.UpdateCommentOptionResDto updateCommentOption(Long commentOptionId, CommentOptionDto.UpdateCommentOptionReqDto req) {
        CommentOption co = commentOptionRepository.findByIdAndDeletedFalse(commentOptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코멘트옵션입니다."));

        co.setContent(req.getContent());
        commentOptionRepository.save(co);

        return CommentOptionDto.UpdateCommentOptionResDto.from(co);
    }


    public void deleteCommentOption(Long commentOptionId) {
        CommentOption co = commentOptionRepository.findByIdAndDeletedFalse(commentOptionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코멘트옵션입니다."));

        co.setDeleted(true);
    }
}