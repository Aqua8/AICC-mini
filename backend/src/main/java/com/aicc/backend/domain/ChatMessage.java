package com.aicc.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_room_id", columnList = "chat_room_id"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_room_created", columnList = "chat_room_id, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SenderType sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ChatMessage(Long chatRoomId, SenderType sender, String content) {
        this.chatRoomId = chatRoomId;
        this.sender = sender;
        this.content = content;
    }

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
