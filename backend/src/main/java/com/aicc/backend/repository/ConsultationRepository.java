package com.aicc.backend.repository;

import com.aicc.backend.domain.Consultation;
import com.aicc.backend.domain.ConsultationStatus;
import com.aicc.backend.repository.projection.CategoryCount;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    @Query("""
            SELECT c FROM Consultation c
            LEFT JOIN FETCH c.category
            WHERE (:cursor IS NULL OR c.id < :cursor)
              AND (:keyword IS NULL OR c.title LIKE CONCAT('%', :keyword, '%') OR c.summary LIKE CONCAT('%', :keyword, '%'))
              AND (:categoryId IS NULL OR c.category.id = :categoryId)
              AND (:status IS NULL OR c.status = :status)
            ORDER BY c.id DESC
            """)
    List<Consultation> searchByCursor(@Param("cursor") Long cursor,
                                      @Param("keyword") String keyword,
                                      @Param("categoryId") Long categoryId,
                                      @Param("status") ConsultationStatus status,
                                      Limit limit);

    @Query("SELECT cat.name AS name, COUNT(c) AS count FROM Consultation c LEFT JOIN c.category cat GROUP BY cat.name")
    List<CategoryCount> countByCategory();

    long countByStatus(ConsultationStatus status);
}
