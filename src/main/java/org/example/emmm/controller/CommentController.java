package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.CommentDto;
import org.example.emmm.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;
    private final SimpMessagingTemplate template;

    public String createWsRes(String text){
        return text+ LocalDateTime.now();
    }

    //comment 추가
    @PostMapping("/{agendaId}")
    public ResponseEntity<CommentDto.CreateCommentResDto> createCommentOption(@PathVariable Long agendaId,
                                                                                    @RequestBody CommentDto.CreateCommentReqDto req) {
        CommentDto.CreateCommentResDto res =  commentService.createComment(agendaId, req);

        String wsRes = createWsRes("update webSocket");
        template.convertAndSend("/topic/comment/list/" + agendaId, wsRes);
        return ResponseEntity.ok(res);
    }

    //해당 안건에 대한 comment들 전부 가져오기
    @GetMapping("/{agendaId}")
    public ResponseEntity<List<CommentDto.DetailCommentResDto>> getCommentOption(@PathVariable Long agendaId) {
        return ResponseEntity.ok(commentService.getComment(agendaId));
    }

    //사용안함
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto.UpdateCommentResDto> updateCommentOption(@PathVariable Long commentId,
                                                                                    @RequestBody CommentDto.UpdateCommentReqDto req) {
        return ResponseEntity.ok(commentService.updateComment(commentId, req));
    }

    //사용안함
    @DeleteMapping("/{commentId}")
    public void deleteCommentOption(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }
}
