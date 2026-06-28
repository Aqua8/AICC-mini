package com.aicc.backend.service;

import com.aicc.backend.domain.Category;
import com.aicc.backend.domain.Faq;
import com.aicc.backend.dto.admin.*;
import com.aicc.backend.exception.ApiException;
import com.aicc.backend.repository.CategoryRepository;
import com.aicc.backend.repository.FaqRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final CategoryRepository categoryRepository;
    private final FaqRepository faqRepository;

    public AdminService(CategoryRepository categoryRepository, FaqRepository faqRepository) {
        this.categoryRepository = categoryRepository;
        this.faqRepository = faqRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw ApiException.badRequest("이미 존재하는 카테고리입니다.");
        }
        Category category = categoryRepository.save(Category.builder()
                .name(request.name())
                .description(request.description())
                .build());
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("카테고리를 찾을 수 없습니다."));
        category.update(request.name(), request.description());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw ApiException.notFound("카테고리를 찾을 수 없습니다.");
        }
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<FaqResponse> listFaqs() {
        return faqRepository.findAllWithCategory().stream().map(FaqResponse::from).toList();
    }

    @Transactional
    public FaqResponse createFaq(FaqRequest request) {
        Faq faq = faqRepository.save(Faq.builder()
                .category(resolveCategory(request.categoryId()))
                .question(request.question())
                .answer(request.answer())
                .build());
        return FaqResponse.from(faq);
    }

    @Transactional
    public FaqResponse updateFaq(Long id, FaqRequest request) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("FAQ를 찾을 수 없습니다."));
        faq.update(resolveCategory(request.categoryId()), request.question(), request.answer());
        return FaqResponse.from(faq);
    }

    @Transactional
    public void deleteFaq(Long id) {
        if (!faqRepository.existsById(id)) {
            throw ApiException.notFound("FAQ를 찾을 수 없습니다.");
        }
        faqRepository.deleteById(id);
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> ApiException.notFound("카테고리를 찾을 수 없습니다."));
    }
}
