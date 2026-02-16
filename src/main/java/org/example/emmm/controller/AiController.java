package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.AiDto;
import org.example.emmm.security.UserPrincipal;
import org.example.emmm.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiController {
    private final AiService aiService;

    @PostMapping("/{agendaId}")
    public ResponseEntity<AiDto.CreateAiResDto> createAi(@PathVariable Long agendaId,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(aiService.createAi(agendaId, reqId));
    }

    @GetMapping("/{agendaId}")
    public ResponseEntity<AiDto.DetailAiResDto> getAiSummary(@PathVariable Long agendaId,
                                                             @AuthenticationPrincipal UserPrincipal principal){
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(aiService.getAiSummary(agendaId, reqId));
    }
}
