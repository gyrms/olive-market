package com.yhg.olivemarket.domain.product.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yhg.olivemarket.domain.product.dto.request.ProductSearchRequest;
import com.yhg.olivemarket.domain.product.entity.Product;
import com.yhg.olivemarket.domain.product.entity.QCategory;
import com.yhg.olivemarket.domain.product.entity.QProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 상품 QueryDSL 동적 검색 Repository
 *
 * QueryDSL을 사용해 조건에 따라 동적으로 쿼리를 생성한다.
 * JpaRepository로 표현하기 어려운 복잡한 검색 조건을 타입 안전하게 작성할 수 있다.
 *
 * QProduct, QCategory:
 * - build.gradle의 annotationProcessor가 Product, Category 엔티티를 스캔해서 자동 생성
 * - Q클래스는 src/main/generated 폴더에 생성됨
 * - IntelliJ에서 Gradle → Tasks → build → compileJava 실행 후 사용 가능
 *
 * BooleanExpression:
 * - QueryDSL의 조건 표현식 (WHERE 절에 해당)
 * - null을 반환하면 해당 조건이 자동으로 무시됨 → 동적 쿼리의 핵심
 */
@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    // QueryDSL이 자동 생성한 Q클래스 (엔티티의 필드를 타입 안전하게 참조)
    private final QProduct product = QProduct.product;
    private final QCategory category = QCategory.category;

    /**
     * 상품 동적 검색
     *
     * 각 조건(keyword, categoryId, minPrice, maxPrice)이 null이면 해당 조건을 무시한다.
     * fetch join으로 카테고리를 함께 조회해 N+1 문제를 방지한다.
     *
     * 생성되는 SQL 예시 (keyword=크림, minPrice=10000):
     * SELECT p.*, c.*
     * FROM product p
     * JOIN category c ON p.category_id = c.id
     * WHERE p.name LIKE '%크림%'
     * AND p.price >= 10000
     * ORDER BY p.created_at DESC
     *
     * @param request 검색 조건 DTO (null 필드는 조건 미적용)
     * @return 조건에 맞는 상품 목록 (카테고리 포함)
     */
    public List<Product> search(ProductSearchRequest request) {
        return queryFactory
                .selectFrom(product)
                // fetch join: 카테고리를 한 번의 쿼리로 함께 조회 (N+1 방지)
                .join(product.category, category).fetchJoin()
                .where(
                        // 각 조건 메서드가 null을 반환하면 QueryDSL이 자동으로 AND 조건에서 제외
                        containsKeyword(request.getKeyword()),
                        eqCategory(request.getCategoryId()),
                        goeMinPrice(request.getMinPrice()),
                        loeMaxPrice(request.getMaxPrice())
                )
                // 최신 등록 순 정렬
                .orderBy(product.createdAt.desc())
                .fetch(); // List<Product> 반환
    }

    /**
     * 키워드 검색 조건 (상품명 LIKE)
     *
     * StringUtils.hasText(): null, 빈 문자열, 공백 문자열이면 false
     * → keyword가 없으면 null 반환 → 조건 미적용
     *
     * containsIgnoreCase: 대소문자 구분 없이 LIKE '%keyword%' 쿼리 생성
     *
     * @param keyword 검색 키워드
     * @return BooleanExpression or null
     */
    private BooleanExpression containsKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null; // 조건 미적용
        }
        return product.name.containsIgnoreCase(keyword);
        // → WHERE name LIKE '%keyword%'
    }

    /**
     * 카테고리 필터 조건
     *
     * categoryId가 null이면 null 반환 → 조건 미적용
     *
     * @param categoryId 카테고리 ID
     * @return BooleanExpression or null
     */
    private BooleanExpression eqCategory(Long categoryId) {
        if (categoryId == null) {
            return null; // 조건 미적용
        }
        return product.category.id.eq(categoryId);
        // → WHERE category_id = categoryId
    }

    /**
     * 최소 가격 조건 (price >= minPrice)
     *
     * goe: Greater Or Equal (>=)
     *
     * @param minPrice 최소 가격
     * @return BooleanExpression or null
     */
    private BooleanExpression goeMinPrice(Integer minPrice) {
        if (minPrice == null) {
            return null; // 조건 미적용
        }
        return product.price.goe(minPrice);
        // → WHERE price >= minPrice
    }

    /**
     * 최대 가격 조건 (price <= maxPrice)
     *
     * loe: Less Or Equal (<=)
     *
     * @param maxPrice 최대 가격
     * @return BooleanExpression or null
     */
    private BooleanExpression loeMaxPrice(Integer maxPrice) {
        if (maxPrice == null) {
            return null; // 조건 미적용
        }
        return product.price.loe(maxPrice);
        // → WHERE price <= maxPrice
    }
}
