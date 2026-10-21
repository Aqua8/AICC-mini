package com.aicc.backend.service;

import com.aicc.backend.domain.Category;
import com.aicc.backend.domain.ChatMessage;
import com.aicc.backend.domain.ConsultationStatus;
import com.aicc.backend.domain.SenderType;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiApiServiceTest {

    private HttpServer server;
    private final AtomicReference<String> lastPath = new AtomicReference<>();
    private final AtomicReference<String> lastKey = new AtomicReference<>();
    private final AtomicReference<String> lastBody = new AtomicReference<>();
    private volatile int status = 200;

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", ex -> {
            lastPath.set(ex.getRequestURI().getPath());
            lastKey.set(ex.getRequestHeaders().getFirst("x-goog-api-key"));
            lastBody.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] res = (status == 200
                    ? "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"안녕하세요 고객님\"}]}}]}"
                    : "{\"error\":{\"message\":\"quota\"}}").getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(status, res.length);
            ex.getResponseBody().write(res);
            ex.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private static Category category(String name, String description) {
        return Category.builder().name(name).description(description).build();
    }

    private GeminiApiService service() {
        return new GeminiApiService("http://127.0.0.1:" + server.getAddress().getPort(), "k-123", "gemini-3.5-flash-lite");
    }

    @Test
    void replyParsesTextAndSendsKeyHeader() {
        String reply = service().reply(List.of(new ChatMessage(1L, SenderType.USER, "배송 언제 와요?")));

        assertThat(reply).isEqualTo("안녕하세요 고객님");
        assertThat(lastPath.get()).isEqualTo("/v1beta/models/gemini-3.5-flash-lite:generateContent");
        assertThat(lastKey.get()).isEqualTo("k-123");
        assertThat(lastBody.get()).contains("배송 언제 와요?").contains("system_instruction");
    }

    @Test
    void replySendsRecentHistoryWithRoles() {
        service().reply(List.of(
                new ChatMessage(1L, SenderType.AI, "앞쪽 AI 발언은 건너뜀"),
                new ChatMessage(1L, SenderType.USER, "RAG가 뭐예요?"),
                new ChatMessage(1L, SenderType.AI, "검색 결과를 붙여 답하는 방식입니다."),
                new ChatMessage(1L, SenderType.USER, "그럼 파인튜닝과 차이는요?")));

        String body = lastBody.get();
        assertThat(body).doesNotContain("앞쪽 AI 발언");
        assertThat(body).contains("\"role\":\"user\"").contains("\"role\":\"model\"");
        assertThat(body.indexOf("RAG가 뭐예요?")).isLessThan(body.indexOf("검색 결과를 붙여"));
        assertThat(body.indexOf("검색 결과를 붙여")).isLessThan(body.indexOf("파인튜닝과 차이는요?"));
    }

    @Test
    void summarizeIncludesTranscript() {
        List<ChatMessage> messages = List.of(
                new ChatMessage(1L, SenderType.USER, "환불하고 싶어요"),
                new ChatMessage(1L, SenderType.AI, "접수해 드렸습니다"));

        ConsultationSummary summary = service().summarize(messages, List.of(category("프롬프트 작성", "프롬프트 설계"), category("기타", "그 외")));

        assertThat(summary.text()).isEqualTo("안녕하세요 고객님");
        assertThat(summary.status()).isEqualTo(ConsultationStatus.UNRESOLVED); // 판정 표지가 없으면 미해결
        assertThat(lastBody.get()).contains("고객: 환불하고 싶어요").contains("상담원: 접수해 드렸습니다")
                .contains("- 프롬프트 작성: 프롬프트 설계").contains("- 기타: 그 외");
    }

    @Test
    void parseSummaryReadsVerdictMarker() {
        ConsultationSummary resolved = GeminiApiService.parseSummary("[해결]\n환불 접수를 안내했습니다.");
        assertThat(resolved.status()).isEqualTo(ConsultationStatus.RESOLVED);
        assertThat(resolved.text()).isEqualTo("환불 접수를 안내했습니다.");

        ConsultationSummary unresolved = GeminiApiService.parseSummary("[미해결] 상담사 연결이 필요합니다.");
        assertThat(unresolved.status()).isEqualTo(ConsultationStatus.UNRESOLVED);
        assertThat(unresolved.text()).isEqualTo("상담사 연결이 필요합니다.");
        assertThat(unresolved.categoryName()).isNull();
    }

    @Test
    void parseSummaryReadsCategoryMarker() {
        ConsultationSummary summary = GeminiApiService.parseSummary("[해결]\n[분류: RAG·검색]\n임베딩 방식을 안내했습니다.");

        assertThat(summary.status()).isEqualTo(ConsultationStatus.RESOLVED);
        assertThat(summary.categoryName()).isEqualTo("RAG·검색");
        assertThat(summary.text()).isEqualTo("임베딩 방식을 안내했습니다.");
    }

    @Test
    void summarizeFailureIsUnresolved() {
        status = 503;

        ConsultationSummary summary = service().summarize(List.of(new ChatMessage(1L, SenderType.USER, "문의")), List.of());

        assertThat(summary.status()).isEqualTo(ConsultationStatus.UNRESOLVED);
        assertThat(summary.text()).contains("요약에 실패");
    }

    @Test
    void failureReturnsFallbackInsteadOfThrowing() {
        status = 429;

        assertThat(service().reply(List.of(new ChatMessage(1L, SenderType.USER, "문의")))).contains("답변을 만들지 못했습니다");
    }
}
