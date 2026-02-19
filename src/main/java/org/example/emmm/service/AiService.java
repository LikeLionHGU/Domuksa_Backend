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


    @Transactional
    public AiDto.CreateAiResDto createAi(Long agendaId, Long userId) {

        // 1) Agenda 확인
        Agenda agenda = agendaRepository.findByIdAndDeletedFalse(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 안건입니다."));

        User u = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 2) 권한/참여자 검증 (최소: 해당 Room 참여자인지)
        UserRoom ur = userRoomRepository.findActiveUserRoom(u.getId(), agenda.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 방 참여자가 아닙니다."));

        // 필요하면 호스트만 요약 생성 허용
        if (!"host".equals(ur.getRole())) {
            throw new IllegalArgumentException("호스트만 AI 요약을 생성할 수 있습니다.");
        }

        // 2-1) AgendaConfig.aiSummaryEnabled 토글 (기능이 생성되면 enabled=true로)
        AgendaConfig ac = agenda.getConfig();
        if (ac != null && (ac.getAiSummaryEnabled() == null || !ac.getAiSummaryEnabled())) {
            ac.setAiSummaryEnabled(true);
            agendaConfigRepository.save(ac);
        }

        // 3) Vote + VoteOption(selectCount)
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

        // 4) Comments 처리 ✅ (단순화됨)
        List<Comment> comments = commentRepository.findAllActiveByAgendaId(agendaId);
        List<AiMaterialDto.CommentItem> commentItems;

        if (comments.isEmpty()) {
            commentItems = List.of();
        } else {
            commentItems = comments.stream()
                    .map(c -> new AiMaterialDto.CommentItem(c.getContent()))
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

            if (url.toLowerCase().endsWith(".pdf")) {
                try {
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
                limitCommentsForPrompt(commentItems),
                limitFilesForPrompt(fileItems),
                extractedTexts
        );

// 8-1) 요약용 Prompt 생성
        String summaryPrompt = promptBuilder.buildSummaryPrompt(materials);

        // 9-1) LLM 호출 (요약 생성)
        String summary = llmClient.summarizeText(summaryPrompt);

        // 8-2) 제목용 Prompt 생성 (생성된 요약을 바탕으로)
        String titlePrompt = promptBuilder.buildTitlePrompt(summary);

        // 9-2) LLM 호출 (제목 생성)
        String title = llmClient.summarizeText(titlePrompt);

        // (혹시 모를 따옴표나 공백 제거)
        if (title != null) {
            title = title.replaceAll("[\"']", "").trim();
        }

        // 10) AISummary upsert
        Ai ai = aiRepository.findActiveAiByAgendaId(agendaId).orElse(null);
        if (ai == null) {
            ai = Ai.builder()
                    .agenda(agenda)
                    .title(title)
                    .summaryText(summary)
                    .build();
        } else {
            ai.setTitle(title);
            ai.setSummaryText(summary);
        }

        Ai saved = aiRepository.save(ai);
        return AiDto.CreateAiResDto.from(saved);
    }

    private static String limitChars(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private static List<AiMaterialDto.CommentItem> limitCommentsForPrompt(List<AiMaterialDto.CommentItem> items) {
        if (items == null) return List.of();
        int limit = Math.min(items.size(), 30); // comment 단위 상한
        List<AiMaterialDto.CommentItem> sliced = items.subList(items.size() - limit, items.size());

        return sliced.stream()
                .map(ci -> new AiMaterialDto.CommentItem(
                        limitChars(ci.getContent(), 400) // 내용 길이를 400자로 컷
                ))
                .toList();
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