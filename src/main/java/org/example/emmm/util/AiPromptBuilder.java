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
너는 회의 안건 요약 비서다.
아래 JSON 자료를 근거로 한국어로 요약하라.

출력 형식:
1) 결론(1~2문장)
2) 핵심 근거 3~7개(불릿)
3) 합의된 사항 / 남은 쟁점
4) 다음 액션 아이템(할 일)

규칙:
- 자료에 없는 내용은 지어내지 말 것.
- 개인정보(전화번호/계좌/주소 등)는 요약에 포함하지 말 것.

자료(JSON):
""" + json;
    }
}
