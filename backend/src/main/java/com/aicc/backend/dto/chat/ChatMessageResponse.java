package com.aicc.backend.dto.chat;

import com.aicc.backend.domain.ChatMessage;
import com.aicc.backend.domain.SenderType;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        SenderType sender,
        String content,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage m) {
        return new ChatMessageResponse(m.getId(), m.getSender(), m.getContent(), m.getCreatedAt());
    }
}
