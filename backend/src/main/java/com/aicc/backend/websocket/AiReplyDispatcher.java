package com.aicc.backend.websocket;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI 응답 생성을 WebSocket 메시지 처리 스레드 밖에서 실행한다.
 * 같은 방의 작업은 항상 같은 레인(단일 스레드)에서 순서대로 실행해 응답 순서를 지키고,
 * 레인 수만큼만 동시에 Gemini를 호출한다.
 */
@Component
public class AiReplyDispatcher {

    private static final Logger log = LoggerFactory.getLogger(AiReplyDispatcher.class);

    private final ExecutorService[] lanes;

    public AiReplyDispatcher(@Value("${chat.ai-reply.lanes:4}") int laneCount) {
        this.lanes = new ExecutorService[Math.max(1, laneCount)];
        for (int i = 0; i < lanes.length; i++) {
            int index = i;
            lanes[i] = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "ai-reply-" + index);
                t.setDaemon(true);
                return t;
            });
        }
    }

    public void submit(Long roomId, Runnable task) {
        lanes[(int) (roomId % lanes.length)].execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.warn("상담 #{} AI 응답 처리 실패: {}", roomId, e.getMessage());
            }
        });
    }

    @PreDestroy
    void shutdown() {
        for (ExecutorService lane : lanes) {
            lane.shutdownNow();
        }
    }
}
