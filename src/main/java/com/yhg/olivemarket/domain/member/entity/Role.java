package com.yhg.olivemarket.domain.member.entity;

/**
 * 회원 권한 열거형
 *
 * Spring Security는 권한명에 "ROLE_" 접두사를 강제한다.
 * hasRole("USER") → 내부적으로 "ROLE_USER"와 비교
 * hasAuthority("ROLE_USER") → 그대로 "ROLE_USER"와 비교
 */
public enum Role {

    /**
     * 일반 회원
     * - 상품 조회, 장바구니 담기, 주문 가능
     */
    ROLE_USER,

    /**
     * 관리자
     * - 일반 회원 기능 + 상품 등록 가능
     */
    ROLE_ADMIN
}
