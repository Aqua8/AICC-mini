package com.aicc.backend.controller;

import com.aicc.backend.dto.admin.*;
import com.aicc.backend.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> listCategories() {
        return ResponseEntity.ok(adminService.listCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(adminService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
                                                           @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(adminService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/faq")
    public ResponseEntity<List<FaqResponse>> listFaqs() {
        return ResponseEntity.ok(adminService.listFaqs());
    }

    @PostMapping("/faq")
    public ResponseEntity<FaqResponse> createFaq(@Valid @RequestBody FaqRequest request) {
        return ResponseEntity.ok(adminService.createFaq(request));
    }

    @PutMapping("/faq/{id}")
    public ResponseEntity<FaqResponse> updateFaq(@PathVariable Long id, @Valid @RequestBody FaqRequest request) {
        return ResponseEntity.ok(adminService.updateFaq(id, request));
    }

    @DeleteMapping("/faq/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        adminService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }
}
