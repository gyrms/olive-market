package com.yhg.olivemarket.global.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스
 *
 * @EnableWebSecurity: Spring Security 활성화
 * - 자동으로 HTTP 보안 설정이 적용됨
 *
 * JWT 기반 Stateless 인증 구조:
 * - 서버에 세션을 저장하지 않음 (SessionCreationPolicy.STATELESS)
 * - 모든 요청마다 JWT 토큰으로 인증
 * - CSRF 보호 비활성화 (세션을 쓰지 않으므로 불필요)
 *
 * 필터 등록 위치:
 * JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter 앞에 삽입
 * → JWT 인증이 Spring Security 기본 인증보다 먼저 실행됨
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * BCrypt 비밀번호 인코더 Bean 등록
     *
     * BCrypt: 단방향 해시 알고리즘
     * - 동일한 평문이라도 매번 다른 해시값 생성 (Salt 사용)
     * - 무차별 대입 공격에 강함 (느린 알고리즘 의도적 사용)
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 보안 필터 체인 설정
     *
     * @param http HttpSecurity 빌더
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화: REST API + JWT는 세션을 사용하지 않으므로 CSRF 불필요
                .csrf(AbstractHttpConfigurer::disable)

                // Form 로그인 비활성화: JWT 로그인 방식 사용
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화: JWT 방식 사용
                .httpBasic(AbstractHttpConfigurer::disable)

                // 세션 정책: STATELESS → 서버에 세션 저장 안 함
                // JWT가 모든 요청의 인증 정보를 담고 있어 세션 불필요
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 요청별 인가(Authorization) 설정
                .authorizeHttpRequests(auth -> auth
                        // 회원가입, 로그인 → 인증 없이 접근 가능 (공개 API)
                        .requestMatchers("/api/auth/**").permitAll()

                        // 상품 목록, 상품 상세 조회 → 인증 없이 접근 가능
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // 상품 등록 → ADMIN만 접근 가능
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")

                        // 그 외 모든 요청 → 인증 필요 (JWT 토큰 있어야 함)
                        .anyRequest().authenticated()
                )

                // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 삽입
                // → 요청마다 JWT 검증 후 SecurityContext에 인증 정보 등록
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
