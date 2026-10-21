package com.aicc.backend.websocket;

import com.aicc.backend.config.jwt.JwtTokenProvider;
import com.aicc.backend.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StompAuthChannelInterceptorTest {

    private JwtTokenProvider tokenProvider;
    private ChatService chatService;
    private StompAuthChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider("test-secret-key-that-is-long-enough-for-hmac-sha-256-algorithm-0123", 60_000);
        chatService = mock(ChatService.class);
        interceptor = new StompAuthChannelInterceptor(tokenProvider, chatService);
    }

    private Message<byte[]> frame(StompCommand command, String destination, String authHeader,
                                  UsernamePasswordAuthenticationToken user) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setSessionId("s1");
        if (destination != null) {
            accessor.setDestination(destination);
        }
        if (authHeader != null) {
            accessor.setNativeHeader("Authorization", authHeader);
        }
        if (user != null) {
            accessor.setUser(user);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private UsernamePasswordAuthenticationToken customer(long id) {
        return new UsernamePasswordAuthenticationToken(id, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    @Test
    void connectWithValidTokenAttachesUser() {
        String token = tokenProvider.createToken(5L, "a@test.com", "CUSTOMER");
        Message<?> in = frame(StompCommand.CONNECT, null, "Bearer " + token, null);

        interceptor.preSend(in, null);

        assertThat(StompHeaderAccessor.wrap(in).getUser()).isNotNull();
        assertThat(((UsernamePasswordAuthenticationToken) StompHeaderAccessor.wrap(in).getUser()).getPrincipal()).isEqualTo(5L);
    }

    @Test
    void connectWithoutOrWithBadTokenIsRejected() {
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, null, null, null), null))
                .isInstanceOf(MessageDeliveryException.class);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, null, "Bearer garbage", null), null))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void subscribeIsLimitedToAccessibleRooms() {
        when(chatService.canAccessRoom(7L, 5L, false)).thenReturn(true);
        when(chatService.canAccessRoom(8L, 5L, false)).thenReturn(false);

        interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/rooms/7", null, customer(5)), null);

        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/rooms/8", null, customer(5)), null))
                .isInstanceOf(MessageDeliveryException.class);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/other", null, customer(5)), null))
                .isInstanceOf(MessageDeliveryException.class);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/rooms/7", null, null), null))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void sendRequiresLogin() {
        interceptor.preSend(frame(StompCommand.SEND, "/app/chat", null, customer(5)), null);

        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SEND, "/app/chat", null, null), null))
                .isInstanceOf(MessageDeliveryException.class);
    }
}
