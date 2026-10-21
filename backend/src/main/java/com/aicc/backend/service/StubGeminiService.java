package com.aicc.backend.service;

import com.aicc.backend.domain.Category;
import com.aicc.backend.domain.ChatMessage;
import com.aicc.backend.domain.ConsultationStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.List;

/** gemini.api-key가 비어 있을 때만 쓰는 로컬용 응답. */
@Service
@ConditionalOnExpression("'${gemini.api-key:}'.isBlank()")
public class StubGeminiService implements GeminiService {

    @Override
    public String reply(List<ChatMessage> history) {
        return "[로컬 스텁 응답] 문의해 주셔서 감사합니다. 실제 AI 응답은 서버 배포 후 Gemini 연동 시 제공됩니다.";
    }

    @Override
    public ConsultationSummary summarize(List<ChatMessage> messages, List<Category> categories) {
        return new ConsultationSummary(
                "[로컬 스텁 요약] 총 " + messages.size() + "개의 메시지로 구성된 상담입니다. (Gemini 연동 후 자동 요약 예정)",
                ConsultationStatus.RESOLVED,
                categories.isEmpty() ? null : categories.get(categories.size() - 1).getName());
    }
}
