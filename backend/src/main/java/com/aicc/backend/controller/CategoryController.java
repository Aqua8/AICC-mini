package com.aicc.backend.controller;

import com.aicc.backend.dto.admin.CategoryResponse;
import com.aicc.backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 로그인한 누구나 카테고리 목록을 읽을 수 있다. (등록·수정·삭제는 /api/admin/categories, ADMIN 전용) */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final AdminService adminService;

    public CategoryController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(adminService.listCategories());
    }
}
