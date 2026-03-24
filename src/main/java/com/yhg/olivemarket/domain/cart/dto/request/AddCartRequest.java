package com.yhg.olivemarket.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장바구니 담기 요청 DTO
 *
 * 요청 JSON 예시:
 * {
 *   "productId": 1,
 *   "quantity": 3
 * }
 *
 * Redis에 저장되는 구조:
 * Key   → "cart:{memberId}"       (예: "cart:1")
 * Field → "{productId}"           (예: "1")
 * Value → "{quantity}"            (예: "3")
 */
@Getter
@NoArgsConstructor
public class AddCartRequest {

    /**
     * 담을 상품 ID
     */
    @NotNull(message = "상품을 선택해 주세요.")
    private Long productId;

    /**
     * 담을 수량
     * - @Min(1): 1개 이상이어야 함
     */
    @NotNull(message = "수량을 입력해 주세요.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Integer quantity;
}
