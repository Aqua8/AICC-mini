package com.aicc.backend.service;

import com.aicc.backend.domain.*;
import com.aicc.backend.dto.chat.ChatRoomResponse;
import com.aicc.backend.dto.common.CursorPageResponse;
import com.aicc.backend.dto.consultation.ConsultationListItem;
import com.aicc.backend.exception.ApiException;
import com.aicc.backend.repository.ChatMessageRepository;
import com.aicc.backend.repository.CategoryRepository;
import com.aicc.backend.repository.ConsultationRepository;
import com.aicc.backend.repository.DailyStatsRepository;
import com.aicc.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 키 없는 테스트 프로파일(스텁 Gemini)에서 종료·자동 종료·권한 흐름을 H2로 검증한다. */
@ActiveProfiles("test")
@SpringBootTest
class ChatConsultationFlowTest {

    @Autowired ChatService chatService;
    @Autowired ConsultationService consultationService;
    @Autowired UserRepository userRepository;
    @Autowired ConsultationRepository consultationRepository;
    @Autowired ChatMessageRepository chatMessageRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired DailyStatsRepository dailyStatsRepository;

    private Long owner;
    private Long other;

    @BeforeEach
    void users() {
        owner = newUser();
        other = newUser();
    }

    private Long newUser() {
        return userRepository.save(User.builder()
                .email(UUID.randomUUID() + "@test.com").password("x").name("t").role(Role.CUSTOMER).build()).getId();
    }

    private Long roomWithConversation() {
        Long roomId = chatService.createRoom(owner, "테스트 상담").id();
        chatService.saveUserMessage(roomId, owner, "RAG가 뭐예요?");
        chatService.saveAiMessageIfActive(roomId, "검색 결과를 붙여 답하는 방식입니다.");
        return roomId;
    }

    private long consultationCount(Long roomId) {
        return consultationRepository.findAll().stream().filter(c -> c.getChatRoomId().equals(roomId)).count();
    }

    @Test
    void endRoomSavesHistoryOnceAndRejectsSecondEnd() {
        Long roomId = roomWithConversation();

        ChatRoomResponse ended = chatService.endRoom(roomId, owner);

        assertThat(ended.status()).isEqualTo(RoomStatus.ENDED);
        assertThat(consultationCount(roomId)).isEqualTo(1);
        assertThatThrownBy(() -> chatService.endRoom(roomId, owner)).isInstanceOf(ApiException.class);
        chatService.autoEndRoom(roomId);
        assertThat(consultationCount(roomId)).isEqualTo(1);
    }

    @Test
    void autoEndSavesHistoryButSkipsEmptyRooms() {
        Long talked = roomWithConversation();
        Long empty = chatService.createRoom(owner, "빈 상담").id();

        chatService.autoEndRoom(talked);
        chatService.autoEndRoom(empty);

        assertThat(consultationCount(talked)).isEqualTo(1);
        assertThat(consultationCount(empty)).isZero();
        assertThat(chatService.getRoom(empty, owner, false).status()).isEqualTo(RoomStatus.ENDED);
    }

    @Test
    void endedRoomRejectsNewMessages() {
        Long roomId = roomWithConversation();
        chatService.endRoom(roomId, owner);

        assertThatThrownBy(() -> chatService.saveUserMessage(roomId, owner, "또 질문"))
                .isInstanceOf(ApiException.class);
        assertThat(chatService.saveAiMessageIfActive(roomId, "늦은 답변")).isEmpty();
    }

    @Test
    void roomAccessIsLimitedToOwnerAndAdmin() {
        Long roomId = roomWithConversation();

        assertThat(chatService.getRoom(roomId, owner, false).id()).isEqualTo(roomId);
        assertThat(chatService.getRoom(roomId, other, true).id()).isEqualTo(roomId);
        assertThatThrownBy(() -> chatService.getRoom(roomId, other, false)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> chatService.saveUserMessage(roomId, other, "남의 방")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> chatService.endRoom(roomId, other)).isInstanceOf(ApiException.class);
        assertThat(chatService.canAccessRoom(roomId, owner, false)).isTrue();
        assertThat(chatService.canAccessRoom(roomId, other, false)).isFalse();
        assertThat(chatService.canAccessRoom(roomId, other, true)).isTrue();
        assertThat(chatService.canAccessRoom(999_999L, owner, true)).isFalse();
    }

    @Test
    void generateReplyUsesRecentHistoryInOrder() {
        Long roomId = roomWithConversation();

        assertThat(chatService.generateReply(roomId)).isNotBlank();
        assertThat(chatMessageRepository.findTop20ByChatRoomIdOrderByIdDesc(roomId)).hasSize(2);
    }

    @Test
    void consultationsAreVisibleToOwnerAndAdminOnly() {
        Long roomId = roomWithConversation();
        chatService.endRoom(roomId, owner);
        Long consultationId = consultationRepository.findAll().stream()
                .filter(c -> c.getChatRoomId().equals(roomId)).findFirst().orElseThrow().getId();

        CursorPageResponse<ConsultationListItem> mine = consultationService.search(null, null, null, null, 50, owner, false);
        CursorPageResponse<ConsultationListItem> others = consultationService.search(null, null, null, null, 50, other, false);
        CursorPageResponse<ConsultationListItem> admin = consultationService.search(null, null, null, null, 50, other, true);

        assertThat(mine.content()).extracting(ConsultationListItem::id).contains(consultationId);
        assertThat(others.content()).extracting(ConsultationListItem::id).doesNotContain(consultationId);
        assertThat(admin.content()).extracting(ConsultationListItem::id).contains(consultationId);

        assertThat(consultationService.getDetail(consultationId, owner, false).id()).isEqualTo(consultationId);
        assertThatThrownBy(() -> consultationService.getDetail(consultationId, other, false)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> consultationService.updateStatus(consultationId, ConsultationStatus.UNRESOLVED, other, false))
                .isInstanceOf(ApiException.class);
        assertThat(consultationService.updateStatus(consultationId, ConsultationStatus.UNRESOLVED, owner, false).status())
                .isEqualTo(ConsultationStatus.UNRESOLVED);
        assertThat(consultationService.updateStatus(consultationId, ConsultationStatus.RESOLVED, other, true).status())
                .isEqualTo(ConsultationStatus.RESOLVED);
    }

    @Test
    void categoryCanBeChangedByOwnerOrAdminOnly() {
        Long roomId = roomWithConversation();
        chatService.endRoom(roomId, owner);
        Long consultationId = consultationRepository.findAll().stream()
                .filter(c -> c.getChatRoomId().equals(roomId)).findFirst().orElseThrow().getId();
        Long categoryId = categoryRepository.findByName("프롬프트 작성").orElseThrow().getId();

        assertThat(consultationService.updateCategory(consultationId, categoryId, owner, false).categoryName())
                .isEqualTo("프롬프트 작성");
        assertThat(consultationService.getDetail(consultationId, owner, false).categoryId()).isEqualTo(categoryId);
        assertThatThrownBy(() -> consultationService.updateCategory(consultationId, categoryId, other, false))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> consultationService.updateCategory(consultationId, 999_999L, owner, false))
                .isInstanceOf(ApiException.class);
        assertThat(consultationService.updateCategory(consultationId, null, other, true).categoryName()).isNull();
    }

    @Test
    void dailyStatsFollowNewConsultationsAndStatusChangesImmediately() {
        LocalDate today = LocalDate.now();
        long before = dailyStatsRepository.findByStatDateBetweenOrderByStatDateAsc(today, today).stream()
                .mapToLong(DailyStats::getTotalCount).sum();
        Long roomId = roomWithConversation();
        chatService.endRoom(roomId, owner);

        DailyStats afterEnd = dailyStatsRepository.findByStatDateBetweenOrderByStatDateAsc(today, today).get(0);
        assertThat(afterEnd.getTotalCount()).isEqualTo(before + 1);

        Long consultationId = consultationRepository.findAll().stream()
                .filter(c -> c.getChatRoomId().equals(roomId)).findFirst().orElseThrow().getId();
        long unresolvedBefore = afterEnd.getUnresolvedCount();
        consultationService.updateStatus(consultationId, ConsultationStatus.UNRESOLVED, owner, false);
        consultationService.updateStatus(consultationId, ConsultationStatus.UNRESOLVED, owner, false);

        DailyStats afterChange = dailyStatsRepository.findByStatDateBetweenOrderByStatDateAsc(today, today).get(0);
        assertThat(afterChange.getTotalCount()).isEqualTo(before + 1);
        assertThat(afterChange.getUnresolvedCount()).isEqualTo(unresolvedBefore + 1); // 스텁 요약은 해결로 저장됨
        assertThat(afterChange.getResolvedCount() + afterChange.getUnresolvedCount() + afterChange.getInProgressCount())
                .isEqualTo(afterChange.getTotalCount());
    }
}
