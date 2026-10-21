package com.aicc.backend.websocket;

import com.aicc.backend.repository.ChatMessageRepository;
import com.aicc.backend.repository.ChatRoomRepository;
import com.aicc.backend.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatSessionTrackerTest {

    private ChatService chatService;
    private TaskScheduler scheduler;
    private ScheduledFuture<?> future;
    private ChatSessionTracker tracker;

    @BeforeEach
    void setUp() {
        chatService = mock(ChatService.class);
        scheduler = mock(TaskScheduler.class);
        future = mock(ScheduledFuture.class);
        doReturn(future).when(scheduler).schedule(any(Runnable.class), any(Instant.class));
        tracker = new ChatSessionTracker(chatService, mock(ChatRoomRepository.class),
                mock(ChatMessageRepository.class), scheduler, 60, 10);
    }

    private SessionSubscribeEvent subscribe(String session, String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setSessionId(session);
        accessor.setSubscriptionId("sub-0");
        accessor.setDestination(destination);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        return new SessionSubscribeEvent(this, message);
    }

    private SessionDisconnectEvent disconnect(String session) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId(session);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        return new SessionDisconnectEvent(this, message, session, CloseStatus.GOING_AWAY);
    }

    @Test
    void disconnectSchedulesAutoEndAndRunsIt() {
        tracker.onSubscribe(subscribe("s1", "/topic/rooms/7"));
        tracker.onDisconnect(disconnect("s1"));

        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(task.capture(), any(Instant.class));
        task.getValue().run();

        verify(chatService).autoEndRoom(7L);
    }

    @Test
    void resubscribeWithinGraceCancelsAutoEnd() {
        tracker.onSubscribe(subscribe("s1", "/topic/rooms/7"));
        tracker.onDisconnect(disconnect("s1"));
        tracker.onSubscribe(subscribe("s2", "/topic/rooms/7"));

        verify(future).cancel(false);
        assertThat(tracker.isConnected(7L)).isTrue();
    }

    @Test
    void roomStaysOpenWhileAnotherSessionIsConnected() {
        tracker.onSubscribe(subscribe("s1", "/topic/rooms/7"));
        tracker.onSubscribe(subscribe("s2", "/topic/rooms/7"));
        tracker.onDisconnect(disconnect("s1"));

        verify(scheduler, never()).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void disconnectOfUntrackedSessionIsIgnored() {
        tracker.onDisconnect(disconnect("unknown"));
        tracker.onSubscribe(subscribe("s1", "/topic/other"));
        tracker.onDisconnect(disconnect("s1"));

        verify(scheduler, never()).schedule(any(Runnable.class), any(Instant.class));
    }
}
