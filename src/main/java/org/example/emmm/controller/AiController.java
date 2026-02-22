package org.example.emmm.controller;

import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.AiDto;
import org.example.emmm.security.UserPrincipal;
import org.example.emmm.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiController {
    private final AiService aiService;
    private final SimpMessagingTemplate template;

    public String createWsRes(String text){
        return text+ LocalDateTime.now();
    }

    @PostMapping("/{agendaId}")
    public ResponseEntity<AiDto.CreateAiResDto> createAi(@PathVariable Long agendaId,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        String wsRes = createWsRes("update");
        template.convertAndSend("/topic/ai/" + agendaId, wsRes);
        return ResponseEntity.ok(aiService.createAi(agendaId, reqId));
    }

    @GetMapping("/{agendaId}")
    public ResponseEntity<AiDto.DetailAiResDto> getAiSummary(@PathVariable Long agendaId,
                                                             @AuthenticationPrincipal UserPrincipal principal){
        Long reqId = principal.getUserId();
        return ResponseEntity.ok(aiService.getAiSummary(agendaId, reqId));
    }

    @PatchMapping("/{agendaId}")
    public ResponseEntity<AiDto.CreateAiResDto> updateAi(@PathVariable Long agendaId,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        Long reqId = principal.getUserId();
        String wsRes = createWsRes("update");
        template.convertAndSend("/topic/ai/" + agendaId, wsRes);
        return ResponseEntity.ok(aiService.updateAi(agendaId, reqId));
    }
}