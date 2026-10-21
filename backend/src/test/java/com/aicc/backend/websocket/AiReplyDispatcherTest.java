package com.aicc.backend.websocket;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AiReplyDispatcherTest {

    @Test
    void sameRoomRunsInOrderOffTheCallerThread() throws Exception {
        AiReplyDispatcher dispatcher = new AiReplyDispatcher(4);
        List<String> order = Collections.synchronizedList(new java.util.ArrayList<>());
        CountDownLatch done = new CountDownLatch(3);

        dispatcher.submit(7L, () -> { sleep(150); order.add("first"); done.countDown(); });
        dispatcher.submit(7L, () -> { order.add("second"); done.countDown(); });
        dispatcher.submit(7L, () -> { throw new IllegalStateException("실패해도 다음 작업은 계속"); });
        dispatcher.submit(7L, () -> { order.add("fourth"); done.countDown(); });

        assertThat(done.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(order).containsExactly("first", "second", "fourth");
        dispatcher.shutdown();
    }

    @Test
    void differentLanesDoNotBlockEachOther() throws Exception {
        AiReplyDispatcher dispatcher = new AiReplyDispatcher(4);
        CountDownLatch slowStarted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch fastDone = new CountDownLatch(1);

        dispatcher.submit(4L, () -> { slowStarted.countDown(); await(release); });
        assertThat(slowStarted.await(2, TimeUnit.SECONDS)).isTrue();
        dispatcher.submit(5L, fastDone::countDown);

        assertThat(fastDone.await(2, TimeUnit.SECONDS)).isTrue();
        release.countDown();
        dispatcher.shutdown();
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static void await(CountDownLatch latch) {
        try { latch.await(3, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
