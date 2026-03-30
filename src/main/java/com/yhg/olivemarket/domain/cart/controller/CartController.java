package com.yhg.olivemarket.domain.cart.controller;

import com.yhg.olivemarket.domain.cart.dto.request.AddCartRequest;
import com.yhg.olivemarket.domain.cart.dto.response.CartResponse;
import com.yhg.olivemarket.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 장바구니 컨트롤러
 *
 * ───────────────────────────────────────────────
 * 인증 요구사항
 * ───────────────────────────────────────────────
 * 모든 API는 JWT 인증 필요.
 * SecurityConfig에서 /api/cart/** → authenticated() 설정.
 * 토큰 없이 접근 시 401 Unauthorized 반환.
 *
 * ───────────────────────────────────────────────
 * @AuthenticationPrincipal 동작 원리
 * ───────────────────────────────────────────────
 * 1. 클라이언트가 Authorization: Bearer {JWT} 헤더 전송
 * 2. JwtAuthenticationFilter가 JWT를 파싱해 이메일 추출
 * 3. SecurityContext에 UsernamePasswordAuthenticationToken 등록
 *    → principal(주체) = 이메일 문자열
 * 4. @AuthenticationPrincipal String email 로 주입받음
 *
 * ───────────────────────────────────────────────
 * 이메일을 직접 Redis Key로 사용하는 이유
 * ───────────────────────────────────────────────
 * 이메일은 DB에서 UNIQUE 제약 조건이 걸린 고유값이므로
 * hashCode() 변환 없이 직접 장바구니 Key로 사용할 수 있다.
 * (hashCode는 충돌 위험 + 음수 반환 가능 → 사용 지양)
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 담기 API
     *
     * POST /api/cart
     * Authorization: Bearer {JWT 토큰}
     *
     * 요청 JSON:
     * {
     *   "productId": 1,
     *   "quantity": 3
     * }
     *
     * 이미 담긴 상품이면 수량을 덮어씀 (누적 아님).
     * 담은 후 Redis TTL 30일 재갱신.
     *
     * @param email   JWT에서 추출한 이메일 (장바구니 Key로 사용)
     * @param request 담을 상품 ID + 수량
     * @return 200 OK (응답 바디 없음)
     */
    @PostMapping
    public ResponseEntity<Void> addCart(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody AddCartRequest request) {

        // 이메일을 직접 CartService에 전달 (hashCode 변환 제거)
        cartService.addCart(email, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 장바구니 전체 조회 API
     *
     * GET /api/cart
     * Authorization: Bearer {JWT 토큰}
     *
     * 응답 JSON:
     * [
     *   { "productId": 1, "quantity": 3 },
     *   { "productId": 5, "quantity": 1 }
     * ]
     *
     * 장바구니가 비어있으면 빈 배열([]) 반환.
     *
     * @param email JWT에서 추출한 이메일
     * @return 200 OK + 장바구니 아이템 목록
     */
    @GetMapping
    public ResponseEntity<List<CartResponse>> getCart(
            @AuthenticationPrincipal String email) {

        List<CartResponse> response = cartService.getCart(email);
        return ResponseEntity.ok(response);
    }

    /**
     * 장바구니 특정 상품 삭제 API
     *
     * DELETE /api/cart/{productId}
     * Authorization: Bearer {JWT 토큰}
     *
     * 지정한 상품만 삭제하고 나머지 상품은 그대로 유지.
     * 존재하지 않는 productId를 삭제 요청해도 에러 없이 정상 처리됨.
     *
     * @param email     JWT에서 추출한 이메일
     * @param productId 삭제할 상품 ID (PathVariable)
     * @return 204 No Content (삭제 성공, 응답 바디 없음)
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeCartItem(
            @AuthenticationPrincipal String email,
            @PathVariable Long productId) {

        cartService.removeCartItem(email, productId);
        // 204 No Content: 처리 성공이지만 반환할 데이터가 없을 때 사용
        return ResponseEntity.noContent().build();
    }
}
