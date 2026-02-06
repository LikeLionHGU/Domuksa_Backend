package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.VoteDto;
import org.example.emmm.dto.VoteOptionDto;
import org.example.emmm.dto.VoteSelectionDto;
import org.example.emmm.security.UserPrincipal;
import org.example.emmm.service.VoteOptionService;
import org.example.emmm.service.VoteSelectionService;
import org.example.emmm.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/vote")
public class VoteController {
    private final VoteService voteService;
    private final VoteOptionService voteOptionService;
    private final VoteSelectionService voteSelectionService;

    //해당 안건 투표 활성화 + 이름정하기
    @PostMapping("/{agendaId}")
    public ResponseEntity<VoteDto.CreateVoteResDto> createVoteTemplate(@PathVariable Long agendaId,
                                                               @RequestBody VoteDto.CreateVoteReqDto req){
        return ResponseEntity.ok(voteService.createVoteTemplate(agendaId, req));
    }

    //해당 안건과 그의 투표 아이디 + 이름 가져오기
    @GetMapping("/{agendaId}")
    public ResponseEntity<VoteDto.DetailVoteResDto> getVoteTemplate(@PathVariable Long agendaId){
        return ResponseEntity.ok(voteService.getVoteTemplate(agendaId));
    }

    //voteOption 만들기
    @PostMapping("/{voteId}/voteOption")
    public ResponseEntity<VoteOptionDto.CreateOptionResDto> createVoteOption(@PathVariable Long voteId, @RequestBody VoteOptionDto.CreateOptionReqDto req){
        return ResponseEntity.ok(voteOptionService.createVoteOption(voteId, req));
    }

    //해당 안건의 vote, voteOption, mySelection 모두 List로 get하기
    @GetMapping("/{voteId}")
    public ResponseEntity<VoteOptionDto.DetailOptionResDto> getVoteOption(@PathVariable Long voteId,
                                                                          @AuthenticationPrincipal UserPrincipal principal){
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(voteOptionService.getVoteOption(voteId, reqId));
    }


    //voteSelection으로 내 투표 내용 저장하기
    @PostMapping("/{voteId}")
    public ResponseEntity<VoteSelectionDto.CreateSelectResDto> createVoteSelect(@RequestBody VoteSelectionDto.CreateSelectReqDto req,
                                                                                @PathVariable Long voteId,
                                                                                @AuthenticationPrincipal UserPrincipal principal){
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(voteSelectionService.createVoteSelection(voteId, reqId, req));
    }
}
