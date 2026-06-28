package com.aicc.backend.dto.chat;

import com.aicc.backend.domain.SenderType;

import java.time.LocalDateTime;

public record ChatBroadcast(
        Long roomId,
        SenderType sender,
        String content,
        LocalDateTime createdAt
) {
}
