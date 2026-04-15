package com.yhg.olivemarket.domain.product.controller;

import com.yhg.olivemarket.domain.product.dto.request.CreateCategoryRequest;
import com.yhg.olivemarket.domain.product.dto.response.CategoryResponse;
import com.yhg.olivemarket.domain.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(201).body(categoryService.create(request));
    }
}
