package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.CommentDto;
import org.example.emmm.dto.CommentOptionDto;
import org.example.emmm.service.CommentOptionService;
import org.example.emmm.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;
    private final CommentOptionService commentOptionService;

    //commentTemplate 생성
    @PostMapping("/{agendaId}")
    public ResponseEntity<CommentDto.CreateCommentResDto> createCommentTemplate(@PathVariable Long agendaId,
                                                                        @RequestBody CommentDto.CreateCommentReqDto req) {
        return ResponseEntity.ok(commentService.createCommentTemplate(agendaId, req));
    }

    //commentTemplate정보 가져오기
    @GetMapping("/{agendaId}")
    public ResponseEntity<CommentDto.DetailCommentResDto> getComment(@PathVariable Long agendaId) {
        return ResponseEntity.ok(commentService.getCommentTemplate(agendaId));
    }

    //commentOption 생성
    @PostMapping("/{commentId}/option")
    public ResponseEntity<CommentOptionDto.CreateCommentOptionResDto> createCommentOption(@PathVariable Long commentId,
                                                                                          @RequestBody CommentOptionDto.CreateCommentOptionReqDto req) {
        return ResponseEntity.ok(commentOptionService.createCommentOption(commentId, req));
    }

    //해당 안건에 대한 commentOption 전부 가져오기
    @GetMapping("/{commentId}/option")
    public ResponseEntity<CommentOptionDto.DetailCommentOptionResDto> getCommentOption(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentOptionService.getCommentOption(commentId));
    }

    @PatchMapping("/{commentOptionId}")
    public ResponseEntity<CommentOptionDto.UpdateCommentOptionResDto> updateCommentOption(@PathVariable Long commentOptionId,
                                                                                          @RequestBody CommentOptionDto.UpdateCommentOptionReqDto req) {
        return ResponseEntity.ok(commentOptionService.updateCommentOption(commentOptionId, req));
    }

    @DeleteMapping("/{commentOptionId}")
    public void deleteCommentOption(@PathVariable Long commentOptionId) {
        commentOptionService.deleteCommentOption(commentOptionId);
    }
}
