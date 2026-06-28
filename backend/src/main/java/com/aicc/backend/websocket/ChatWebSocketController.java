package com.aicc.backend.websocket;

import com.aicc.backend.domain.ChatMessage;
import com.aicc.backend.dto.chat.ChatBroadcast;
import com.aicc.backend.dto.chat.ChatSendRequest;
import com.aicc.backend.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    public void handle(ChatSendRequest request) {
        Long roomId = request.roomId();
        String destination = "/topic/rooms/" + roomId;

        ChatMessage userMessage = chatService.saveUserMessage(roomId, request.content());
        messagingTemplate.convertAndSend(destination, toBroadcast(roomId, userMessage));

        String reply = chatService.generateReply(request.content());
        ChatMessage aiMessage = chatService.saveAiMessage(roomId, reply);
        messagingTemplate.convertAndSend(destination, toBroadcast(roomId, aiMessage));
    }

    private ChatBroadcast toBroadcast(Long roomId, ChatMessage m) {
        return new ChatBroadcast(roomId, m.getSender(), m.getContent(), m.getCreatedAt());
    }
}
