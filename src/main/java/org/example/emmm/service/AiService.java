package org.example.emmm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.*;
import org.example.emmm.dto.AiDto;
import org.example.emmm.dto.AiMaterialDto;
import org.example.emmm.repository.*;
import org.example.emmm.util.AiPromptBuilder;
import org.example.emmm.util.LlmClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final CommentOptionRepository commentOptionRepository;

    private final FileRepository fileRepository;

    private final AiPromptBuilder promptBuilder;
    private final LlmClient llmClient;

    private final S3Downloader s3Downloader;
    private final PdfTextExtractor pdfTextExtractor;
    private final UserRepository userRepository;


    @Transactional
    public AiDto.CreateAiResDto createAi(Long agendaId, Long userId) {

        // 1) Agenda 확인
        Agenda agenda = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        User u = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 2) 권한/참여자 검증 (최소: 해당 Room 참여자인지)
        UserRoom ur = userRoomRepository.findActiveUserRoom(u, agenda.getRoom())
                .orElseThrow(() -> new IllegalArgumentException("해당 방 참여자가 아닙니다."));

        // 필요하면 호스트만 요약 생성 허용
        if (!"host".equals(ur.getRole())) {
            throw new IllegalArgumentException("호스트만 AI 요약을 생성할 수 있습니다.");
        }

        // 2-1) AgendaConfig.aiSummaryEnabled 토글 (기능이 생성되면 enabled=true로)
        AgendaConfig ac = agenda.getConfig();
        if (ac != null && (ac.getAiSummaryEnabled() == null || !ac.getAiSummaryEnabled())) {
            ac.setAiSummaryEnabled(true);
            // CommentService/VoteService 패턴과 동일하게 명시 save
            agendaConfigRepository.save(ac);
        }


        // 3) Vote + VoteOption(selectCount)
        AiMaterialDto.VoteItem voteItem = null;
        Vote vote = voteRepository.findActiveByAgendaId(agendaId).orElse(null);

        if (vote != null) {
            List<VoteOption> options = voteOptionRepository.findAllActiveByVoteId(vote.getId());
            // ✅ DB의 selectCount가 항상 최신이라는 보장이 없어서, 요약에서는 "실제 선택 수"를 즉시 계산
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

        // 4) Comments + CommentOption(contents 전부)
        List<Comment> comments = commentRepository.findAllActiveByAgendaId(agendaId);
        List<AiMaterialDto.CommentItem> commentItems;

        if (comments.isEmpty()) {
            commentItems = List.of();
        } else {
            List<Long> commentIds = comments.stream().map(Comment::getId).toList();
            // ✅ deleted=false 필터 버전 사용 (삭제된 옵션이 요약에 섞이지 않게)
            List<CommentOption> commentOptions = commentOptionRepository.findAllActiveByCommentIdIn(commentIds);

            // commentId -> contents 리스트로 묶기
            Map<Long, List<String>> map = new HashMap<>();
            for (CommentOption co : commentOptions) {
                map.computeIfAbsent(co.getComment().getId(), k -> new ArrayList<>())
                        .add(co.getContent());
            }

            commentItems = comments.stream()
                    .map(c -> new AiMaterialDto.CommentItem(
                            c.getTitle(),
                            map.getOrDefault(c.getId(), List.of())
                    ))
                    .toList();
        }

        // 5) Files(url 전부)
        List<File> files = fileRepository.findAllByAgendaIdAndDeletedFalse(agendaId);
        List<AiMaterialDto.FileItem> fileItems = files.stream()
                .map(f -> new AiMaterialDto.FileItem(f.getFileName(), f.getFileUrl()))
                .toList();

        // 6) (선택) PDF 텍스트 추출해서 materials에 포함
        List<String> extractedTexts = new ArrayList<>();
        for (File f : files) {
            String url = f.getFileUrl();
            if (url == null) continue;

            // 아주 단순한 판별: 확장자 기반 (너희 저장 규칙에 맞춰 고도화 가능)
            if (url.toLowerCase().endsWith(".pdf")) {
                try {
                    // ✅ URL이 아니라 File.s3Key로 다운받는게 제일 안전함
                    // (S3 url 포맷이 바뀌거나, 서명 URL을 쓰는 경우에도 s3Key면 안정적)
                    if (f.getS3Key() == null || f.getS3Key().isBlank()) {
                        continue;
                    }
                    byte[] bytes = s3Downloader.downloadByKey(f.getS3Key());
                    String text = pdfTextExtractor.extractText(bytes);
                    if (!text.isBlank()) extractedTexts.add(limitChars(text, 12000));
                } catch (Exception ignore) {
                    // 실패해도 전체 요약이 죽지 않게
                }
            }
        }

        // 7) materials 구성
        AiMaterialDto.AgendaMaterials materials = new AiMaterialDto.AgendaMaterials(
                agenda.getId(),
                agenda.getName(),
                agenda.getSequence(),
                voteItem,
                limitCommentsForPrompt(commentItems), // 길이 안전장치
                limitFilesForPrompt(fileItems),
                extractedTexts
        );

        // 8) prompt 생성
        String prompt = promptBuilder.buildSummaryPrompt(materials);

        // 9) LLM 호출
        String summary = llmClient.summarizeText(prompt);

        // 10) AISummary upsert(update/insert)
        Ai ai = aiRepository.findActiveAiByAgendaId(agendaId).orElse(null);
        if (ai == null) {
            ai = Ai.builder()
                    .agenda(agenda)
                    .summaryText(summary)
                    .build();
        } else {
            ai.setSummaryText(summary);
        }

        Ai saved = aiRepository.save(ai);
        return AiDto.CreateAiResDto.from(saved);
    }

    private static String limitChars(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    // 댓글 “전부”는 수집하되, prompt에는 안전하게 컷 (실패 방지)
    private static List<AiMaterialDto.CommentItem> limitCommentsForPrompt(List<AiMaterialDto.CommentItem> items) {
        if (items == null) return List.of();
        int limit = Math.min(items.size(), 30); // comment 단위 상한
        List<AiMaterialDto.CommentItem> sliced = items.subList(items.size() - limit, items.size());

        // 각 comment의 contents도 너무 길면 컷
        return sliced.stream().map(ci -> new AiMaterialDto.CommentItem(
                ci.getTitle(),
                ci.getContents().stream().map(c -> limitChars(c, 400)).toList()
        )).toList();
    }

    private static List<AiMaterialDto.FileItem> limitFilesForPrompt(List<AiMaterialDto.FileItem> items) {
        if (items == null) return List.of();
        int limit = Math.min(items.size(), 20);
        return items.subList(0, limit);
    }

    public AiDto.DetailAiResDto getAiSummary(Long agendaId, Long reqId) {
        Agenda a = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        User u = userRepository.findByIdAndDeletedFalse(reqId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Ai ai = aiRepository.findActiveAiByAgendaId(a.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 AI요약입니다."));

        return AiDto.DetailAiResDto.from(ai);
    }
}
