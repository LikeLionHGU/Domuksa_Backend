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
        final String json;
        try {
            json = objectMapper.writeValueAsString(materials);
        } catch (Exception e) {
            throw new IllegalStateException("materials json 직렬화 실패", e);
        }

        return """
        너는 기업의 수석 회의 서기다.
        아래 [입력 데이터(JSON)]만 근거로 "회의 결과 보고서"를 작성한다.
        반드시 아래 규칙을 지켜서 결과를 출력하라.

        =========================
        [절대 출력 규칙 - 위반 금지]
        1) 출력은 **오직 JSON 객체 1개**만 허용한다. (설명/코드펜스/추가 텍스트/주석 금지)
        2) JSON 키는 정확히 "title", "summaryText" 두 개만 출력한다. 다른 키 금지.
        3) title: 한국어 20자 이내, 따옴표(" '), 해시(#), 이모지 금지. "회의/보고서" 같은 뻔한 단어 금지.
        4) summaryText: 반드시 **마크다운(Markdown)** 문서이며, 아래 [문서 템플릿] 구조를 그대로 따른다.
        5) 입력 JSON에 없는 내용은 절대 생성(추측/상상/추가)하지 말 것.
        6) 의미 없는 랜덤 문자열/반복문자(예: asdasd, qweqwe, vnv...) 절대 금지.
        7) 모든 섹션은 "데이터가 없으면 없음"을 명확히 표기한다.
        =========================

        [분석 지침]
        - Vote가 있으면 "결과 요약 + 해석(수렴/갈림)"을 작성
        - Files/PDF 추출 텍스트가 있으면 "자료의 주장/근거/한계"만 정리(확대해석 금지)
        - Comments가 있으면 "논의 흐름/쟁점/요구사항/결정"을 댓글에서만 추출해 구조화
          * 댓글의 표현이 짧아도, 중복되는 키워드/문장으로 묶어서 "핵심 테마"로 정리
          * 누가 말했다(발화자 정보)가 없으면 사람 구분을 하지 말고 "의견 A/B"처럼 중립적으로 정리
          * 공격적/감정적 표현이 있더라도 평가하지 말고 사실만 기록

        [문서 템플릿 - summaryText는 반드시 이 구조]
        # 0. 안건 개요
        - 안건: (agendaName / sequence)
        - 요약 범위: 투표 / 댓글 / 자료(파일·PDF) 기반 정리

        ---
        # 1. 투표 결과 요약
        (투표 데이터가 없으면: "진행된 투표가 없습니다."만 출력)
        - 투표 주제:
        - 상태:
        - 결과:
          - 옵션명: 득표수
        - 해석:
          - **핵심 수렴 지점**:
          - **의견 갈림 지점**:

        ---
        # 2. 코멘트 기반 논의 요약
        (코멘트이 없으면: "등록된 코멘트이 없습니다."만 출력)
        - 논의 흐름:
          - (처음) ...
          - (중간) ...
          - (마무리) ...
        - **핵심 테마 Top 3**:
          1) 테마 1: (코멘트 근거 문장/요지)
          2) 테마 2: (코멘트 근거 문장/요지)
          3) 테마 3: (코멘트 근거 문장/요지)
        - 쟁점/질문:
          - 항목 1
          - 항목 2
        - 합의/결정(코멘트에 근거가 있을 때만):
          - 항목 1
          - 항목 2

        ---
        # 3. 업로드된 자료 검토 결과
        (자료가 없으면: "검토된 자료가 없습니다."만 출력)
        - 자료 요약:
          - 항목 1
          - 항목 2
        - 근거/한계(자료에 실제로 있는 내용만):
          - 항목 1
          - 항목 2
        - 비교/맥락(있을 때만):
          - 항목 1

        ---
        # 4. Final Summary
        > **[최종 결론]**
        > 한 문장으로 최종 합의/핵심 결론을 정리함. (근거는 투표/코멘트/자료 중 존재하는 것만)

        - **핵심 포인트**:
          - 항목 1
          - 항목 2
          - 항목 3
        - **결정 사항**:
          - 항목 1
          - 항목 2
        - **Next Steps**:
          - (우선순위 1) ...
          - (우선순위 2) ...
          - (우선순위 3) ...

        [출력 JSON 스키마 - 이 형태로만 출력]
        {
          "title": "여기에 20자 이내 제목",
          "summaryText": "여기에 위 템플릿을 따른 마크다운 보고서"
        }

        [입력 데이터(JSON)]
        """ + json;
    }
}
