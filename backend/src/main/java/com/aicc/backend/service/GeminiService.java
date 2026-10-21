package com.aicc.backend.service;

import com.aicc.backend.domain.Category;
import com.aicc.backend.domain.ChatMessage;

import java.util.List;

/**
 * gemini.api-key가 있으면 GeminiApiService(실제 호출), 없으면 StubGeminiService가 동작한다.
 */
public interface GeminiService {

    /** history는 시간순 최근 대화이며 마지막이 고객의 새 메시지다. */
    String reply(List<ChatMessage> history);

    /** categories 중에서 상담 분류를 고르게 한다. 비어 있으면 분류하지 않는다. */
    ConsultationSummary summarize(List<ChatMessage> messages, List<Category> categories);
}
