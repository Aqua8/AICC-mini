package com.aicc.backend.controller;

import com.aicc.backend.config.jwt.AuthUser;
import org.springframework.security.core.Authentication;
import com.aicc.backend.dto.chat.ChatRoomResponse;
import com.aicc.backend.dto.chat.CreateRoomRequest;
import com.aicc.backend.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(@AuthenticationPrincipal Long userId,
                                                       @RequestBody(required = false) CreateRoomRequest request) {
        String title = request != null ? request.title() : null;
        return ResponseEntity.ok(chatService.createRoom(userId, title));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<ChatRoomResponse> getRoom(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(chatService.getRoom(id, AuthUser.userId(authentication), AuthUser.isAdmin(authentication)));
    }

    @PostMapping("/rooms/{id}/end")
    public ResponseEntity<ChatRoomResponse> endRoom(@AuthenticationPrincipal Long userId,
                                                    @PathVariable Long id) {
        return ResponseEntity.ok(chatService.endRoom(id, userId));
    }
}
