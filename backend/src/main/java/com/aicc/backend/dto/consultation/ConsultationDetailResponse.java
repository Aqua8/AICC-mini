package com.aicc.backend.dto.consultation;

import com.aicc.backend.domain.ConsultationStatus;
import com.aicc.backend.dto.chat.ChatMessageResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ConsultationDetailResponse(
        Long id,
        Long chatRoomId,
        String title,
        String summary,
        String categoryName,
        ConsultationStatus status,
        LocalDateTime createdAt,
        List<ChatMessageResponse> messages
) {
}
