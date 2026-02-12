package org.example.emmm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.*;
import org.example.emmm.dto.CommentDto;
import org.example.emmm.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final AgendaRepository agendaRepository;
    private final AgendaConfigRepository agendaConfigRepository;
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;

    @Transactional
    public CommentDto.CreateCommentResDto createCommentTemplate(Long agendaId, Long reqId,CommentDto.CreateCommentReqDto req) {
        Agenda a = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        User u = userRepository.findByIdAndDeletedFalse(reqId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        UserRoom ur = userRoomRepository.findActiveUserRoom(u, a.getRoom())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저룸입니다."));

        if(!ur.getRole().equals("host")){
            throw new IllegalArgumentException("호스트가 아닙니다.");
        } else {


        }

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
