package com.yhg.olivemarket.domain.product.dto.response;

import com.yhg.olivemarket.domain.product.entity.Category;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CategoryResponse {

    private final Long id;
    private final String name;

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
