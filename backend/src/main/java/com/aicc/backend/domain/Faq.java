package com.aicc.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "faq", indexes = {
        @Index(name = "idx_faq_category", columnList = "category_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Builder
    public Faq(Category category, String question, String answer) {
        this.category = category;
        this.question = question;
        this.answer = answer;
    }

    public void update(Category category, String question, String answer) {
        this.category = category;
        this.question = question;
        this.answer = answer;
    }
}
