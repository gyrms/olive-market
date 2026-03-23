package com.yhg.olivemarket.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 *
 * @RestControllerAdvice: 모든 @RestController에서 발생하는 예외를 한 곳에서 처리
 * - @ControllerAdvice + @ResponseBody 합친 것
 * - 각 Controller에 try-catch를 쓰지 않아도 됨
 *
 * 처리하는 예외 종류:
 * 1. CustomException   → 비즈니스 로직 예외 (4xx, 5xx)
 * 2. MethodArgumentNotValidException → @Valid 검증 실패 (400)
 * 3. Exception         → 나머지 모든 예외 (500)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리 (CustomException)
     *
     * 서비스 레이어에서 throw new CustomException(ErrorCode.XXX) 로 던진 예외를 처리
     *
     * 응답 예시:
     * {
     *   "status": 404,
     *   "message": "존재하지 않는 회원입니다."
     * }
     *
     * @param e CustomException
     * @return ResponseEntity (에러코드에 맞는 HTTP 상태 + 바디)
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException e) {
        log.warn("[CustomException] {}: {}", e.getErrorCode(), e.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("status", e.getErrorCode().getStatus().value());
        body.put("message", e.getMessage());

        return ResponseEntity.status(e.getErrorCode().getStatus()).body(body);
    }

    /**
     * @Valid 검증 실패 예외 처리 (MethodArgumentNotValidException)
     *
     * @RequestBody에 @Valid를 붙였을 때 검증 실패 시 발생
     *
     * 응답 예시:
     * {
     *   "status": 400,
     *   "message": "잘못된 입력값입니다.",
     *   "errors": {
     *     "email": "이메일 형식이 올바르지 않습니다.",
     *     "password": "비밀번호는 8자 이상이어야 합니다."
     *   }
     * }
     *
     * @param e MethodArgumentNotValidException
     * @return ResponseEntity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("[ValidationException] {}", e.getMessage());

        // 각 필드별 에러 메시지 수집
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("message", ErrorCode.INVALID_INPUT_VALUE.getMessage());
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 기타 모든 예외 처리 (Exception)
     *
     * 예상치 못한 런타임 예외가 발생했을 때의 마지막 방어선
     * 클라이언트에 내부 스택 트레이스를 노출하지 않고 일반 메시지만 반환
     *
     * @param e Exception
     * @return ResponseEntity (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        // 스택 트레이스는 서버 로그에만 기록 (클라이언트에 노출 금지)
        log.error("[UnhandledException] {}", e.getMessage(), e);

        Map<String, Object> body = new HashMap<>();
        body.put("status", 500);
        body.put("message", ErrorCode.INTERNAL_SERVER_ERROR.getMessage());

        return ResponseEntity.internalServerError().body(body);
    }
}
