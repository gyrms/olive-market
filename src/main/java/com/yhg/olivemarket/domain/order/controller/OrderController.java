package com.yhg.olivemarket.domain.order.controller;

import com.yhg.olivemarket.domain.order.dto.request.CreateOrderRequest;
import com.yhg.olivemarket.domain.order.dto.response.OrderResponse;
import com.yhg.olivemarket.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 주문 컨트롤러
 *
 * 모든 API는 JWT 인증 필요 (SecurityConfig에서 /api/orders/** → authenticated())
 *
 * @AuthenticationPrincipal String email:
 * - JwtAuthenticationFilter가 SecurityContext에 저장한 이메일을 주입받음
 * - 주문자 식별에 사용
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성 API
     *
     * POST /api/orders
     * Authorization: Bearer {JWT 토큰} 필요
     *
     * 요청:
     * {
     *   "orderItems": [
     *     { "productId": 1, "quantity": 2 },
     *     { "productId": 3, "quantity": 1 }
     *   ]
     * }
     *
     * 응답 (201 Created):
     * {
     *   "orderId": 1,
     *   "totalPrice": 75000,
     *   "status": "PENDING",
     *   "orderItems": [...]
     * }
     *
     * @param email   JWT에서 추출한 이메일
     * @param request 주문 생성 요청 DTO
     * @return 201 Created + 생성된 주문 정보
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse response = orderService.createOrder(email, request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * 내 주문 목록 조회 API
     *
     * GET /api/orders
     * Authorization: Bearer {JWT 토큰} 필요
     *
     * 응답 (200 OK):
     * [
     *   { "orderId": 2, "totalPrice": 50000, "status": "PENDING", ... },
     *   { "orderId": 1, "totalPrice": 75000, "status": "CONFIRMED", ... }
     * ]
     *
     * @param email JWT에서 추출한 이메일
     * @return 200 OK + 내 주문 목록 (최신순)
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal String email) {

        List<OrderResponse> response = orderService.getMyOrders(email);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상세 조회 API
     *
     * GET /api/orders/{id}
     * Authorization: Bearer {JWT 토큰} 필요
     *
     * 다른 회원의 주문 조회 시 403 Forbidden 반환
     *
     * @param email   JWT에서 추출한 이메일
     * @param orderId 조회할 주문 ID
     * @return 200 OK + 주문 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal String email,
            @PathVariable("id") Long orderId) {

        OrderResponse response = orderService.getOrder(email, orderId);
        return ResponseEntity.ok(response);
    }
}
