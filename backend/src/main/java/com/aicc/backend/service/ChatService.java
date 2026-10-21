package com.aicc.backend.service;

import com.aicc.backend.domain.*;
import com.aicc.backend.dto.chat.ChatMessageResponse;
import com.aicc.backend.dto.chat.ChatRoomResponse;
import com.aicc.backend.exception.ApiException;
import com.aicc.backend.repository.CategoryRepository;
import com.aicc.backend.repository.ChatMessageRepository;
import com.aicc.backend.repository.ChatRoomRepository;
import com.aicc.backend.repository.ConsultationRepository;
import com.aicc.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ConsultationRepository consultationRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final GeminiService geminiService;
    private final TransactionTemplate transactionTemplate;
    private final DailyStatsRefresher dailyStatsRefresher;

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatMessageRepository chatMessageRepository,
                       ConsultationRepository consultationRepository,
                       UserRepository userRepository,
                       CategoryRepository categoryRepository,
                       GeminiService geminiService,
                       TransactionTemplate transactionTemplate,
                       DailyStatsRefresher dailyStatsRefresher) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.consultationRepository = consultationRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.geminiService = geminiService;
        this.transactionTemplate = transactionTemplate;
        this.dailyStatsRefresher = dailyStatsRefresher;
    }

    @Transactional
    public ChatRoomResponse createRoom(Long userId, String title) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
        ChatRoom room = chatRoomRepository.save(ChatRoom.builder()
                .user(user)
                .title(title != null && !title.isBlank() ? title : "상담 " + java.time.LocalDate.now())
                .build());
        return toRoomResponse(room, List.of());
    }

    /** 방 주인이나 관리자만 볼 수 있다. */
    @Transactional(readOnly = true)
    public ChatRoomResponse getRoom(Long roomId, Long userId, boolean admin) {
        ChatRoom room = findRoom(roomId);
        requireAccess(roomId, userId, admin);
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);
        return toRoomResponse(room, messages);
    }

    /** 방이 있고 방 주인이거나 관리자면 true. WebSocket 구독·전송 검증에도 쓴다. */
    @Transactional(readOnly = true)
    public boolean canAccessRoom(Long roomId, Long userId, boolean admin) {
        if (userId == null || !chatRoomRepository.existsById(roomId)) {
            return false;
        }
        return admin || chatRoomRepository.existsByIdAndUser_Id(roomId, userId);
    }

    /** 고객 메시지를 저장한다. 방 주인의 진행 중인 상담에만 쓸 수 있다. */
    @Transactional
    public ChatMessage saveUserMessage(Long roomId, Long userId, String content) {
        ChatRoom room = findRoom(roomId);
        if (!chatRoomRepository.existsByIdAndUser_Id(roomId, userId)) {
            throw ApiException.forbidden("권한이 없습니다.");
        }
        if (room.getStatus() == RoomStatus.ENDED) {
            throw ApiException.badRequest("이미 종료된 상담입니다.");
        }
        return chatMessageRepository.save(ChatMessage.builder()
                .chatRoomId(roomId)
                .sender(SenderType.USER)
                .content(content)
                .build());
    }

    /** AI가 답을 만드는 사이 상담이 종료됐으면 저장하지 않는다. */
    @Transactional
    public Optional<ChatMessage> saveAiMessageIfActive(Long roomId, String content) {
        return chatRoomRepository.findById(roomId)
                .filter(room -> room.getStatus() == RoomStatus.ACTIVE)
                .map(room -> chatMessageRepository.save(ChatMessage.builder()
                        .chatRoomId(roomId)
                        .sender(SenderType.AI)
                        .content(content)
                        .build()));
    }

    /** 최근 대화를 맥락으로 AI 응답을 만든다. 외부 호출이 길 수 있어 트랜잭션을 열지 않는다. */
    public String generateReply(Long roomId) {
        List<ChatMessage> history = new ArrayList<>(chatMessageRepository.findTop20ByChatRoomIdOrderByIdDesc(roomId));
        Collections.reverse(history);
        return geminiService.reply(history);
    }

    /**
     * 종료 버튼으로 상담을 끝낸다. 상태 변경 → 요약(외부 호출, 트랜잭션 밖) → 이력 저장 순서로 나눠
     * Gemini 응답을 기다리는 동안 DB 커넥션을 붙잡지 않는다.
     */
    public ChatRoomResponse endRoom(Long roomId, Long userId) {
        ChatRoom room = findRoom(roomId);
        if (!chatRoomRepository.existsByIdAndUser_Id(room.getId(), userId)) {
            throw ApiException.forbidden("권한이 없습니다.");
        }
        if (!claimEnd(roomId)) {
            throw ApiException.badRequest("이미 종료된 상담입니다.");
        }
        return saveHistory(roomId, false);
    }

    /**
     * 사용자가 종료 버튼을 누르지 않고 떠난 상담을 시스템이 종료한다.
     * 이미 종료됐으면 아무것도 하지 않고, 고객 메시지가 없으면 이력을 남기지 않는다.
     */
    public void autoEndRoom(Long roomId) {
        if (claimEnd(roomId)) {
            saveHistory(roomId, true);
        }
    }

    /** ACTIVE → ENDED를 한 번에 선점한다. 종료 버튼과 자동 종료가 겹쳐도 한쪽만 true를 받는다. */
    private boolean claimEnd(Long roomId) {
        return chatRoomRepository.markEnded(roomId, LocalDateTime.now(), RoomStatus.ACTIVE, RoomStatus.ENDED) == 1;
    }

    private ChatRoomResponse saveHistory(Long roomId, boolean skipIfNoUserMessage) {
        ChatRoom room = findRoom(roomId);
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);
        boolean hasUserMessage = messages.stream().anyMatch(m -> m.getSender() == SenderType.USER);
        if (skipIfNoUserMessage && !hasUserMessage) {
            return toRoomResponse(room, messages);
        }

        List<Category> categories = categoryRepository.findAll();
        ConsultationSummary summary = messages.isEmpty()
                ? new ConsultationSummary("대화 내용이 없습니다.", ConsultationStatus.RESOLVED, null)
                : geminiService.summarize(messages, categories);
        Category category = summary.categoryName() == null ? null
                : categories.stream().filter(c -> c.getName().equals(summary.categoryName())).findFirst().orElse(null);

        transactionTemplate.executeWithoutResult(status -> consultationRepository.save(Consultation.builder()
                .chatRoomId(roomId)
                .category(category)
                .title(room.getTitle())
                .summary(summary.text())
                .status(summary.status())
                .build()));
        dailyStatsRefresher.refresh(LocalDate.now());
        return toRoomResponse(room, messages);
    }

    private void requireAccess(Long roomId, Long userId, boolean admin) {
        if (!admin && !chatRoomRepository.existsByIdAndUser_Id(roomId, userId)) {
            throw ApiException.forbidden("권한이 없습니다.");
        }
    }

    private ChatRoom findRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> ApiException.notFound("채팅방을 찾을 수 없습니다."));
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room, List<ChatMessage> messages) {
        return new ChatRoomResponse(
                room.getId(),
                room.getTitle(),
                room.getStatus(),
                room.getStartedAt(),
                room.getEndedAt(),
                messages.stream().map(ChatMessageResponse::from).toList());
    }
}
