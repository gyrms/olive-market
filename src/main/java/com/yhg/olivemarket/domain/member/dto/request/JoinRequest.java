package com.yhg.olivemarket.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 *
 * Controller에서 @RequestBody로 받는 JSON 요청 데이터
 *
 * 요청 JSON 예시:
 * {
 *   "email": "user@test.com",
 *   "password": "password123",
 *   "name": "홍길동"
 * }
 *
 * @Valid와 함께 사용 시 아래 검증 어노테이션이 자동으로 동작한다.
 * 검증 실패 시 GlobalExceptionHandler가 400 응답을 반환한다.
 */
@Getter
@NoArgsConstructor
public class JoinRequest {

    /**
     * 이메일 (로그인 아이디)
     * - @Email: 이메일 형식 검증 (xxx@xxx.xxx)
     * - @NotBlank: null, 빈 문자열, 공백 문자열 모두 거부
     */
    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    /**
     * 비밀번호 (평문, 서비스에서 BCrypt로 인코딩)
     * - @Size: 8~20자 제한
     */
    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이여야 합니다.")
    private String password;

    /**
     * 이름
     */
    @NotBlank(message = "이름을 입력해 주세요.")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String name;
}
