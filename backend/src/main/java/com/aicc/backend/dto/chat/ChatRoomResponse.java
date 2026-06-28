package com.aicc.backend.dto.chat;

import com.aicc.backend.domain.RoomStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ChatRoomResponse(
        Long id,
        String title,
        RoomStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        List<ChatMessageResponse> messages
) {
}
