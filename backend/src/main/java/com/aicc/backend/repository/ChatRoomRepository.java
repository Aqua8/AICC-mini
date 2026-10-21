package com.aicc.backend.repository;

import com.aicc.backend.domain.ChatRoom;
import com.aicc.backend.domain.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByStatus(RoomStatus status);

    boolean existsByIdAndUser_Id(Long id, Long userId);

    /** ACTIVE인 방만 ENDED로 바꾼다. 바뀐 행이 1이면 내가 종료를 선점한 것이다. 종료가 동시에 겹쳐도 한 번만 성공한다. */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChatRoom r SET r.status = :ended, r.endedAt = :endedAt WHERE r.id = :id AND r.status = :active")
    int markEnded(@Param("id") Long id,
                  @Param("endedAt") LocalDateTime endedAt,
                  @Param("active") RoomStatus active,
                  @Param("ended") RoomStatus ended);
}
