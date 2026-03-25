package com.yhg.olivemarket.domain.order.repository;

import com.yhg.olivemarket.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 주문 Repository
 *
 * JpaRepository<Order, Long>:
 * - Order: 관리할 엔티티 타입
 * - Long: 기본키(PK) 타입
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 회원의 주문 목록 조회 (주문상품 + 상품 fetch join)
     *
     * 일반 조회 시 N+1 발생 구조:
     * - 주문 목록 조회 1번
     * - 각 주문의 orderItems 조회 N번
     * - 각 orderItem의 product 조회 N번
     *
     * fetch join으로 한 번의 쿼리로 해결:
     * SELECT o.*, oi.*, p.*
     * FROM orders o
     * JOIN order_item oi ON oi.order_id = o.id
     * JOIN product p ON p.id = oi.product_id
     * WHERE o.member_id = :memberId
     * ORDER BY o.created_at DESC
     *
     * DISTINCT: fetch join 시 주문이 orderItem 수만큼 중복 조회되는 문제 방지
     *
     * @param memberId 회원 ID
     * @return 해당 회원의 주문 목록 (최신순)
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.orderItems oi " +
            "JOIN FETCH oi.product " +
            "WHERE o.member.id = :memberId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findAllByMemberIdWithItems(@Param("memberId") Long memberId);

    /**
     * 주문 단건 조회 (주문상품 + 상품 fetch join)
     *
     * 주문 상세 조회 시 사용
     * 주문상품과 상품 정보를 한 번의 쿼리로 가져옴
     *
     * @param orderId 주문 ID
     * @return Optional<Order> (주문상품 + 상품 포함)
     */
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.orderItems oi " +
            "JOIN FETCH oi.product " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
}
