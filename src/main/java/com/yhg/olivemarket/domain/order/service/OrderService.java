package com.yhg.olivemarket.domain.order.service;

import com.yhg.olivemarket.domain.member.entity.Member;
import com.yhg.olivemarket.domain.member.repository.MemberRepository;
import com.yhg.olivemarket.domain.order.dto.request.CreateOrderRequest;
import com.yhg.olivemarket.domain.order.dto.response.OrderResponse;
import com.yhg.olivemarket.domain.order.entity.Order;
import com.yhg.olivemarket.domain.order.entity.OrderItem;
import com.yhg.olivemarket.domain.order.entity.OrderStatus;
import com.yhg.olivemarket.domain.order.repository.OrderRepository;
import com.yhg.olivemarket.domain.product.entity.Product;
import com.yhg.olivemarket.domain.product.repository.ProductRepository;
import com.yhg.olivemarket.global.exception.CustomException;
import com.yhg.olivemarket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 서비스
 *
 * 비즈니스 로직 담당:
 * 1. 주문 생성: 상품 조회 → 재고 차감 → 주문 저장
 * 2. 내 주문 목록 조회
 * 3. 주문 상세 조회
 *
 * @Transactional 핵심 포인트:
 * - 주문 생성은 여러 테이블에 걸친 쓰기 작업 (Order + OrderItem + Product 재고 차감)
 * - 중간에 예외 발생 시 전체 롤백되어 데이터 정합성 보장
 * - 예: 상품A 재고 차감 성공 후 상품B 재고 부족 예외 → 상품A 재고 차감도 롤백
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본 조회 전용 트랜잭션
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    /**
     * 주문 생성
     *
     * 처리 순서:
     * 1. 이메일로 회원 조회
     * 2. 각 주문 상품에 대해:
     *    a. 상품 조회 (없으면 예외)
     *    b. 재고 차감 (부족하면 예외)
     *    c. OrderItem 생성 (주문 당시 가격 스냅샷)
     * 3. 총 주문 금액 계산
     * 4. Order 생성 + OrderItem 추가
     * 5. DB 저장 (cascade로 OrderItem도 함께 저장)
     *
     * @Transactional:
     * - 재고 차감(Product) + 주문 저장(Order, OrderItem) 을 하나의 트랜잭션으로 처리
     * - 예외 발생 시 전체 롤백 → 재고 차감 후 주문 저장 실패해도 재고가 복구됨
     *
     * @param email   JWT에서 추출한 이메일 (주문자 식별)
     * @param request 주문 생성 요청 DTO
     * @return 생성된 주문 정보 DTO
     */
    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest request) {
        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 각 주문 상품 처리 → OrderItem 리스트 생성
        List<OrderItem> orderItems = request.getOrderItems().stream()
                .map(itemRequest -> {
                    // 2-a. 상품 조회 (없으면 PRODUCT_NOT_FOUND 예외)
                    Product product = productRepository.findByIdWithCategory(itemRequest.getProductId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

                    // 2-b. 재고 차감
                    // product.reduceStock()이 재고 부족 시 IllegalArgumentException 던짐
                    // → @Transactional로 인해 이전 재고 차감도 모두 롤백
                    try {
                        product.reduceStock(itemRequest.getQuantity());
                    } catch (IllegalArgumentException e) {
                        throw new CustomException(ErrorCode.OUT_OF_STOCK);
                    }

                    // 2-c. OrderItem 생성 (주문 당시 가격 스냅샷으로 저장)
                    return OrderItem.builder()
                            .product(product)
                            .quantity(itemRequest.getQuantity())
                            .price(product.getPrice()) // 현재 가격을 스냅샷으로 저장
                            .build();
                })
                .collect(Collectors.toList());

        // 3. 총 주문 금액 계산 (각 OrderItem의 소계 합산)
        int totalPrice = orderItems.stream()
                .mapToInt(OrderItem::getSubTotal) // price * quantity
                .sum();

        // 4. Order 생성
        Order order = Order.builder()
                .member(member)
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING) // 초기 상태: 주문 대기
                .build();

        // 5. OrderItem을 Order에 추가 (양방향 연관관계 설정)
        // addOrderItem()이 내부에서 orderItem.setOrder(order)도 호출
        orderItems.forEach(order::addOrderItem);

        // 6. Order 저장 (cascade = PERSIST로 OrderItem도 함께 저장)
        Order savedOrder = orderRepository.save(order);

        return OrderResponse.from(savedOrder);
    }

    /**
     * 내 주문 목록 조회
     *
     * fetch join으로 주문 + 주문상품 + 상품을 한 번의 쿼리로 조회 (N+1 방지)
     *
     * @param email JWT에서 추출한 이메일
     * @return 내 주문 목록 (최신순)
     */
    public List<OrderResponse> getMyOrders(String email) {
        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // fetch join으로 주문 목록 조회
        List<Order> orders = orderRepository.findAllByMemberIdWithItems(member.getId());

        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 주문 상세 조회
     *
     * 본인 주문만 조회 가능 (다른 회원의 주문 접근 시 예외)
     *
     * @param email   JWT에서 추출한 이메일
     * @param orderId 조회할 주문 ID
     * @return 주문 상세 정보 DTO
     */
    public OrderResponse getOrder(String email, Long orderId) {
        // 주문 조회 (주문상품 + 상품 fetch join)
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 본인 주문 여부 확인
        // order.getMember().getEmail()은 LAZY 로딩이지만
        // 이미 member가 영속성 컨텍스트에 있으므로 추가 쿼리 없이 접근 가능
        if (!order.getMember().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return OrderResponse.from(order);
    }
}
