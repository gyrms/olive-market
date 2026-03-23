package com.yhg.olivemarket.global.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외 클래스
 *
 * 서비스 레이어에서 비즈니스 규칙 위반 시 이 예외를 던진다.
 * GlobalExceptionHandler가 이 예외를 잡아 클라이언트에 적절한 에러 응답을 반환한다.
 *
 * 사용 예시:
 *   throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
 *   throw new CustomException(ErrorCode.OUT_OF_STOCK);
 *
 * RuntimeException을 상속받아 unchecked exception으로 처리
 * → try-catch 강제 없이 자유롭게 던질 수 있음
 */
@Getter
public class CustomException extends RuntimeException {

    /**
     * 에러 코드 (HttpStatus + 메시지 포함)
     */
    private final ErrorCode errorCode;

    /**
     * 에러 코드를 받아 CustomException 생성
     *
     * @param errorCode ErrorCode 열거형 값
     */
    public CustomException(ErrorCode errorCode) {
        // 부모 클래스(RuntimeException)에 에러 메시지 전달
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
