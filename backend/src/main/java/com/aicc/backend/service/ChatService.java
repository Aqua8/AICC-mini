package com.aicc.backend.service;

import com.aicc.backend.domain.*;
import com.aicc.backend.dto.chat.ChatMessageResponse;
import com.aicc.backend.dto.chat.ChatRoomResponse;
import com.aicc.backend.exception.ApiException;
import com.aicc.backend.repository.ChatMessageRepository;
import com.aicc.backend.repository.ChatRoomRepository;
import com.aicc.backend.repository.ConsultationRepository;
import com.aicc.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ConsultationRepository consultationRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatMessageRepository chatMessageRepository,
                       ConsultationRepository consultationRepository,
                       UserRepository userRepository,
                       GeminiService geminiService) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.consultationRepository = consultationRepository;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
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

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoom(Long roomId) {
        ChatRoom room = findRoom(roomId);
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);
        return toRoomResponse(room, messages);
    }

    @Transactional
    public ChatMessage saveUserMessage(Long roomId, String content) {
        findRoom(roomId);
        return chatMessageRepository.save(ChatMessage.builder()
                .chatRoomId(roomId)
                .sender(SenderType.USER)
                .content(content)
                .build());
    }

    @Transactional
    public ChatMessage saveAiMessage(Long roomId, String content) {
        return chatMessageRepository.save(ChatMessage.builder()
                .chatRoomId(roomId)
                .sender(SenderType.AI)
                .content(content)
                .build());
    }

    public String generateReply(String userMessage) {
        return geminiService.reply(userMessage);
    }

    @Transactional
    public ChatRoomResponse endRoom(Long roomId) {
        ChatRoom room = findRoom(roomId);
        if (room.getStatus() == RoomStatus.ENDED) {
            throw ApiException.badRequest("이미 종료된 상담입니다.");
        }
        room.end();

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);
        String summary = messages.isEmpty() ? "대화 내용이 없습니다." : geminiService.summarize(messages);

        consultationRepository.save(Consultation.builder()
                .chatRoomId(roomId)
                .title(room.getTitle())
                .summary(summary)
                .status(ConsultationStatus.RESOLVED)
                .build());

        return toRoomResponse(room, messages);
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
