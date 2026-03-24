package com.yhg.olivemarket.domain.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 등록 요청 DTO
 *
 * ADMIN 권한을 가진 사용자만 사용 가능 (SecurityConfig에서 설정)
 *
 * 요청 JSON 예시:
 * {
 *   "name": "수분 크림",
 *   "price": 25000,
 *   "stock": 100,
 *   "description": "촉촉한 수분 크림입니다.",
 *   "categoryId": 1
 * }
 */
@Getter
@NoArgsConstructor
public class CreateProductRequest {

    /**
     * 상품명
     */
    @NotBlank(message = "상품명을 입력해 주세요.")
    private String name;

    /**
     * 상품 가격
     * - @NotNull: null 방지
     * - @Min(0): 음수 가격 방지
     */
    @NotNull(message = "가격을 입력해 주세요.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    /**
     * 재고 수량
     * - @Min(0): 음수 재고 방지
     */
    @NotNull(message = "재고를 입력해 주세요.")
    @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
    private Integer stock;

    /**
     * 상품 설명 (선택값)
     */
    private String description;

    /**
     * 카테고리 ID (FK)
     * - 해당 ID의 Category가 DB에 존재해야 함
     * - 없으면 ProductService에서 CustomException 발생
     */
    @NotNull(message = "카테고리를 선택해 주세요.")
    private Long categoryId;
}
