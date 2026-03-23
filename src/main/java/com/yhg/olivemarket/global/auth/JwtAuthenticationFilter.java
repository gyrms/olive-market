package com.yhg.olivemarket.global.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 *
 * 모든 HTTP 요청이 들어올 때 실행되며, JWT 토큰을 검증하고
 * 인증 정보를 SecurityContext에 등록한다.
 *
 * OncePerRequestFilter: 하나의 요청에 단 한 번만 실행되도록 보장
 * (필터 체인에서 중복 실행 방지)
 *
 * 필터 실행 순서:
 * 요청 → [JwtAuthenticationFilter] → SecurityContext에 인증 등록 → Controller
 *
 * 처리 흐름:
 * 1. Authorization 헤더에서 "Bearer {token}" 추출
 * 2. JWT 유효성 검증
 * 3. 유효하면 Authentication 객체를 SecurityContext에 등록
 * 4. 필터 체인 계속 실행 (filterChain.doFilter)
 *
 * 인증이 없는 요청 (공개 API):
 * - 토큰이 없거나 유효하지 않으면 SecurityContext 등록 없이 통과
 * - SecurityConfig에서 permitAll()로 설정된 경로는 인증 없이 접근 가능
 * - 인증이 필요한 경로는 Spring Security가 401 응답 반환
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Authorization 헤더 이름 */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** Bearer 토큰 접두사 */
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 실제 필터 로직
     *
     * @param request     HTTP 요청
     * @param response    HTTP 응답
     * @param filterChain 다음 필터 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 요청 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰이 있고 유효하면 SecurityContext에 인증 정보 등록
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // JWT에서 이메일, 권한 정보로 Authentication 객체 생성
            Authentication authentication = jwtTokenProvider.getAuthentication(token);

            // SecurityContext에 등록 → 이후 컨트롤러에서 @AuthenticationPrincipal로 접근 가능
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("[JWT] 인증 성공 - 사용자: {}", authentication.getName());
        }

        // 3. 다음 필터로 요청 전달 (필터 체인 계속)
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰 추출
     *
     * Authorization 헤더 형식: "Bearer eyJhbGci..."
     * "Bearer " 접두사를 제거하고 순수 토큰 문자열만 반환
     *
     * @param request HTTP 요청
     * @return 토큰 문자열 또는 null (헤더가 없거나 형식이 맞지 않을 경우)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        // hasText(): null, 빈 문자열, 공백 문자열 모두 false 반환
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            // "Bearer " (7글자) 이후의 토큰 문자열만 추출
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
