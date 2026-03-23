package com.yhg.olivemarket.domain.member.controller;

import com.yhg.olivemarket.domain.member.dto.request.JoinRequest;
import com.yhg.olivemarket.domain.member.dto.request.LoginRequest;
import com.yhg.olivemarket.domain.member.dto.response.MemberResponse;
import com.yhg.olivemarket.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 인증 관련 컨트롤러 (회원가입, 로그인)
 *
 * @RestController: @Controller + @ResponseBody
 * - 반환값을 자동으로 JSON으로 직렬화해 응답 body에 담음
 *
 * @RequestMapping("/api/auth"): 이 컨트롤러의 모든 API는 /api/auth로 시작
 *
 * SecurityConfig에서 /api/auth/** 는 permitAll()로 설정되어 있어
 * JWT 토큰 없이도 접근 가능하다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입 API
     *
     * POST /api/auth/join
     *
     * 요청:
     * {
     *   "email": "user@test.com",
     *   "password": "password123",
     *   "name": "홍길동"
     * }
     *
     * 응답 (201 Created):
     * {
     *   "id": 1,
     *   "email": "user@test.com",
     *   "name": "홍길동",
     *   "role": "ROLE_USER",
     *   "createdAt": "2024-03-20T10:00:00"
     * }
     *
     * @Valid: JoinRequest의 검증 어노테이션 실행
     *        검증 실패 시 GlobalExceptionHandler가 400 응답 반환
     *
     * @param request 회원가입 요청 DTO
     * @return 201 Created + 저장된 회원 정보
     */
    @PostMapping("/join")
    public ResponseEntity<MemberResponse> join(@Valid @RequestBody JoinRequest request) {
        MemberResponse response = memberService.join(request);
        // 201 Created: 새 리소스가 성공적으로 생성됨
        return ResponseEntity.status(201).body(response);
    }

    /**
     * 로그인 API
     *
     * POST /api/auth/login
     *
     * 요청:
     * {
     *   "email": "user@test.com",
     *   "password": "password123"
     * }
     *
     * 응답 (200 OK):
     * {
     *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     *
     * 이후 인증이 필요한 API 호출 시:
     * Authorization 헤더에 "Bearer {accessToken}" 추가
     *
     * @param request 로그인 요청 DTO
     * @return 200 OK + JWT 액세스 토큰
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String accessToken = memberService.login(request);
        // Map.of: 불변 Map 생성 (간단한 JSON 응답에 적합)
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }
}
