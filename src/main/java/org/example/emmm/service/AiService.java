package org.example.emmm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.*;
import org.example.emmm.dto.AiDto;
import org.example.emmm.dto.AiMaterialDto;
import org.example.emmm.repository.*;
import org.example.emmm.util.AiPromptBuilder;
import org.example.emmm.util.LlmClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiRepository aiRepository;
    private final AgendaRepository agendaRepository;
    private final AgendaConfigRepository agendaConfigRepository;
    private final UserRoomRepository userRoomRepository;

    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteSelectionRepository voteSelectionRepository;

    private final CommentRepository commentRepository;
    private final FileRepository fileRepository;

    private final AiPromptBuilder promptBuilder;
    private final LlmClient llmClient;

    private final S3Downloader s3Downloader;
    private final PdfTextExtractor pdfTextExtractor;
    private final UserRepository userRepository;

    // ✅ Bean 주입이 꼬일 수 있으면 new ObjectMapper()로 바꿔도 됩니다.
    private final ObjectMapper objectMapper;

    // --------------------------------------
    // 1) POST: 생성(없으면 생성, 있으면 덮어쓰기)
    // --------------------------------------
    @Transactional
    public AiDto.CreateAiResDto createAi(Long agendaId, Long userId) {
        return generateAndSaveAi(agendaId, userId, false);
    }

    // --------------------------------------
    // 2) PATCH: 업데이트(재요약) - 기존 Ai가 있어야만
    // --------------------------------------
    @Transactional
    public AiDto.CreateAiResDto updateAi(Long agendaId, Long userId) {
        return generateAndSaveAi(agendaId, userId, true);
    }

    // --------------------------------------
    // 공통: 재료 수집 → (LLM 1회) JSON 응답 → 저장
    // requireExisting=true면 기존 AI 없을 때 예외
    // --------------------------------------
    private AiDto.CreateAiResDto generateAndSaveAi(Long agendaId, Long userId, boolean requireExisting) {

        // 1) Agenda 확인
        Agenda agenda = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        // 2) User 확인
        User u = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 3) 참여자 검증
        UserRoom ur = userRoomRepository.findActiveUserRoom(u.getId(), agenda.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 방 참여자가 아닙니다."));

        // 4) 호스트만 허용
        if (!"host".equals(ur.getRole())) {
            throw new IllegalArgumentException("호스트만 AI 요약을 생성/수정할 수 있습니다.");
        }

        // 5) PATCH면 기존 Ai 존재해야 함
        Ai existing = aiRepository.findActiveAiByAgendaId(agendaId).orElse(null);
        if (requireExisting && existing == null) {
            throw new IllegalArgumentException("기존 AI요약이 없습니다. 먼저 생성(POST)하세요.");
        }

        // 6) AgendaConfig.aiSummaryEnabled 토글
        AgendaConfig ac = agenda.getConfig();
        if (ac != null && (ac.getAiSummaryEnabled() == null || !ac.getAiSummaryEnabled())) {
            ac.setAiSummaryEnabled(true);
            agendaConfigRepository.save(ac);
        }

        // 7) materials 수집
        AiMaterialDto.AgendaMaterials materials = buildMaterials(agenda);

        // 8) 프롬프트 생성 (⚠️ 반드시 JSON만 출력하도록 PromptBuilder를 바꿔야 함)
        String prompt = promptBuilder.buildSummaryPrompt(materials);

        // 9) LLM 1회 호출 → JSON 문자열 받기
        //    (LlmClient가 generateText(prompt) + summarizeText default 구조라면 summarizeText를 호출해도 됨)
        String raw = llmClient.summarizeText(prompt);
        assertLlmOk(raw);

        // 10) JSON 파싱: {"title":"...", "summaryText":"..."}
        AiGenResponse parsed = parseAiGenResponse(raw);

        String title = sanitizeTitle(parsed.getTitle());
        String summary = (parsed.getSummaryText() == null) ? "" : parsed.getSummaryText().trim();

        // 11) 검증
        assertLlmOk(title);
        assertLlmOk(summary);

        // 12) 저장(upsert)
        Ai ai = (existing != null) ? existing : Ai.builder().agenda(agenda).build();
        ai.setTitle(title);
        ai.setSummaryText(summary);

        Ai saved = aiRepository.save(ai);
        return AiDto.CreateAiResDto.from(saved);
    }

    // --------------------------------------
    // materials 구성 (기존 로직 유지)
    // --------------------------------------
    private AiMaterialDto.AgendaMaterials buildMaterials(Agenda agenda) {
        Long agendaId = agenda.getId();

        // Vote
        AiMaterialDto.VoteItem voteItem = null;
        Vote vote = voteRepository.findActiveByAgendaId(agendaId).orElse(null);
        if (vote != null) {
            List<VoteOption> options = voteOptionRepository.findAllActiveByVoteId(vote.getId());
            List<AiMaterialDto.VoteOptionItem> optionItems = options.stream()
                    .map(o -> {
                        int count = voteSelectionRepository.countByVoteOptionIdAndDeletedFalse(o.getId());
                        return new AiMaterialDto.VoteOptionItem(o.getContent(), count);
                    })
                    .toList();

            voteItem = new AiMaterialDto.VoteItem(
                    vote.getTitle(),
                    String.valueOf(vote.getVoteStatus()),
                    optionItems
            );
        }

        // Comments
        List<Comment> comments = commentRepository.findAllActiveByAgendaId(agendaId);
        List<AiMaterialDto.CommentItem> commentItems = comments.isEmpty()
                ? List.of()
                : comments.stream().map(c -> new AiMaterialDto.CommentItem(c.getContent())).toList();

        // Files
        List<File> files = fileRepository.findAllByAgendaIdAndDeletedFalse(agendaId);
        List<AiMaterialDto.FileItem> fileItems = files.stream()
                .map(f -> new AiMaterialDto.FileItem(f.getFileName(), f.getFileUrl()))
                .toList();

        // PDF 텍스트 추출
        List<String> extractedTexts = new ArrayList<>();
        for (File f : files) {
            String url = f.getFileUrl();
            if (url == null) continue;

            if (url.toLowerCase().endsWith(".pdf")) {
                try {
                    if (f.getS3Key() == null || f.getS3Key().isBlank()) continue;
                    byte[] bytes = s3Downloader.downloadByKey(f.getS3Key());
                    String text = pdfTextExtractor.extractText(bytes);
                    if (text != null && !text.isBlank()) {
                        extractedTexts.add(limitChars(text, 12000));
                    }
                } catch (Exception ignore) {
                    // 실패해도 요약 생성은 계속
                }
            }
        }

        return new AiMaterialDto.AgendaMaterials(
                agenda.getId(),
                agenda.getName(),
                agenda.getSequence(),
                voteItem,
                limitCommentsForPrompt(commentItems),
                limitFilesForPrompt(fileItems),
                extractedTexts
        );
    }

    // --------------------------------------
    // LLM 응답 파싱/정제
    // --------------------------------------
    private AiGenResponse parseAiGenResponse(String raw) {
        try {
            String cleaned = stripCodeFence(raw).trim();
            cleaned = extractJsonObjectIfWrapped(cleaned);
            AiGenResponse res = objectMapper.readValue(cleaned, AiGenResponse.class);

            // title/summaryText null 방어
            if (res == null) return new AiGenResponse("", "");
            return new AiGenResponse(
                    res.getTitle() == null ? "" : res.getTitle(),
                    res.getSummaryText() == null ? "" : res.getSummaryText()
            );
        } catch (Exception e) {
            throw new IllegalStateException("LLM JSON 파싱 실패. raw=" + raw, e);
        }
    }

    // ```json ... ``` 같은 코드펜스 제거
    private String stripCodeFence(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.startsWith("```")) {
            t = t.replaceFirst("^```[a-zA-Z]*\\s*", "");
            t = t.replaceFirst("\\s*```$", "");
        }
        return t;
    }

    // LLM이 앞/뒤에 설명을 붙여도 첫 { .. 마지막 }만 잘라서 JSON 파싱 시도
    private String extractJsonObjectIfWrapped(String s) {
        if (s == null) return "";
        int first = s.indexOf('{');
        int last = s.lastIndexOf('}');
        if (first >= 0 && last > first) {
            return s.substring(first, last + 1);
        }
        return s;
    }

    private String sanitizeTitle(String title) {
        if (title == null) return "";
        String t = title.replaceAll("[\"'#]", "").trim();
        if (t.length() > 20) t = t.substring(0, 20);
        return t;
    }

    // --------------------------------------
    // 기존 유틸
    // --------------------------------------
    private static String limitChars(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private static List<AiMaterialDto.CommentItem> limitCommentsForPrompt(List<AiMaterialDto.CommentItem> items) {
        if (items == null) return List.of();
        int limit = Math.min(items.size(), 30);
        List<AiMaterialDto.CommentItem> sliced = items.subList(items.size() - limit, items.size());
        return sliced.stream()
                .map(ci -> new AiMaterialDto.CommentItem(limitChars(ci.getContent(), 400)))
                .toList();
    }

    private static List<AiMaterialDto.FileItem> limitFilesForPrompt(List<AiMaterialDto.FileItem> items) {
        if (items == null) return List.of();
        int limit = Math.min(items.size(), 20);
        return items.subList(0, limit);
    }

    private void assertLlmOk(String text) {
        if (text == null || text.isBlank()) throw new IllegalStateException("LLM 응답이 비었습니다.");
        if (text.startsWith("404 에러") || text.startsWith("429 에러") || text.startsWith("서비스 오류")) {
            throw new IllegalStateException(text);
        }
    }

    // --------------------------------------
    // GET: 조회 (참여자 검증 포함)
    // --------------------------------------
    public AiDto.DetailAiResDto getAiSummary(Long agendaId, Long reqId) {
        Agenda a = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        User u = userRepository.findByIdAndDeletedFalse(reqId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        userRoomRepository.findActiveUserRoom(u.getId(), a.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 방 참여자가 아닙니다."));

        Ai ai = aiRepository.findActiveAiByAgendaId(a.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 AI요약입니다."));

        return AiDto.DetailAiResDto.from(ai);
    }

    // --------------------------------------
    // 내부 DTO: LLM JSON 응답 파싱용
    // --------------------------------------
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AiGenResponse {
        private String title;
        private String summaryText;
    }
}
