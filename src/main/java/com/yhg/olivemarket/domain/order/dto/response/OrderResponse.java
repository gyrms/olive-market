package com.yhg.olivemarket.domain.order.dto.response;

import com.yhg.olivemarket.domain.order.entity.Order;
import com.yhg.olivemarket.domain.order.entity.OrderItem;
import com.yhg.olivemarket.domain.order.entity.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 응답 DTO
 *
 * 응답 JSON 예시:
 * {
 *   "orderId": 1,
 *   "totalPrice": 75000,
 *   "status": "PENDING",
 *   "createdAt": "2024-03-29T10:00:00",
 *   "orderItems": [
 *     {
 *       "productId": 1,
 *       "productName": "수분 크림",
 *       "quantity": 2,
 *       "price": 25000,
 *       "subTotal": 50000
 *     },
 *     {
 *       "productId": 3,
 *       "productName": "선크림",
 *       "quantity": 1,
 *       "price": 25000,
 *       "subTotal": 25000
 *     }
 *   ]
 * }
 */
@Getter
public class OrderResponse {

    /** 주문 ID */
    private final Long orderId;

    /** 총 주문 금액 */
    private final Integer totalPrice;

    /** 주문 상태 (PENDING, CONFIRMED 등) */
    private final OrderStatus status;

    /** 주문 일시 */
    private final LocalDateTime createdAt;

    /** 주문 상품 목록 */
    private final List<OrderItemResponse> orderItems;

    /**
     * Order 엔티티 → OrderResponse DTO 변환 (정적 팩토리 메서드)
     *
     * @param order Order 엔티티 (orderItems + product fetch join 필요)
     * @return OrderResponse
     */
    public static OrderResponse from(Order order) {
        return new OrderResponse(order);
    }

    private OrderResponse(Order order) {
        this.orderId = order.getId();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        // OrderItem 리스트 → OrderItemResponse 리스트 변환
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 주문 상품 응답 내부 클래스
     *
     * OrderResponse 안에서만 사용하므로 내부 클래스로 정의
     */
    @Getter
    public static class OrderItemResponse {

        /** 상품 ID */
        private final Long productId;

        /** 상품명 */
        private final String productName;

        /** 주문 수량 */
        private final Integer quantity;

        /** 주문 당시 상품 가격 (스냅샷) */
        private final Integer price;

        /**
         * 소계 (price * quantity)
         * OrderItem.getSubTotal() 메서드 활용
         */
        private final Integer subTotal;

        public static OrderItemResponse from(OrderItem orderItem) {
            return new OrderItemResponse(orderItem);
        }

        private OrderItemResponse(OrderItem orderItem) {
            this.productId = orderItem.getProduct().getId();
            this.productName = orderItem.getProduct().getName();
            this.quantity = orderItem.getQuantity();
            this.price = orderItem.getPrice();
            this.subTotal = orderItem.getSubTotal(); // price * quantity
        }
    }
}
