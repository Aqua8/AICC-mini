package com.aicc.backend.dto.consultation;

import com.aicc.backend.domain.ConsultationStatus;
import jakarta.validation.constraints.NotNull;

public record ConsultationStatusRequest(@NotNull ConsultationStatus status) {
}
