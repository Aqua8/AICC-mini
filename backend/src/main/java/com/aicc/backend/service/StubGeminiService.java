package com.aicc.backend.service;

import com.aicc.backend.domain.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StubGeminiService implements GeminiService {

    @Override
    public String reply(String userMessage) {
        return "[로컬 스텁 응답] 문의해 주셔서 감사합니다. 실제 AI 응답은 서버 배포 후 Gemini 연동 시 제공됩니다.";
    }

    @Override
    public String summarize(List<ChatMessage> messages) {
        return "[로컬 스텁 요약] 총 " + messages.size() + "개의 메시지로 구성된 상담입니다. (Gemini 연동 후 자동 요약 예정)";
    }
}
