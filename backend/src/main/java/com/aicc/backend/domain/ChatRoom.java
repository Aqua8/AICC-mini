package com.aicc.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms", indexes = {
        @Index(name = "idx_chat_rooms_user", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Builder
    public ChatRoom(User user, String title) {
        this.user = user;
        this.title = title;
        this.status = RoomStatus.ACTIVE;
    }

    public void end() {
        this.status = RoomStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }

    @PrePersist
    void onCreate() {
        this.startedAt = LocalDateTime.now();
    }
}
