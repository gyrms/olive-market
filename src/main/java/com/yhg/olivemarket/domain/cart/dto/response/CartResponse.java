package com.yhg.olivemarket.domain.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 장바구니 아이템 응답 DTO
 *
 * Redis Hash에서 꺼낸 데이터를 클라이언트에 반환할 형태로 변환
 *
 * 응답 JSON 예시 (장바구니 전체 조회):
 * [
 *   { "productId": 1, "quantity": 3 },
 *   { "productId": 5, "quantity": 1 }
 * ]
 */
@Getter
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 자동 생성
public class CartResponse {

    /**
     * 상품 ID
     * Redis Hash의 Field에 해당
     */
    private final Long productId;

    /**
     * 수량
     * Redis Hash의 Value에 해당
     * Redis는 문자열로 저장되므로 서비스에서 Integer로 변환
     */
    private final Integer quantity;
}
