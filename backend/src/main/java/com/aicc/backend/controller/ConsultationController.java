package com.aicc.backend.controller;

import com.aicc.backend.domain.ConsultationStatus;
import com.aicc.backend.dto.common.CursorPageResponse;
import com.aicc.backend.dto.consultation.ConsultationDetailResponse;
import com.aicc.backend.dto.consultation.ConsultationListItem;
import com.aicc.backend.service.ConsultationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping
    public ResponseEntity<CursorPageResponse<ConsultationListItem>> list(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ConsultationStatus status,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(consultationService.search(cursor, keyword, categoryId, status, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(consultationService.getDetail(id));
    }
}
