package com.aicc.backend.service;

import com.aicc.backend.domain.Category;
import com.aicc.backend.domain.ChatMessage;
import com.aicc.backend.domain.ConsultationStatus;
import com.aicc.backend.domain.SenderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Gemini generateContent REST API를 호출한다. gemini.api-key가 있을 때만 등록되고,
 * 없으면 StubGeminiService가 동작한다. 호출이 실패하면 상담이 끊기지 않도록 안내 문구를 돌려준다.
 */
@Service
@ConditionalOnExpression("!'${gemini.api-key:}'.isBlank()")
public class GeminiApiService implements GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiApiService.class);

    private static final String REPLY_PROMPT = """
            당신은 AI·LLM 전문 상담 서비스의 상담원입니다. 한국어로 정중하고 간결하게 답하세요.
            상담 범위: LLM의 개념과 동작 원리, 프롬프트 작성, LLM API 활용, RAG·임베딩, 파인튜닝, 에이전트·도구 사용, 비용·성능, AI 안전·윤리.
            정책:
            - 범위 밖 질문(날씨, 일반 상식, 다른 서비스 장애 등)에는 상담 범위를 알려 주고 AI·LLM 관련 질문을 유도하세요.
            - 모르거나 최신 정보가 필요한 내용은 추측하지 말고 공식 문서 확인을 권하세요. 모델 가격·한도·버전은 자주 바뀐다고 알리세요.
            - 코드나 예시가 필요하면 짧게 보여 주세요.
            - 개인정보, API 키, 비밀번호를 요구하지 말고, 사용자가 올리면 노출 위험을 알려 주세요.
            - 해로운 목적(악성코드, 타인 사칭, 개인정보 유출 등)의 요청은 정중히 거절하세요.
            - 해결이 어렵거나 계정·결제처럼 직접 처리가 필요한 문의는 상담사 연결을 안내하세요.
            """;
    private static final String SUMMARY_PROMPT =
            "다음은 고객과 AI 상담원의 대화입니다. 먼저 첫 줄에 상담 해결 여부를 [해결] 또는 [미해결] 중 하나로만 쓰세요. "
                    + "고객의 문의에 필요한 답이나 안내가 대화 안에서 충분히 이루어졌으면 [해결], "
                    + "답을 얻지 못했거나 상담사 연결이 필요하거나 고객이 중간에 떠난 것으로 보이면 [미해결]입니다. ";
    private static final String CATEGORY_PROMPT =
            "그다음 줄에 상담 주제를 [분류: 이름] 형식으로 쓰세요. 이름은 아래 목록의 이름 중 하나만 고르고, "
                    + "대화의 핵심 주제와 가장 가까운 설명을 가진 것을 고르세요.\n%s\n";
    private static final String BODY_PROMPT =
            "그다음 줄부터 문의 내용, 처리 결과, 후속 조치가 있으면 그것을 한국어로 3문장 이내로 요약하세요.\n\n";
    private static final Pattern VERDICT = Pattern.compile("^\\s*\\[(해결|미해결)]\\s*");
    private static final Pattern CATEGORY = Pattern.compile("^\\s*\\[분류\\s*[:：]\\s*([^\\]]+)]\\s*");
    private static final String FAILURE_REPLY =
            "죄송합니다. 지금은 답변을 만들지 못했습니다. 잠시 후 다시 시도하거나 상담사 연결을 요청해 주세요.";
    private static final ConsultationSummary FAILURE_SUMMARY =
            new ConsultationSummary("(자동 요약에 실패했습니다)", ConsultationStatus.UNRESOLVED, null);

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiApiService(@Value("${gemini.base-url}") String baseUrl,
                            @Value("${gemini.api-key}") String apiKey,
                            @Value("${gemini.model}") String model) {
        // 응답이 지연돼도 상담 흐름이 멈추지 않게 타임아웃을 둔다.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(30_000);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String reply(List<ChatMessage> history) {
        List<Map<String, Object>> contents = new ArrayList<>();
        for (ChatMessage m : history) {
            // 대화는 고객 발언으로 시작해야 하므로 앞쪽의 AI 발언은 건너뛴다
            if (contents.isEmpty() && m.getSender() != SenderType.USER) {
                continue;
            }
            contents.add(textContent(m.getSender() == SenderType.USER ? "user" : "model", m.getContent()));
        }
        return generate(REPLY_PROMPT, contents, FAILURE_REPLY);
    }

    @Override
    public ConsultationSummary summarize(List<ChatMessage> messages, List<Category> categories) {
        String transcript = messages.stream()
                .map(m -> (m.getSender() == SenderType.USER ? "고객: " : "상담원: ") + m.getContent())
                .collect(Collectors.joining("\n"));
        String prompt = SUMMARY_PROMPT
                + (categories.isEmpty() ? "" : CATEGORY_PROMPT.formatted(categories.stream()
                        .map(c -> "- " + c.getName() + ": " + (c.getDescription() != null ? c.getDescription() : ""))
                        .collect(Collectors.joining("\n"))))
                + BODY_PROMPT + transcript;
        String answer = generate(null, List.of(textContent("user", prompt)), null);
        return answer == null || answer.isBlank() ? FAILURE_SUMMARY : parseSummary(answer);
    }

    /**
     * 첫 줄의 [해결]/[미해결], 이어지는 [분류: 이름] 표지를 읽고 나머지를 요약으로 쓴다.
     * 해결 표지가 없으면 확인할 수 없으므로 미해결로 두고, 분류 표지가 없으면 분류하지 않는다.
     */
    static ConsultationSummary parseSummary(String answer) {
        String rest = answer;
        ConsultationStatus status = ConsultationStatus.UNRESOLVED;
        Matcher verdict = VERDICT.matcher(rest);
        if (verdict.find()) {
            status = "해결".equals(verdict.group(1)) ? ConsultationStatus.RESOLVED : ConsultationStatus.UNRESOLVED;
            rest = rest.substring(verdict.end());
        }
        String categoryName = null;
        Matcher category = CATEGORY.matcher(rest);
        if (category.find()) {
            categoryName = category.group(1).strip();
            rest = rest.substring(category.end());
        }
        String text = rest.strip();
        return new ConsultationSummary(text.isEmpty() ? "(요약 없음)" : text, status, categoryName);
    }

    @SuppressWarnings("unchecked")
    private String generate(String systemPrompt, List<Map<String, Object>> contents, String fallback) {
        Map<String, Object> body = systemPrompt == null
                ? Map.of("contents", contents)
                : Map.of("system_instruction", textContent(null, systemPrompt),
                        "contents", contents);
        try {
            Map<String, Object> response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return parts.stream()
                    .filter(p -> p.get("text") instanceof String)
                    .map(p -> (String) p.get("text"))
                    .collect(Collectors.joining());
        } catch (Exception e) {
            // 한도 초과(429)나 네트워크 오류가 상담 흐름을 막지 않게 한다. 키는 로그에 남기지 않는다.
            log.warn("Gemini 호출 실패: {}", e.getClass().getSimpleName() + " " + e.getMessage());
            return fallback;
        }
    }

    private Map<String, Object> textContent(String role, String text) {
        Map<String, Object> content = new LinkedHashMap<>();
        if (role != null) {
            content.put("role", role);
        }
        content.put("parts", List.of(Map.of("text", text)));
        return content;
    }
}
