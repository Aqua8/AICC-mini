package com.aicc.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation", indexes = {
        @Index(name = "idx_consultation_status", columnList = "status"),
        @Index(name = "idx_consultation_category", columnList = "category_id"),
        @Index(name = "idx_consultation_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsultationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Consultation(Long chatRoomId, Category category, String title, String summary, ConsultationStatus status) {
        this.chatRoomId = chatRoomId;
        this.category = category;
        this.title = title;
        this.summary = summary;
        this.status = status != null ? status : ConsultationStatus.IN_PROGRESS;
    }

    public void resolve(String summary, Category category) {
        this.summary = summary;
        if (category != null) {
            this.category = category;
        }
        this.status = ConsultationStatus.RESOLVED;
    }

    public void changeCategory(Category category) {
        this.category = category;
    }

    public void changeStatus(ConsultationStatus status) {
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
