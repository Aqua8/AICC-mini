package com.aicc.backend.service;

import com.aicc.backend.domain.ChatMessage;
import com.aicc.backend.domain.Consultation;
import com.aicc.backend.domain.ConsultationStatus;
import com.aicc.backend.dto.chat.ChatMessageResponse;
import com.aicc.backend.dto.common.CursorPageResponse;
import com.aicc.backend.dto.consultation.ConsultationDetailResponse;
import com.aicc.backend.dto.consultation.ConsultationListItem;
import com.aicc.backend.exception.ApiException;
import com.aicc.backend.domain.Category;
import com.aicc.backend.repository.CategoryRepository;
import com.aicc.backend.repository.ChatMessageRepository;
import com.aicc.backend.repository.ChatRoomRepository;
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
    private final ChatRoomRepository chatRoomRepository;
    private final CategoryRepository categoryRepository;
    private final DailyStatsRefresher dailyStatsRefresher;

    public ConsultationService(ConsultationRepository consultationRepository,
                               ChatMessageRepository chatMessageRepository,
                               ChatRoomRepository chatRoomRepository,
                               CategoryRepository categoryRepository,
                               DailyStatsRefresher dailyStatsRefresher) {
        this.consultationRepository = consultationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.categoryRepository = categoryRepository;
        this.dailyStatsRefresher = dailyStatsRefresher;
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<ConsultationListItem> search(Long cursor, String keyword,
                                                           Long categoryId, ConsultationStatus status, int size,
                                                           Long userId, boolean admin) {
        String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword : null;
        List<Consultation> rows = consultationRepository.searchByCursor(
                cursor, normalizedKeyword, categoryId, status, admin ? null : userId, Limit.of(size + 1));

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
    public ConsultationDetailResponse getDetail(Long id, Long userId, boolean admin) {
        Consultation c = findAccessible(id, userId, admin);
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(c.getChatRoomId());
        return new ConsultationDetailResponse(
                c.getId(),
                c.getChatRoomId(),
                c.getTitle(),
                c.getSummary(),
                c.getCategory() != null ? c.getCategory().getId() : null,
                c.getCategory() != null ? c.getCategory().getName() : null,
                c.getStatus(),
                c.getCreatedAt(),
                messages.stream().map(ChatMessageResponse::from).toList());
    }

    @Transactional
    public ConsultationListItem updateStatus(Long id, ConsultationStatus status, Long userId, boolean admin) {
        Consultation c = findAccessible(id, userId, admin);
        c.changeStatus(status);
        ConsultationListItem item = ConsultationListItem.from(c);
        consultationRepository.flush(); // 같은 트랜잭션에서 집계하므로 변경분을 먼저 DB에 반영
        dailyStatsRefresher.refresh(c.getCreatedAt().toLocalDate());
        return item;
    }

    @Transactional
    public ConsultationListItem updateCategory(Long id, Long categoryId, Long userId, boolean admin) {
        Consultation c = findAccessible(id, userId, admin);
        Category category = categoryId == null ? null : categoryRepository.findById(categoryId)
                .orElseThrow(() -> ApiException.notFound("카테고리를 찾을 수 없습니다."));
        c.changeCategory(category);
        return ConsultationListItem.from(c);
    }

    /** 관리자는 전부, 일반 사용자는 본인 상담의 이력만 볼 수 있다. */
    private Consultation findAccessible(Long id, Long userId, boolean admin) {
        Consultation c = consultationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("상담 이력을 찾을 수 없습니다."));
        if (!admin && !chatRoomRepository.existsByIdAndUser_Id(c.getChatRoomId(), userId)) {
            throw ApiException.forbidden("권한이 없습니다.");
        }
        return c;
    }
}
