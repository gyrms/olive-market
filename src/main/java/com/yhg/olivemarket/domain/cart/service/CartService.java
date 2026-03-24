package com.yhg.olivemarket.domain.cart.service;

import com.yhg.olivemarket.domain.cart.dto.request.AddCartRequest;
import com.yhg.olivemarket.domain.cart.dto.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 장바구니 서비스 (Redis Hash 기반)
 *
 * Redis 자료구조: Hash
 * - Key   → "cart:{memberId}"  (회원별 장바구니)
 * - Field → "{productId}"      (상품 ID)
 * - Value → "{quantity}"       (수량)
 *
 * Redis Hash 구조 예시:
 * "cart:1" → { "3": "2", "7": "1", "12": "5" }
 *   ↑ memberId=1의 장바구니: 상품3 2개, 상품7 1개, 상품12 5개
 *
 * TTL (Time To Live):
 * - 장바구니는 30일 후 자동 만료
 * - 담기/수량 변경 시마다 TTL 갱신
 * - 세션이 만료된 장바구니는 Redis가 자동 삭제 → 별도 삭제 로직 불필요
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, String> redisTemplate;

    /** 장바구니 TTL: 30일 */
    private static final long CART_EXPIRE_DAYS = 30;

    /**
     * 장바구니 Redis Key 생성
     *
     * @param memberId 회원 ID
     * @return "cart:{memberId}" (예: "cart:1")
     */
    private String getCartKey(Long memberId) {
        return "cart:" + memberId;
    }

    /**
     * 장바구니 담기
     *
     * 이미 같은 상품이 담겨있으면 수량을 덮어씀 (누적 아님)
     * 담은 후 TTL 30일로 갱신
     *
     * Redis 명령어:
     * HSET cart:1 {productId} {quantity}
     * EXPIRE cart:1 2592000 (30일 = 30 * 24 * 60 * 60초)
     *
     * @param memberId 회원 ID (JWT에서 추출)
     * @param request  담을 상품 ID + 수량
     */
    public void addCart(Long memberId, AddCartRequest request) {
        String cartKey = getCartKey(memberId);

        // HSET: Hash의 특정 Field에 Value 저장
        // opsForHash(): Redis Hash 자료구조 조작
        redisTemplate.opsForHash().put(
                cartKey,                                    // Key: "cart:1"
                String.valueOf(request.getProductId()),     // Field: "3"
                String.valueOf(request.getQuantity())       // Value: "2"
        );

        // TTL 갱신: 장바구니에 변경이 생길 때마다 30일 연장
        redisTemplate.expire(cartKey, CART_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 장바구니 전체 조회
     *
     * Redis 명령어:
     * HGETALL cart:1 → { "3": "2", "7": "1" }
     *
     * @param memberId 회원 ID
     * @return 장바구니 아이템 목록 DTO
     */
    public List<CartResponse> getCart(Long memberId) {
        String cartKey = getCartKey(memberId);

        // HGETALL: Hash의 모든 Field-Value 쌍 조회
        // entries(): Map<Object, Object> 반환
        Map<Object, Object> cartData = redisTemplate.opsForHash().entries(cartKey);

        // Map<Object, Object> → List<CartResponse> 변환
        return cartData.entrySet().stream()
                .map(entry -> new CartResponse(
                        Long.parseLong((String) entry.getKey()),    // Field → productId (Long)
                        Integer.parseInt((String) entry.getValue()) // Value → quantity (Integer)
                ))
                .collect(Collectors.toList());
    }

    /**
     * 장바구니 특정 상품 삭제
     *
     * Redis 명령어:
     * HDEL cart:1 {productId}
     *
     * @param memberId  회원 ID
     * @param productId 삭제할 상품 ID
     */
    public void removeCartItem(Long memberId, Long productId) {
        String cartKey = getCartKey(memberId);

        // HDEL: Hash에서 특정 Field 삭제
        redisTemplate.opsForHash().delete(cartKey, String.valueOf(productId));
    }
}
