package com.yhg.olivemarket.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 *
 * 요청 JSON 예시:
 * {
 *   "email": "user@test.com",
 *   "password": "password123"
 * }
 *
 * 로그인 성공 시 JWT 액세스 토큰을 응답으로 반환한다.
 */
@Getter
@NoArgsConstructor
public class LoginRequest {

    /**
     * 이메일
     */
    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    /**
     * 비밀번호 (평문)
     * PasswordEncoder.matches()로 DB의 인코딩된 비밀번호와 비교
     */
    @NotBlank(message = "비밀번호를 입력해 주세요.")
    private String password;
}
