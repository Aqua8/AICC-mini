package com.aicc.backend.dto.chat;

public record ChatSendRequest(
        Long roomId,
        String content
) {
}
