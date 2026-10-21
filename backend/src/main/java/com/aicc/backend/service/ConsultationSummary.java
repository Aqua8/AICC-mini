package com.aicc.backend.service;

import com.aicc.backend.domain.ConsultationStatus;

/** 상담 종료 시 AI가 만든 요약, 해결 여부 판정, 분류. 분류를 알 수 없으면 categoryName은 null. */
public record ConsultationSummary(String text, ConsultationStatus status, String categoryName) {
}
