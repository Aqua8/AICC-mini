package com.aicc.backend.repository;

import com.aicc.backend.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    @Query("SELECT f FROM Faq f LEFT JOIN FETCH f.category ORDER BY f.id DESC")
    List<Faq> findAllWithCategory();
}
