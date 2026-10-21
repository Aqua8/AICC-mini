package com.aicc.backend.repository;

import com.aicc.backend.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    Optional<Category> findByName(String name);
}
