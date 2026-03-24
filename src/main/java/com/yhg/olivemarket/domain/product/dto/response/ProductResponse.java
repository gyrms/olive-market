package com.yhg.olivemarket.domain.product.dto.response;

import com.yhg.olivemarket.domain.product.entity.Product;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 상품 응답 DTO
 *
 * Product 엔티티를 직접 반환하지 않고 DTO로 변환해서 반환
 * - 엔티티 내부 구조 노출 방지
 * - LAZY 로딩된 연관관계 직렬화 문제 방지
 *
 * 응답 JSON 예시:
 * {
 *   "id": 1,
 *   "name": "수분 크림",
 *   "price": 25000,
 *   "stock": 100,
 *   "description": "촉촉한 수분 크림입니다.",
 *   "categoryName": "스킨케어",
 *   "createdAt": "2024-03-25T10:00:00"
 * }
 */
@Getter
public class ProductResponse {

    /** 상품 ID */
    private final Long id;

    /** 상품명 */
    private final String name;

    /** 가격 */
    private final Integer price;

    /** 재고 */
    private final Integer stock;

    /** 상품 설명 */
    private final String description;

    /**
     * 카테고리명
     * category_id 대신 카테고리 이름을 직접 반환
     * → 클라이언트가 별도로 카테고리 조회할 필요 없음
     */
    private final String categoryName;

    /** 등록 일시 */
    private final LocalDateTime createdAt;

    /**
     * Product 엔티티 → ProductResponse DTO 변환 (정적 팩토리 메서드)
     *
     * fetch join으로 조회된 Product를 넘겨야 category.getName() 호출 시
     * 추가 쿼리가 발생하지 않음
     *
     * @param product Product 엔티티 (카테고리 포함)
     * @return ProductResponse
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(product);
    }

    private ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.description = product.getDescription();
        // category는 LAZY 로딩이므로 fetch join으로 가져온 경우에만 안전하게 접근 가능
        this.categoryName = product.getCategory().getName();
        this.createdAt = product.getCreatedAt();
    }
}
