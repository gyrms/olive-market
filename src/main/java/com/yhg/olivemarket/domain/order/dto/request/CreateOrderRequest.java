package com.yhg.olivemarket.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 주문 생성 요청 DTO
 *
 * 한 번에 여러 상품을 주문할 수 있도록 List 구조로 설계
 *
 * 요청 JSON 예시:
 * {
 *   "orderItems": [
 *     { "productId": 1, "quantity": 2 },
 *     { "productId": 3, "quantity": 1 }
 *   ]
 * }
 */
@Getter
@NoArgsConstructor
public class CreateOrderRequest {

    /**
     * 주문 상품 목록
     *
     * @NotEmpty: 최소 1개 이상의 상품이 있어야 함
     * @Valid: 리스트 내부 OrderItemRequest도 검증
     */
    @NotEmpty(message = "주문 상품을 1개 이상 선택해 주세요.")
    @Valid
    private List<OrderItemRequest> orderItems;

    /**
     * 주문 상품 요청 내부 클래스
     *
     * CreateOrderRequest 안에서만 사용하므로 내부 클래스로 정의
     */
    @Getter
    @NoArgsConstructor
    public static class OrderItemRequest {

        /**
         * 주문할 상품 ID
         */
        @NotNull(message = "상품을 선택해 주세요.")
        private Long productId;

        /**
         * 주문 수량
         * @Min(1): 최소 1개 이상
         */
        @NotNull(message = "수량을 입력해 주세요.")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        private Integer quantity;
    }
}
