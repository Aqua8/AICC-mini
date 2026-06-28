package com.aicc.backend.service;

import com.aicc.backend.domain.ChatMessage;
import com.aicc.backend.domain.Consultation;
import com.aicc.backend.domain.ConsultationStatus;
import com.aicc.backend.dto.chat.ChatMessageResponse;
import com.aicc.backend.dto.common.CursorPageResponse;
import com.aicc.backend.dto.consultation.ConsultationDetailResponse;
import com.aicc.backend.dto.consultation.ConsultationListItem;
import com.aicc.backend.exception.ApiException;
import com.aicc.backend.repository.ChatMessageRepository;
import com.aicc.backend.repository.ConsultationRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ConsultationService(ConsultationRepository consultationRepository,
                               ChatMessageRepository chatMessageRepository) {
        this.consultationRepository = consultationRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<ConsultationListItem> search(Long cursor, String keyword,
                                                           Long categoryId, ConsultationStatus status, int size) {
        String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword : null;
        List<Consultation> rows = consultationRepository.searchByCursor(
                cursor, normalizedKeyword, categoryId, status, Limit.of(size + 1));

        boolean hasNext = rows.size() > size;
        List<Consultation> page = hasNext ? rows.subList(0, size) : rows;

        List<ConsultationListItem> content = new ArrayList<>(page.size());
        for (Consultation c : page) {
            content.add(ConsultationListItem.from(c));
        }
        Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public ConsultationDetailResponse getDetail(Long id) {
        Consultation c = consultationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("상담 이력을 찾을 수 없습니다."));
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(c.getChatRoomId());
        return new ConsultationDetailResponse(
                c.getId(),
                c.getChatRoomId(),
                c.getTitle(),
                c.getSummary(),
                c.getCategory() != null ? c.getCategory().getName() : null,
                c.getStatus(),
                c.getCreatedAt(),
                messages.stream().map(ChatMessageResponse::from).toList());
    }
}
