package com.yhg.olivemarket.domain.product.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 동적 검색 조건 DTO
 *
 * QueryDSL의 BooleanExpression을 활용한 동적 검색에 사용된다.
 * 각 필드는 null 허용 → null인 필드는 검색 조건에서 제외됨
 *
 * 요청 쿼리 파라미터 예시:
 * GET /api/products?keyword=크림&categoryId=1&minPrice=10000&maxPrice=50000
 *
 * 조건 조합 예시:
 * - keyword만 있으면 → 이름에 "크림" 포함된 상품
 * - categoryId만 있으면 → 해당 카테고리 상품
 * - minPrice + maxPrice → 가격 범위 내 상품
 * - 전부 있으면 → 모든 조건을 AND로 조합
 * - 전부 없으면 → 전체 상품 조회
 */
@Getter
@NoArgsConstructor
public class ProductSearchRequest {

    /**
     * 상품명 키워드 검색
     * null이면 키워드 조건 미적용
     * 예: "크림" → name LIKE '%크림%'
     */
    private String keyword;

    /**
     * 카테고리 ID 필터
     * null이면 카테고리 조건 미적용
     * 예: 1 → category_id = 1
     */
    private Long categoryId;

    /**
     * 최소 가격 필터
     * null이면 최소 가격 조건 미적용
     * 예: 10000 → price >= 10000
     */
    private Integer minPrice;

    /**
     * 최대 가격 필터
     * null이면 최대 가격 조건 미적용
     * 예: 50000 → price <= 50000
     */
    private Integer maxPrice;
}
