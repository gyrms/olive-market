package com.yhg.olivemarket.domain.product.repository;

import com.yhg.olivemarket.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 상품 Repository
 *
 * JpaRepository<Product, Long>:
 * - Product: 관리할 엔티티 타입
 * - Long: 기본키(PK) 타입
 *
 * 동적 검색(카테고리/가격범위/키워드)은 QueryDSL을 사용하는
 * ProductQueryRepository에서 처리한다. (2주차에 작성 예정)
 * 이 Repository는 단순 CRUD + 기본 조회 메서드만 담당한다.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 상품 단건 조회 (카테고리 fetch join)
     *
     * 일반 findById()로 조회 시 카테고리는 LAZY 로딩 → 추가 쿼리 발생 (N+1)
     * fetch join으로 상품 + 카테고리를 한 번의 쿼리로 조회
     *
     * JPQL 문법:
     * - p: Product 별칭
     * - JOIN FETCH p.category: category를 즉시 로딩 (LEFT JOIN + 데이터 함께 조회)
     * - :id → @Param("id")로 바인딩
     *
     * @param id 상품 ID
     * @return Optional<Product> (카테고리 포함)
     */
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    /**
     * 카테고리별 상품 목록 조회
     *
     * Spring Data JPA 메서드 네이밍 규칙:
     * findBy + Category + Id → WHERE category_id = ? 쿼리 자동 생성
     *
     * @param categoryId 카테고리 ID
     * @return 해당 카테고리의 상품 목록
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * 재고 있는 상품만 조회
     *
     * GreaterThan: stock > 0 조건
     * → 품절 상품을 목록에서 제외할 때 사용
     *
     * @return 재고 1개 이상인 상품 목록
     */
    List<Product> findByStockGreaterThan(int stock);
}
