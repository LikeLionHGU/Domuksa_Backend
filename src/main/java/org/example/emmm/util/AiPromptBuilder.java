package org.example.emmm.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.AiMaterialDto;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiPromptBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String buildSummaryPrompt(AiMaterialDto.AgendaMaterials materials) {
        String json;
        try {
            json = objectMapper.writeValueAsString(materials);
        } catch (Exception e) {
            throw new IllegalStateException("materials json 직렬화 실패", e);
        }

        return """
            당신은 기업의 수석 회의 서기이자 데이터 분석가입니다.
            제공된 JSON 데이터(투표 결과, 댓글 토론, 업로드된 파일 내용)를 바탕으로 전문적인 '안건 결과 보고서'를 작성하세요.
            
            반드시 아래 [출력 형식]을 엄격하게 따르세요. 한국어로 작성하십시오.
            
            ---
            [출력 형식]
            
            # 1. 투표 결과 요약
            (투표 데이터인 'vote' 필드가 없다면 "진행된 투표가 없습니다."라고 출력)
            - 투표 결과를 단순히 숫자로 나열하지 말고, **팀원들의 의견이 어떤 방향으로 수렴되었는지** 분석하여 서술형으로 요약.
            - 가장 많은 선택을 받은 항목과 그 의미를 강조.
            
            # 2. 업로드된 자료 검토 결과
            (파일 데이터인 'fileExtractedTexts' 필드가 비어있다면 "검토된 자료가 없습니다."라고 출력)
            - 첨부된 파일들(PDF 등)에서 추출된 핵심 내용을 요약.
            - 자료가 시사하는 바가 무엇인지 3개 내외의 불릿 포인트로 정리.
            
            # 3. Final Summary (종합 결론)
            - 댓글('comments')과 위의 내용들을 종합하여 최종 결론을 도출.
            - 논의된 쟁점, 합의된 사항, 그리고 앞으로의 해결 과제(Next Step)를 명확히 기술.
            - 문장은 "~함", "~됨" 등의 개조식 서술형 어미 사용.
            
            ---
            [제약 사항]
            1. 제공된 JSON 데이터에 없는 내용은 절대 지어내지 말 것.
            2. 마크다운(Markdown) 문법을 사용할 것 (제목은 #, 소제목은 ##, 리스트는 - 사용).
            3. 감정적인 표현을 배제하고 객관적이고 건조한 톤 유지.
            
            [입력 데이터(JSON)]
            """ + json;
    }

    public String buildTitlePrompt(String summaryText) {
        return """
            아래 제공된 '회의 요약본'을 읽고, 이 회의 내용을 가장 잘 대변하는 **핵심 제목**을 하나 지어주세요.
            
            [제약 사항]
            1. 제목은 20자 이내로 간결하게 작성할 것.
            2. "회의 결과 보고서" 같은 뻔한 제목보다는, 구체적인 안건이나 결정 사항이 드러나게 작성할 것.
            3. 따옴표(" ")나 마크다운(#) 없이 오직 텍스트만 출력할 것.
            4. 한국어로 작성할 것.
            
            [회의 요약본]
            """ + summaryText;
    }
}