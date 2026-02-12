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

import java.util.List;


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

    //해당 안건과 그의 해당하는 모든 투표 아이디 + 이름 가져오기
    @GetMapping("/{agendaId}")
    public ResponseEntity<List<VoteDto.DetailVoteResDto>> getVote(@PathVariable Long agendaId){
        return ResponseEntity.ok(voteService.getVote(agendaId));
    }

    //투표 제목 수정하기
    @PatchMapping("/{voteId}")
    public ResponseEntity<VoteDto.UpdateVoteResDto> updateVote(@PathVariable Long voteId,
                                                               @RequestBody VoteDto.UpdateVoteReqDto req){
        return ResponseEntity.ok(voteService.updateVote(voteId, req));
    }

    //Vote 삭제하기
    @DeleteMapping("/{voteId}")
    public ResponseEntity<String> deleteVote(@PathVariable Long voteId){
        return ResponseEntity.ok(voteService.deleteVote(voteId));
    }


    //voteOption 만들기
    @PostMapping("/{voteId}/option")
    public ResponseEntity<VoteOptionDto.CreateOptionResDto> createVoteOption(@PathVariable Long voteId, @RequestBody VoteOptionDto.CreateOptionReqDto req){
        return ResponseEntity.ok(voteOptionService.createVoteOption(voteId, req));
    }

    //해당 안건의 vote, voteOption, mySelection 모두 List로 get하기
    @GetMapping("/{voteId}/option")
    public ResponseEntity<VoteOptionDto.DetailOptionResDto> getVoteOption(@PathVariable Long voteId,
                                                                          @AuthenticationPrincipal UserPrincipal principal){
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(voteOptionService.getVoteOption(voteId, reqId));
    }

    //voteOption의 content내용 변경
    @PatchMapping("/{voteOptionId}/option")
    public ResponseEntity<VoteOptionDto.UpdateOptionResDto> updateVoteOption(@PathVariable Long voteOptionId,
                                                                             @RequestBody VoteOptionDto.UpdateOptionReqDto req){
        return ResponseEntity.ok(voteOptionService.updateOption(voteOptionId, req));
    }

    //voteOption 삭제하기
    @DeleteMapping("/{voteOptionId}/option")
    public void deleteVoteOption(@PathVariable Long voteOptionId) {
        voteOptionService.deleteOption(voteOptionId);
    }

    //voteSelection으로 내 투표 내용 저장하기
    @PostMapping("/{voteId}/voteSelect")
    public ResponseEntity<VoteSelectionDto.CreateSelectResDto> createVoteSelect(@RequestBody VoteSelectionDto.CreateSelectReqDto req,
                                                                                @PathVariable Long voteId,
                                                                                @AuthenticationPrincipal UserPrincipal principal){
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(voteSelectionService.createVoteSelection(voteId, reqId, req));
    }

    //voteSelection의 내 투표 내용 수정하기
    @PatchMapping("/{voteId}/voteSelect")
    public ResponseEntity<VoteSelectionDto.UpdateSelectResDto> updateVoteSelect(@RequestBody VoteSelectionDto.UpdateSelectReqDto req,
                                                                                @PathVariable Long voteId,
                                                                                @AuthenticationPrincipal UserPrincipal principal){
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(voteSelectionService.updateVoteSelection(voteId, reqId, req));
    }
}
