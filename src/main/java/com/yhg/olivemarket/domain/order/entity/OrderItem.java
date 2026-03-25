package com.yhg.olivemarket.domain.order.entity;

import com.yhg.olivemarket.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 상품 엔티티
 *
 * ERD:
 *   OrderItem (id, order_id, product_id, quantity, price)
 *
 * 연관관계:
 * - Order와 @ManyToOne (주문상품 N : 주문 1)
 * - Product와 @ManyToOne (주문상품 N : 상품 1)
 *
 * 설계 포인트:
 * - price 필드: 주문 당시 상품 가격을 스냅샷으로 저장
 *   → 나중에 상품 가격이 변경되어도 주문 당시 가격 보존
 *   → product.getPrice()를 직접 참조하면 가격 변경 시 과거 주문 금액도 바뀌는 문제 발생
 */
@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    /**
     * 기본키 (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 주문 (FK)
     *
     * Order.orderItems와 양방향 관계
     * 연관관계 주인: OrderItem (order_id FK를 OrderItem 테이블이 가짐)
     *
     * @Setter 대신 setOrder() 메서드로 접근 제어
     * (Order.addOrderItem()에서 호출)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * 주문한 상품 (FK)
     *
     * LAZY 로딩: 주문 상품 조회 시 상품 정보를 즉시 로딩하지 않음
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * 주문 수량
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * 주문 당시 상품 가격 (스냅샷)
     *
     * 주문 생성 시 product.getPrice()를 복사해서 저장
     * 이후 상품 가격이 변경되어도 이 값은 변하지 않음
     * → 주문 내역의 정합성 보장
     */
    @Column(nullable = false)
    private Integer price;

    /**
     * Builder 패턴으로 OrderItem 생성
     *
     * 사용 예시:
     *   OrderItem item = OrderItem.builder()
     *       .product(product)
     *       .quantity(3)
     *       .price(product.getPrice()) // 현재 가격 스냅샷
     *       .build();
     */
    @Builder
    public OrderItem(Product product, Integer quantity, Integer price) {
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    /**
     * 주문 설정 메서드 (양방향 연관관계용)
     *
     * Order.addOrderItem()에서만 호출
     * 외부에서 직접 호출하지 않도록 패키지 접근 제어
     *
     * @param order 소속 주문
     */
    void setOrder(Order order) {
        this.order = order;
    }

    /**
     * 주문 상품 소계 계산
     *
     * price(주문 당시 가격) * quantity(수량)
     *
     * @return 소계 금액
     */
    public int getSubTotal() {
        return this.price * this.quantity;
    }
}
