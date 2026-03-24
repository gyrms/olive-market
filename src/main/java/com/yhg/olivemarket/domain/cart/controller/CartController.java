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
 * 모든 API는 JWT 인증 필요 (SecurityConfig에서 /api/cart/** → authenticated())
 *
 * @AuthenticationPrincipal:
 * - JwtAuthenticationFilter에서 SecurityContext에 등록한 Authentication 객체의
 *   principal(이메일)을 파라미터로 주입받음
 * - JWT에서 추출한 이메일로 회원을 식별
 *
 * 주의: 현재 구현은 memberId를 이메일로 대체하고 있음
 * 실제로는 이메일로 Member를 조회해서 memberId를 사용해야 함
 * 간결함을 위해 CartService에서 이메일을 Key로 사용하도록 설계
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
     * Authorization: Bearer {JWT 토큰} 필요
     *
     * 요청:
     * { "productId": 1, "quantity": 3 }
     *
     * @param email   JWT에서 추출한 이메일 (회원 식별용)
     * @param request 담을 상품 ID + 수량
     * @return 200 OK
     */
    @PostMapping
    public ResponseEntity<Void> addCart(
            @AuthenticationPrincipal String email, // JWT principal (이메일)
            @Valid @RequestBody AddCartRequest request) {

        // 이메일을 해시코드로 변환해서 memberId 대신 사용
        // TODO: MemberRepository로 이메일 → memberId 조회로 교체 권장
        cartService.addCart((long) email.hashCode(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 장바구니 전체 조회 API
     *
     * GET /api/cart
     * Authorization: Bearer {JWT 토큰} 필요
     *
     * 응답:
     * [
     *   { "productId": 1, "quantity": 3 },
     *   { "productId": 5, "quantity": 1 }
     * ]
     *
     * @param email JWT에서 추출한 이메일
     * @return 200 OK + 장바구니 목록
     */
    @GetMapping
    public ResponseEntity<List<CartResponse>> getCart(
            @AuthenticationPrincipal String email) {

        List<CartResponse> response = cartService.getCart((long) email.hashCode());
        return ResponseEntity.ok(response);
    }

    /**
     * 장바구니 특정 상품 삭제 API
     *
     * DELETE /api/cart/{productId}
     * Authorization: Bearer {JWT 토큰} 필요
     *
     * @param email     JWT에서 추출한 이메일
     * @param productId 삭제할 상품 ID
     * @return 204 No Content (삭제 성공, 응답 바디 없음)
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeCartItem(
            @AuthenticationPrincipal String email,
            @PathVariable Long productId) {

        cartService.removeCartItem((long) email.hashCode(), productId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
