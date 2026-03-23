package com.yhg.olivemarket.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전역 에러 코드 열거형
 *
 * 에러 코드 네이밍 규칙:
 * - 4xx: 클라이언트 오류 (잘못된 요청, 인증 실패 등)
 * - 5xx: 서버 오류 (예상치 못한 오류)
 *
 * 각 에러코드는 (HttpStatus, 에러 메시지)로 구성된다.
 * GlobalExceptionHandler에서 CustomException을 잡아 이 코드를 응답에 담는다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ============================================================
    // 공통 에러 (C)
    // ============================================================
    /** 서버 내부 오류 - 예상치 못한 예외 발생 시 */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    /** 잘못된 요청 파라미터 */
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),

    // ============================================================
    // 회원 에러 (M)
    // ============================================================
    /** 이미 가입된 이메일로 회원가입 시도 */
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    /** 존재하지 않는 회원 조회 */
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

    /** 비밀번호 불일치 */
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),

    // ============================================================
    // 인증/인가 에러 (A)
    // ============================================================
    /** JWT 토큰이 없거나 형식이 잘못됨 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    /** JWT 토큰 만료 */
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    /** 접근 권한 없음 (ADMIN 전용 API를 USER가 접근) */
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // ============================================================
    // 상품 에러 (P)
    // ============================================================
    /** 존재하지 않는 상품 조회 */
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),

    /** 재고 부족 (주문 수량 > 현재 재고) */
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),

    // ============================================================
    // 주문 에러 (O)
    // ============================================================
    /** 존재하지 않는 주문 조회 */
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),

    /** 본인 주문이 아닌 주문 접근 */
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인의 주문만 조회할 수 있습니다.");

    /** HTTP 응답 상태 코드 */
    private final HttpStatus status;

    /** 사용자에게 반환할 에러 메시지 */
    private final String message;
}
