package com.yhg.olivemarket.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 상품 엔티티
 *
 * ERD:
 *   Product (id, name, price, stock, description, category_id, created_at)
 *
 * Category와의 관계:
 * - @ManyToOne: 여러 상품이 하나의 카테고리에 속함
 * - @JoinColumn: DB에 category_id 컬럼으로 저장 (FK)
 * - FetchType.LAZY: 상품 조회 시 카테고리를 즉시 로딩하지 않음 (N+1 방지)
 *
 * 재고(stock) 관리:
 * - 주문 생성 시 OrderService에서 reduceStock() 호출해 재고 차감
 * - 재고가 0 미만이 되면 CustomException(OUT_OF_STOCK) 발생
 */
@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자, 외부 직접 생성 금지
@EntityListeners(AuditingEntityListener.class)      // @CreatedDate 자동 처리
public class Product {

    /**
     * 기본키 (PK)
     * IDENTITY 전략: MySQL의 AUTO_INCREMENT 사용
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 상품명
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 상품 가격
     * - int 대신 Integer 사용: null 허용 (추후 확장 고려)
     */
    @Column(nullable = false)
    private Integer price;

    /**
     * 재고 수량
     * - 주문 생성 시 감소
     * - 0 미만으로 내려가면 OUT_OF_STOCK 예외 발생
     */
    @Column(nullable = false)
    private Integer stock;

    /**
     * 상품 설명
     * - @Column(columnDefinition = "TEXT"): 긴 텍스트 저장 가능 (VARCHAR 대신 TEXT 타입)
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 카테고리 (FK)
     *
     * @ManyToOne: 다대일 관계 (상품 N : 카테고리 1)
     * @JoinColumn(name = "category_id"): DB에 category_id 컬럼으로 저장
     * FetchType.LAZY: 지연 로딩
     *   - 상품 조회 시 카테고리 쿼리를 즉시 실행하지 않음
     *   - 실제로 category.getName() 등 호출 시점에 쿼리 실행
     *   - N+1 문제 방지를 위해 필요 시 fetch join 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * 등록 일시
     * @CreatedDate: 엔티티 최초 저장 시 자동으로 현재 시간 세팅
     * updatable=false: 이후 수정 불가
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Builder 패턴으로 Product 생성
     *
     * 사용 예시:
     *   Product product = Product.builder()
     *       .name("수분 크림")
     *       .price(25000)
     *       .stock(100)
     *       .description("촉촉한 수분 크림입니다.")
     *       .category(category)
     *       .build();
     */
    @Builder
    public Product(String name, Integer price, Integer stock, String description, Category category) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.category = category;
    }

    /**
     * 재고 차감 메서드
     *
     * 주문 생성 시 OrderService에서 호출
     * 도메인 로직을 엔티티 안에 위치시켜 응집도를 높임 (도메인 모델 패턴)
     *
     * @param quantity 차감할 수량
     * @throws IllegalArgumentException 재고 부족 시
     *
     * 사용 예시:
     *   product.reduceStock(3); // 재고 3개 차감
     */
    public void reduceStock(int quantity) {
        if (this.stock < quantity) {
            // 재고 부족 시 예외 발생 → OrderService에서 CustomException으로 변환
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stock -= quantity;
    }
}
