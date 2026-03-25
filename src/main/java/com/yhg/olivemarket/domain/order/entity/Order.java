package com.yhg.olivemarket.domain.order.entity;

import com.yhg.olivemarket.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문 엔티티
 *
 * ERD:
 *   Order (id, member_id, total_price, status, created_at)
 *
 * 연관관계:
 * - Member와 @ManyToOne (주문 N : 회원 1)
 * - OrderItem과 @OneToMany (주문 1 : 주문상품 N)
 *
 * 설계 포인트:
 * - OrderItem은 Order를 통해서만 생성/관리 (Order가 연관관계 주인)
 * - cascade = PERSIST: Order 저장 시 OrderItem도 함께 저장
 * - orphanRemoval = true: Order에서 제거된 OrderItem은 DB에서도 삭제
 */
@Entity
@Table(name = "orders") // order는 SQL 예약어라 orders로 지정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    /**
     * 기본키 (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 주문한 회원 (FK)
     *
     * @ManyToOne(LAZY): 주문 조회 시 회원 정보를 즉시 로딩하지 않음
     * @JoinColumn(name = "member_id"): DB에 member_id 컬럼으로 저장
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * 주문 상품 목록
     *
     * @OneToMany: 주문 1 : 주문상품 N
     * mappedBy = "order": 연관관계 주인이 OrderItem.order 필드임을 명시
     *   → Order 테이블에 FK 컬럼 없음, OrderItem 테이블에 order_id FK 있음
     *
     * cascade = PERSIST:
     *   Order 저장(save) 시 OrderItem도 자동으로 함께 저장
     *   → orderRepository.save(order) 한 번으로 OrderItem까지 저장됨
     *
     * orphanRemoval = true:
     *   order.getOrderItems().remove(item) 시 해당 OrderItem DB에서도 삭제
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * 총 주문 금액
     * 주문 생성 시 OrderItem들의 (price * quantity) 합산값
     */
    @Column(nullable = false)
    private Integer totalPrice;

    /**
     * 주문 상태
     * @Enumerated(STRING): DB에 "PENDING" 문자열로 저장
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    /**
     * 주문 일시
     * @CreatedDate: 저장 시 자동으로 현재 시간 세팅
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Builder 패턴으로 Order 생성
     *
     * 사용 예시:
     *   Order order = Order.builder()
     *       .member(member)
     *       .totalPrice(75000)
     *       .status(OrderStatus.PENDING)
     *       .build();
     */
    @Builder
    public Order(Member member, Integer totalPrice, OrderStatus status) {
        this.member = member;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    /**
     * 주문 상품 추가
     *
     * Order와 OrderItem 양방향 연관관계 편의 메서드
     * order.addOrderItem(item) 호출 시:
     * 1. orderItems 리스트에 추가
     * 2. item.setOrder(order) 호출 (OrderItem 쪽 연관관계도 설정)
     *
     * 이 메서드 없이 직접 orderItems.add()만 하면
     * OrderItem의 order 필드가 설정되지 않아 DB에 order_id가 null로 저장됨
     *
     * @param orderItem 추가할 주문 상품
     */
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this); // OrderItem 쪽 연관관계 설정
    }
}
