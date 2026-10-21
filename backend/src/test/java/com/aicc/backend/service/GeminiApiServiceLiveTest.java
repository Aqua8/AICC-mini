package com.aicc.backend.service;

import com.aicc.backend.domain.Category;
import com.aicc.backend.domain.ChatMessage;
import com.aicc.backend.domain.ConsultationStatus;
import com.aicc.backend.domain.DefaultCategories;
import com.aicc.backend.domain.SenderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 실제 Gemini API를 호출한다. GEMINI_API_KEY가 있을 때만 실행된다. */
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiApiServiceLiveTest {

    private static final List<Category> CATEGORIES = DefaultCategories.ITEMS.stream()
            .map(i -> Category.builder().name(i.name()).description(i.description()).build()).toList();

    private final GeminiApiService service = new GeminiApiService(
            "https://generativelanguage.googleapis.com",
            System.getenv("GEMINI_API_KEY"),
            "gemini-3.5-flash-lite");

    @Test
    void replyAnswersWithinTopic() {
        String answer = service.reply(List.of(new ChatMessage(1L, SenderType.USER, "RAG와 파인튜닝 중 뭘 써야 하나요?")));
        System.out.println("[LIVE reply] " + answer);
        assertThat(answer).isNotBlank().doesNotContain("답변을 만들지 못했습니다");
    }

    @Test
    void replyUsesPreviousTurns() {
        String answer = service.reply(List.of(
                new ChatMessage(1L, SenderType.USER, "RAG가 뭐예요? 한 문장으로 알려 주세요."),
                new ChatMessage(1L, SenderType.AI, "RAG는 외부 문서를 검색해 답변에 반영하는 방식입니다."),
                new ChatMessage(1L, SenderType.USER, "방금 말한 그것의 단점을 하나만 알려 주세요.")));
        System.out.println("[LIVE context] " + answer);
        assertThat(answer).isNotBlank().doesNotContain("답변을 만들지 못했습니다");
    }

    @Test
    void replyRedirectsOffTopicQuestion() {
        String answer = service.reply(List.of(new ChatMessage(1L, SenderType.USER, "오늘 서울 날씨 어때요? 그리고 점심 메뉴 추천해 주세요.")));
        System.out.println("[LIVE off-topic] " + answer);
        assertThat(answer).isNotBlank().doesNotContain("답변을 만들지 못했습니다");
    }

    @Test
    void hallucinationQuestionIsClassifiedAsLlmBasics() {
        ConsultationSummary summary = service.summarize(List.of(
                new ChatMessage(1L, SenderType.USER, "LLM이 없는 사실을 지어내는 환각은 왜 생기나요?"),
                new ChatMessage(1L, SenderType.AI, "LLM은 다음 토큰을 확률적으로 예측하기 때문에 근거가 없어도 그럴듯한 문장을 만들 수 있습니다."),
                new ChatMessage(1L, SenderType.USER, "아, 그렇군요. 이해했습니다.")), CATEGORIES);
        System.out.println("[LIVE hallucination] " + summary);
        assertThat(summary.categoryName()).isEqualTo("LLM 기초");
    }

    @Test
    void summarizeJudgesStatusAndCategory() {
        ConsultationSummary resolved = service.summarize(List.of(
                new ChatMessage(1L, SenderType.USER, "벡터 DB에 문서를 넣고 검색해서 답하게 하려면 어떻게 하나요?"),
                new ChatMessage(1L, SenderType.AI, "문서를 청크로 나눠 임베딩한 뒤 벡터 DB에 저장하고, 질문 임베딩으로 유사 문서를 검색해 프롬프트에 넣습니다."),
                new ChatMessage(1L, SenderType.USER, "이해했어요, 감사합니다.")), CATEGORIES);
        ConsultationSummary unresolved = service.summarize(List.of(
                new ChatMessage(1L, SenderType.USER, "API 결제 카드가 계속 거절돼서 키가 비활성화됐어요. 지금 바로 풀어 주세요."),
                new ChatMessage(1L, SenderType.AI, "계정 결제는 직접 처리할 수 없습니다. 상담사 연결을 요청해 주세요."),
                new ChatMessage(1L, SenderType.USER, "그럼 해결이 안 된 거잖아요.")), CATEGORIES);
        System.out.println("[LIVE resolved] " + resolved);
        System.out.println("[LIVE unresolved] " + unresolved);
        assertThat(resolved.status()).isEqualTo(ConsultationStatus.RESOLVED);
        assertThat(resolved.categoryName()).isEqualTo("RAG·검색");
        assertThat(unresolved.status()).isEqualTo(ConsultationStatus.UNRESOLVED);
        assertThat(DefaultCategories.names()).contains(unresolved.categoryName());
    }
}
