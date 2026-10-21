package com.aicc.backend.config;

import com.aicc.backend.domain.Category;
import com.aicc.backend.domain.DefaultCategories;
import com.aicc.backend.repository.CategoryRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** 카테고리가 하나도 없으면 AI·LLM 상담 기본 카테고리를 넣는다. 관리자가 만든 카테고리가 있으면 건드리지 않는다. */
@Component
public class CategoryInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    public CategoryInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 0) {
            return;
        }
        DefaultCategories.ITEMS.forEach(item -> categoryRepository.save(
                Category.builder().name(item.name()).description(item.description()).build()));
    }
}
