package com.aicc.backend.websocket;

import com.aicc.backend.domain.ChatMessage;
import com.aicc.backend.domain.ChatRoom;
import com.aicc.backend.domain.RoomStatus;
import com.aicc.backend.repository.ChatMessageRepository;
import com.aicc.backend.repository.ChatRoomRepository;
import com.aicc.backend.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 채팅방 WebSocket 연결을 추적해, 종료 버튼 없이 창을 닫은 상담을 자동으로 종료·저장한다.
 * 연결이 모두 끊기면 유예 시간 뒤에 종료하고, 그 안에 다시 구독하면(새로고침 등) 취소한다.
 * 서버 재시작 등으로 추적하지 못한 방은 주기적으로 점검해 오래 비활성이면 종료한다.
 */
@Component
public class ChatSessionTracker {

    private static final Logger log = LoggerFactory.getLogger(ChatSessionTracker.class);
    private static final Pattern ROOM_TOPIC = Pattern.compile("^/topic/rooms/(\\d+)$");

    private final ChatService chatService;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TaskScheduler taskScheduler;
    private final Duration grace;
    private final Duration idle;

    private final Map<String, Long> sessionRooms = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> pendingEnds = new ConcurrentHashMap<>();

    public ChatSessionTracker(ChatService chatService,
                              ChatRoomRepository chatRoomRepository,
                              ChatMessageRepository chatMessageRepository,
                              TaskScheduler taskScheduler,
                              @Value("${chat.auto-end.grace-seconds:60}") long graceSeconds,
                              @Value("${chat.auto-end.idle-minutes:10}") long idleMinutes) {
        this.chatService = chatService;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.taskScheduler = taskScheduler;
        this.grace = Duration.ofSeconds(graceSeconds);
        this.idle = Duration.ofMinutes(idleMinutes);
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        if (destination == null || accessor.getSessionId() == null) {
            return;
        }
        Matcher matcher = ROOM_TOPIC.matcher(destination);
        if (!matcher.matches()) {
            return;
        }
        Long roomId = Long.valueOf(matcher.group(1));
        sessionRooms.put(accessor.getSessionId(), roomId);
        ScheduledFuture<?> pending = pendingEnds.remove(roomId);
        if (pending != null) {
            pending.cancel(false);
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        Long roomId = sessionRooms.remove(event.getSessionId());
        if (roomId != null && !isConnected(roomId)) {
            scheduleEnd(roomId);
        }
    }

    boolean isConnected(Long roomId) {
        return sessionRooms.containsValue(roomId);
    }

    private void scheduleEnd(Long roomId) {
        ScheduledFuture<?> future = taskScheduler.schedule(() -> endIfStillGone(roomId), Instant.now().plus(grace));
        ScheduledFuture<?> previous = pendingEnds.put(roomId, future);
        if (previous != null) {
            previous.cancel(false);
        }
    }

    void endIfStillGone(Long roomId) {
        pendingEnds.remove(roomId);
        if (isConnected(roomId)) {
            return;
        }
        autoEnd(roomId);
    }

    @Scheduled(initialDelayString = "${chat.auto-end.sweep-interval-ms:60000}",
            fixedDelayString = "${chat.auto-end.sweep-interval-ms:60000}")
    public void sweepIdleRooms() {
        LocalDateTime threshold = LocalDateTime.now().minus(idle);
        for (ChatRoom room : chatRoomRepository.findByStatus(RoomStatus.ACTIVE)) {
            Long roomId = room.getId();
            if (isConnected(roomId) || pendingEnds.containsKey(roomId)) {
                continue;
            }
            List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);
            LocalDateTime lastActivity = messages.isEmpty()
                    ? room.getStartedAt()
                    : messages.get(messages.size() - 1).getCreatedAt();
            if (lastActivity.isBefore(threshold)) {
                autoEnd(roomId);
            }
        }
    }

    private void autoEnd(Long roomId) {
        try {
            chatService.autoEndRoom(roomId);
            log.info("연결이 끊긴 상담 #{}을 자동 종료했습니다.", roomId);
        } catch (Exception e) {
            log.warn("상담 #{} 자동 종료 실패: {}", roomId, e.getMessage());
        }
    }
}
