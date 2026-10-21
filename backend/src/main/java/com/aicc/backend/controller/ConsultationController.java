package com.aicc.backend.controller;

import com.aicc.backend.config.jwt.AuthUser;
import com.aicc.backend.domain.ConsultationStatus;
import com.aicc.backend.dto.common.CursorPageResponse;
import com.aicc.backend.dto.consultation.ConsultationDetailResponse;
import com.aicc.backend.dto.consultation.ConsultationCategoryRequest;
import com.aicc.backend.dto.consultation.ConsultationListItem;
import com.aicc.backend.dto.consultation.ConsultationStatusRequest;
import com.aicc.backend.service.ConsultationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            Authentication authentication,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ConsultationStatus status,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(consultationService.search(cursor, keyword, categoryId, status, size,
                AuthUser.userId(authentication), AuthUser.isAdmin(authentication)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationDetailResponse> detail(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(consultationService.getDetail(id,
                AuthUser.userId(authentication), AuthUser.isAdmin(authentication)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ConsultationListItem> updateStatus(Authentication authentication,
                                                             @PathVariable Long id,
                                                             @Valid @RequestBody ConsultationStatusRequest request) {
        return ResponseEntity.ok(consultationService.updateStatus(id, request.status(),
                AuthUser.userId(authentication), AuthUser.isAdmin(authentication)));
    }

    @PatchMapping("/{id}/category")
    public ResponseEntity<ConsultationListItem> updateCategory(Authentication authentication,
                                                               @PathVariable Long id,
                                                               @RequestBody ConsultationCategoryRequest request) {
        return ResponseEntity.ok(consultationService.updateCategory(id, request.categoryId(),
                AuthUser.userId(authentication), AuthUser.isAdmin(authentication)));
    }
}
