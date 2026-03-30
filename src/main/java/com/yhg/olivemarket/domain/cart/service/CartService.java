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
 * ───────────────────────────────────────────────
 * Redis 자료구조: Hash
 * ───────────────────────────────────────────────
 * Key   → "cart:{email}"       (회원별 장바구니, 예: "cart:user@test.com")
 * Field → "{productId}"        (담은 상품 ID,    예: "3")
 * Value → "{quantity}"         (담은 수량,       예: "2")
 *
 * Redis CLI 확인 예시:
 *   HGETALL cart:user@test.com
 *   1) "3"    ← productId
 *   2) "2"    ← quantity
 *   3) "7"    ← productId
 *   4) "1"    ← quantity
 *
 * ───────────────────────────────────────────────
 * 왜 이메일을 Key로 사용하는가?
 * ───────────────────────────────────────────────
 * 이전 구현은 email.hashCode()로 Long 변환 후 Key로 사용했으나,
 * Java의 hashCode()는 음수도 반환할 수 있고, 서로 다른 이메일이 같은 해시값을
 * 가질 수 있는 충돌(collision) 위험이 있다.
 * 이메일은 이미 유일한 값(UNIQUE 제약 조건)이므로 직접 Key로 사용하는 것이 안전하다.
 *
 * ───────────────────────────────────────────────
 * TTL (Time To Live)
 * ───────────────────────────────────────────────
 * - 장바구니는 마지막 변경으로부터 30일 후 자동 만료
 * - 담기/수량 변경 시마다 TTL 30일로 재갱신
 * - 만료된 장바구니는 Redis가 자동 삭제 → 별도 삭제 로직 불필요
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, String> redisTemplate;

    /** 장바구니 TTL: 30일 (초 단위 아닌 TimeUnit.DAYS 사용) */
    private static final long CART_EXPIRE_DAYS = 30;

    /**
     * Redis Key 생성
     *
     * 이메일로 회원별 고유 Key를 생성한다.
     * 네임스페이스("cart:") 를 붙여 다른 도메인의 Redis Key와 충돌을 방지한다.
     *
     * @param email 회원 이메일 (JWT에서 추출, 유일값 보장)
     * @return Redis Key (예: "cart:user@test.com")
     */
    private String getCartKey(String email) {
        return "cart:" + email;
    }

    /**
     * 장바구니 담기
     *
     * 이미 같은 상품이 담겨있으면 수량을 덮어씀 (누적 아님).
     * 새 상품을 담거나 수량을 변경할 때마다 TTL을 30일로 재갱신한다.
     *
     * Redis 명령어:
     *   HSET  cart:user@test.com {productId} {quantity}
     *   EXPIRE cart:user@test.com 2592000  (30일 = 30 * 24 * 60 * 60 초)
     *
     * @param email   JWT에서 추출한 회원 이메일 (회원 식별용)
     * @param request 담을 상품 ID + 수량 DTO
     */
    public void addCart(String email, AddCartRequest request) {
        String cartKey = getCartKey(email);

        // HSET: Hash의 특정 Field(productId)에 Value(quantity)를 저장
        // 같은 productId가 이미 있으면 덮어씀 (upsert)
        redisTemplate.opsForHash().put(
                cartKey,                                    // Key:   "cart:user@test.com"
                String.valueOf(request.getProductId()),     // Field: "3"  (productId)
                String.valueOf(request.getQuantity())       // Value: "2"  (quantity)
        );

        // EXPIRE: 장바구니에 변경이 생길 때마다 TTL을 30일로 갱신
        // 마지막 활동으로부터 30일간 유지 (슬라이딩 만료)
        redisTemplate.expire(cartKey, CART_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 장바구니 전체 조회
     *
     * Redis에서 해당 회원의 모든 Hash Field-Value를 한 번에 조회한다.
     *
     * Redis 명령어:
     *   HGETALL cart:user@test.com
     *   → { "3": "2", "7": "1" }  (productId: quantity)
     *
     * @param email 회원 이메일
     * @return 장바구니 아이템 목록 DTO (productId + quantity)
     */
    public List<CartResponse> getCart(String email) {
        String cartKey = getCartKey(email);

        // HGETALL: Hash의 모든 Field-Value 쌍을 Map으로 반환
        // 장바구니가 비어있거나 Key가 없으면 빈 Map 반환
        Map<Object, Object> cartData = redisTemplate.opsForHash().entries(cartKey);

        // Map<Object, Object> → List<CartResponse> 변환
        // Redis는 모든 값을 String으로 저장하므로 Long/Integer로 파싱 필요
        return cartData.entrySet().stream()
                .map(entry -> new CartResponse(
                        Long.parseLong((String) entry.getKey()),     // Field → productId (Long)
                        Integer.parseInt((String) entry.getValue())  // Value → quantity (Integer)
                ))
                .collect(Collectors.toList());
    }

    /**
     * 장바구니 특정 상품 삭제
     *
     * Hash에서 특정 Field(productId)만 삭제한다.
     * 다른 상품은 그대로 유지되며, TTL도 변경되지 않는다.
     *
     * Redis 명령어:
     *   HDEL cart:user@test.com {productId}
     *
     * @param email     회원 이메일
     * @param productId 삭제할 상품 ID
     */
    public void removeCartItem(String email, Long productId) {
        String cartKey = getCartKey(email);

        // HDEL: Hash에서 특정 Field(productId)만 삭제
        // productId를 String으로 변환해야 Redis Field와 타입이 일치
        redisTemplate.opsForHash().delete(cartKey, String.valueOf(productId));
    }
}
