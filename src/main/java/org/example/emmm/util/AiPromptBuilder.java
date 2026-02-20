package org.example.emmm.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.emmm.dto.AiMaterialDto;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiPromptBuilder {

    private final ObjectMapper objectMapper;

    public String buildSummaryPrompt(AiMaterialDto.AgendaMaterials materials) {
        String json;
        try {
            json = objectMapper.writeValueAsString(materials);
        } catch (Exception e) {
            throw new IllegalStateException("materials json 직렬화 실패", e);
        }

        return """
                당신은 기업의 수석 회의 서기입니다.
                제공된 JSON 데이터만 근거로 아래 형식의 JSON을 **오직 JSON만** 출력하세요.
                (설명/코드펜스/추가 텍스트 금지)
                
                [출력 JSON 스키마]
                {
                  "title": "20자 이내 핵심 제목",
                  "summaryText": "마크다운 회의 결과 보고서 전체"
                }
                
                [title 규칙]
                1) 20자 이내
                2) 따옴표/해시(#) 포함 금지
                3) 구체적인 안건/결정이 드러나게
                
                [summaryText 규칙]
                1) 마크다운 문법 엄격 준수
                2) 섹션(#) 사이에 --- 구분선 삽입
                3) 핵심은 **굵게**
                4) 중요한 합의는 > 블록 인용
                5) "~함/~됨" 개조식 톤
                6) 입력 JSON에 없는 내용은 절대 지어내지 말 것
                
                [보고서 형식]
                # 📊 1. 투표 결과 분석
                ---
                # 📂 2. 주요 참고 자료 검토
                ---
                # 💡 3. Final Summary (종합 결론)
                
                [입력 데이터(JSON)]
                """ + json;
    }
}
