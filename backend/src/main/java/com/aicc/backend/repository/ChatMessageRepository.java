package com.aicc.backend.repository;

import com.aicc.backend.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    /** 최근 메시지 20개(최신순). 맥락으로 쓸 때는 뒤집어서 시간순으로 만든다. */
    List<ChatMessage> findTop20ByChatRoomIdOrderByIdDesc(Long chatRoomId);
}
