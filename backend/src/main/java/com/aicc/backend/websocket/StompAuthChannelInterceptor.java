package com.aicc.backend.websocket;

import com.aicc.backend.config.jwt.AuthUser;
import com.aicc.backend.config.jwt.JwtTokenProvider;
import com.aicc.backend.service.ChatService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * STOMP 프레임 단위 인증·인가.
 * CONNECT에서 JWT를 검증해 사용자를 세션에 붙이고, SUBSCRIBE는 자기 상담방(관리자는 전체)만,
 * SEND는 로그인한 사용자만 허용한다. 거부되면 STOMP ERROR 프레임이 나가고 연결이 닫힌다.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern ROOM_TOPIC = Pattern.compile("^/topic/rooms/(\\d+)$");

    private final JwtTokenProvider tokenProvider;
    private final ChatService chatService;

    public StompAuthChannelInterceptor(JwtTokenProvider tokenProvider, ChatService chatService) {
        this.tokenProvider = tokenProvider;
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor != null ? accessor.getCommand() : null;
        if (command == null) {
            return message;
        }
        switch (command) {
            case CONNECT -> connect(accessor);
            case SUBSCRIBE -> subscribe(accessor);
            case SEND -> requireUser(accessor);
            default -> {
            }
        }
        return message;
    }

    private void connect(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
        Authentication authentication = tokenProvider.authenticate(token);
        if (authentication == null) {
            throw new MessageDeliveryException("인증이 필요합니다.");
        }
        accessor.setUser(authentication);
    }

    private void subscribe(StompHeaderAccessor accessor) {
        Authentication authentication = requireUser(accessor);
        String destination = accessor.getDestination();
        Matcher matcher = destination != null ? ROOM_TOPIC.matcher(destination) : null;
        if (matcher == null || !matcher.matches()) {
            throw new MessageDeliveryException("구독할 수 없는 경로입니다.");
        }
        Long roomId = Long.valueOf(matcher.group(1));
        if (!chatService.canAccessRoom(roomId, AuthUser.userId(authentication), AuthUser.isAdmin(authentication))) {
            throw new MessageDeliveryException("이 상담방에 접근할 수 없습니다.");
        }
    }

    private Authentication requireUser(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof Authentication authentication) {
            return authentication;
        }
        throw new MessageDeliveryException("인증이 필요합니다.");
    }
}
