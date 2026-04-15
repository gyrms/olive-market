package com.yhg.olivemarket.domain.product.service;

import com.yhg.olivemarket.domain.product.dto.request.CreateCategoryRequest;
import com.yhg.olivemarket.domain.product.dto.response.CategoryResponse;
import com.yhg.olivemarket.domain.product.entity.Category;
import com.yhg.olivemarket.domain.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }
}
