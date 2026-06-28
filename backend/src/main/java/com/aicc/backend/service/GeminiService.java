package com.aicc.backend.service;

import com.aicc.backend.domain.ChatMessage;

import java.util.List;

/**
 * 로컬에서는 스텁으로 동작한다. 실제 Gemini API 호출은 서버 배포 후 구현 예정.
 */
public interface GeminiService {

    String reply(String userMessage);

    String summarize(List<ChatMessage> messages);
}
