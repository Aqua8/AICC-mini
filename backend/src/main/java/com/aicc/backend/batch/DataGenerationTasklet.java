package com.aicc.backend.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class DataGenerationTasklet implements Tasklet {

    private static final int ROOMS = 50_000;
    private static final int MESSAGES_PER_ROOM = 20;
    private static final int BATCH_SIZE = 1_000;
    private static final int HISTORY_DAYS = 90;

    private static final String[] USER_SAMPLES = {
            "배송 언제 오나요?", "환불 받고 싶어요", "상품이 불량이에요", "주문을 취소하고 싶습니다",
            "교환 가능한가요?", "쿠폰 사용이 안돼요", "결제가 두 번 됐어요", "비밀번호를 잊어버렸어요"
    };
    private static final String[] AI_SAMPLES = {
            "확인해 드리겠습니다. 주문번호를 알려주시겠어요?", "환불 절차를 안내해 드리겠습니다.",
            "불편을 드려 죄송합니다. 교환 접수를 도와드릴게요.", "주문 취소가 정상 처리되었습니다.",
            "네, 수령 후 7일 이내 교환 가능합니다.", "쿠폰 유효기간을 확인해 보겠습니다."
    };
    private static final String[] CATEGORY_NAMES = {"배송", "환불", "교환", "결제", "기술지원", "기타"};
    private static final String[] STATUSES = {"RESOLVED", "UNRESOLVED", "IN_PROGRESS"};

    private final JdbcTemplate jdbcTemplate;

    public DataGenerationTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        long generatorUserId = ensureGeneratorUser();
        List<Long> categoryIds = ensureCategories();
        long baseRoomId = currentMaxId("chat_rooms");

        insertRooms(generatorUserId);
        insertConsultations(baseRoomId, categoryIds);
        long inserted = insertMessages(baseRoomId);

        log.info("Data generation complete: {} rooms, {} messages", ROOMS, inserted);
        return RepeatStatus.FINISHED;
    }

    private long ensureGeneratorUser() {
        Long id = jdbcTemplate.query(
                "SELECT id FROM users WHERE email = ? LIMIT 1",
                rs -> rs.next() ? rs.getLong(1) : null,
                "batch-generator@aicc.local");
        if (id != null) {
            return id;
        }
        jdbcTemplate.update(
                "INSERT INTO users (email, password, name, role, created_at) VALUES (?, ?, ?, ?, ?)",
                "batch-generator@aicc.local", "{noop}disabled", "배치생성", "CUSTOMER",
                Timestamp.valueOf(LocalDateTime.now()));
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?", Long.class, "batch-generator@aicc.local");
    }

    private List<Long> ensureCategories() {
        List<Long> ids = jdbcTemplate.queryForList("SELECT id FROM categories", Long.class);
        if (!ids.isEmpty()) {
            return ids;
        }
        for (String name : CATEGORY_NAMES) {
            jdbcTemplate.update("INSERT INTO categories (name, description) VALUES (?, ?)", name, name + " 관련 문의");
        }
        return jdbcTemplate.queryForList("SELECT id FROM categories", Long.class);
    }

    private long currentMaxId(String table) {
        Long max = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) FROM " + table, Long.class);
        return max != null ? max : 0L;
    }

    private void insertRooms(long userId) {
        String sql = "INSERT INTO chat_rooms (user_id, title, status, started_at, ended_at) VALUES (?, ?, ?, ?, ?)";
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        for (int i = 0; i < ROOMS; i++) {
            batch.add(new Object[]{userId, "상담 #" + (i + 1), "ENDED", now, now});
            if (batch.size() == BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
        }
    }

    private void insertConsultations(long baseRoomId, List<Long> categoryIds) {
        String sql = "INSERT INTO consultation (chat_room_id, category_id, title, summary, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < ROOMS; i++) {
            long roomId = baseRoomId + 1 + i;
            Long categoryId = categoryIds.get(rnd.nextInt(categoryIds.size()));
            String status = STATUSES[rnd.nextInt(STATUSES.length)];
            LocalDateTime createdAt = LocalDateTime.now().minusDays(rnd.nextInt(HISTORY_DAYS))
                    .minusMinutes(rnd.nextInt(1440));
            batch.add(new Object[]{roomId, categoryId, "상담 #" + (i + 1),
                    "고객 문의 자동 생성 요약입니다.", status, Timestamp.valueOf(createdAt)});
            if (batch.size() == BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
        }
    }

    private long insertMessages(long baseRoomId) {
        String sql = "INSERT INTO chat_messages (chat_room_id, sender, content, created_at) VALUES (?, ?, ?, ?)";
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        long total = (long) ROOMS * MESSAGES_PER_ROOM;
        for (long i = 0; i < total; i++) {
            long roomId = baseRoomId + 1 + (i % ROOMS);
            boolean isUser = (i % 2) == 0;
            String content = isUser
                    ? USER_SAMPLES[rnd.nextInt(USER_SAMPLES.length)]
                    : AI_SAMPLES[rnd.nextInt(AI_SAMPLES.length)];
            LocalDateTime createdAt = LocalDateTime.now().minusDays(rnd.nextInt(HISTORY_DAYS));
            batch.add(new Object[]{roomId, isUser ? "USER" : "AI", content, Timestamp.valueOf(createdAt)});
            if (batch.size() == BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
        }
        return total;
    }
}
