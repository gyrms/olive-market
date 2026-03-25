package com.yhg.olivemarket.domain.order.entity;

/**
 * 주문 상태 열거형
 *
 * 주문 상태 흐름:
 * PENDING → CONFIRMED → SHIPPED → DELIVERED
 *                ↓
 *           CANCELLED (주문 취소)
 *
 * DB에 문자열로 저장됨 (@Enumerated(EnumType.STRING))
 * → "PENDING", "CONFIRMED" 등으로 저장
 */
public enum OrderStatus {

    /**
     * 주문 대기
     * 주문 생성 직후 초기 상태
     */
    PENDING,

    /**
     * 주문 확인
     * 판매자가 주문을 확인한 상태
     */
    CONFIRMED,

    /**
     * 배송 중
     */
    SHIPPED,

    /**
     * 배송 완료
     */
    DELIVERED,

    /**
     * 주문 취소
     * 재고 부족, 사용자 취소 등
     */
    CANCELLED
}
