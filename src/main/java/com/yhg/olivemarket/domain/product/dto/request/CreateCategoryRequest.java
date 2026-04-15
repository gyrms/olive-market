package com.yhg.olivemarket.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "카테고리명을 입력해 주세요.")
    private String name;
}
