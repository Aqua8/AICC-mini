package com.aicc.backend.websocket;

import com.aicc.backend.config.jwt.AuthUser;
import com.aicc.backend.domain.ChatMessage;
import com.aicc.backend.dto.chat.ChatBroadcast;
import com.aicc.backend.dto.chat.ChatSendRequest;
import com.aicc.backend.exception.ApiException;
import com.aicc.backend.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AiReplyDispatcher aiReplyDispatcher;

    public ChatWebSocketController(ChatService chatService,
                                   SimpMessagingTemplate messagingTemplate,
                                   AiReplyDispatcher aiReplyDispatcher) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
        this.aiReplyDispatcher = aiReplyDispatcher;
    }

    @MessageMapping("/chat")
    public void handle(ChatSendRequest request, Principal principal) {
        Long userId = AuthUser.userId(principal instanceof Authentication a ? a : null);
        if (userId == null || request.roomId() == null
                || request.content() == null || request.content().isBlank()) {
            return;
        }
        Long roomId = request.roomId();
        String destination = "/topic/rooms/" + roomId;

        ChatMessage userMessage;
        try {
            userMessage = chatService.saveUserMessage(roomId, userId, request.content());
        } catch (ApiException e) {
            log.warn("상담 #{} 메시지 거부(user {}): {}", roomId, userId, e.getMessage());
            return;
        }
        messagingTemplate.convertAndSend(destination, toBroadcast(roomId, userMessage));

        // Gemini 응답은 오래 걸릴 수 있어 메시지 처리 스레드를 붙잡지 않고 별도 레인에서 만든다
        aiReplyDispatcher.submit(roomId, () -> {
            String reply = chatService.generateReply(roomId);
            chatService.saveAiMessageIfActive(roomId, reply)
                    .ifPresent(ai -> messagingTemplate.convertAndSend(destination, toBroadcast(roomId, ai)));
        });
    }

    private ChatBroadcast toBroadcast(Long roomId, ChatMessage m) {
        return new ChatBroadcast(roomId, m.getSender(), m.getContent(), m.getCreatedAt());
    }
}
