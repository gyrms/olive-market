package com.yhg.olivemarket.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

/**
 * JWT 토큰 발급 / 파싱 / 검증 컴포넌트
 *
 * JWT 구조: Header.Payload.Signature
 * - Header: 알고리즘 정보 (HS256)
 * - Payload: 클레임 (subject=이메일, role=권한, exp=만료시간)
 * - Signature: 비밀키로 서명한 값 → 위변조 방지
 *
 * 흐름:
 * 1. 로그인 성공 → generateToken()으로 JWT 발급
 * 2. 이후 요청마다 JwtAuthenticationFilter가 JWT 검증
 * 3. 검증 통과 시 getAuthentication()으로 SecurityContext에 인증 정보 등록
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /** Base64 인코딩된 비밀키 (application.yml에서 주입) */
    private final SecretKey secretKey;

    /** 액세스 토큰 만료 시간 (밀리초, application.yml에서 주입) */
    private final long expiration;

    /**
     * 생성자 주입: application.yml의 jwt.secret, jwt.expiration 값을 받아
     * HMAC-SHA 키 객체(SecretKey)로 변환
     *
     * Keys.hmacShaKeyFor(): 바이트 배열로 SecretKey 생성
     * Decoders.BASE64.decode(): Base64 문자열 → 바이트 배열
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
    }

    /**
     * JWT 토큰 생성
     *
     * 페이로드에 포함되는 클레임:
     * - subject: 이메일 (사용자 식별)
     * - role: 권한 (ROLE_USER / ROLE_ADMIN)
     * - issuedAt: 발급 시간
     * - expiration: 만료 시간
     *
     * @param email 이메일
     * @param role  권한 (예: "ROLE_USER")
     * @return 서명된 JWT 문자열 (Bearer 토큰)
     */
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(email)                    // 토큰 주체 (이메일)
                .claim("role", role)               // 커스텀 클레임 (권한)
                .issuedAt(now)                     // 발급 시간
                .expiration(expiredAt)             // 만료 시간
                .signWith(secretKey)               // HMAC-SHA256 서명
                .compact();                        // JWT 문자열로 직렬화
    }

    /**
     * JWT 토큰에서 이메일 추출
     *
     * @param token JWT 문자열 (Bearer 접두사 없이)
     * @return 이메일
     */
    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * JWT 토큰에서 권한 추출
     *
     * @param token JWT 문자열
     * @return 권한 문자열 (예: "ROLE_USER")
     */
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * JWT 토큰에서 Spring Security Authentication 객체 생성
     *
     * SecurityContext에 등록하기 위해 Authentication 구현체를 반환한다.
     * UsernamePasswordAuthenticationToken(principal, credentials, authorities)
     * - principal: 이메일 (사용자 식별)
     * - credentials: null (JWT 기반이므로 비밀번호 불필요)
     * - authorities: 권한 목록
     *
     * @param token JWT 문자열
     * @return Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        String email = getEmail(token);
        String role = getRole(token);

        // SimpleGrantedAuthority: Spring Security의 권한 구현체
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        return new UsernamePasswordAuthenticationToken(
                email,
                null,                                  // credentials
                Collections.singleton(authority)       // 권한 목록
        );
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * 검증 항목:
     * - 서명 위변조 여부 (SignatureException)
     * - 형식 이상 여부 (MalformedJwtException)
     * - 만료 여부 (ExpiredJwtException)
     * - 지원하지 않는 형식 (UnsupportedJwtException)
     * - 클레임 비어있음 (IllegalArgumentException)
     *
     * @param token JWT 문자열
     * @return true: 유효한 토큰, false: 유효하지 않은 토큰
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JWT] 만료된 토큰: {}", e.getMessage());
        } catch (SignatureException | MalformedJwtException e) {
            log.warn("[JWT] 위변조된 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("[JWT] 지원하지 않는 토큰 형식: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("[JWT] 빈 토큰: {}", e.getMessage());
        }
        return false;
    }

    /**
     * JWT 토큰 파싱 → Claims(페이로드) 추출
     *
     * verifyWith(secretKey): 서명 검증 (위변조 방지)
     * build().parseSignedClaims(): 파싱 + 검증 동시 수행
     *
     * @param token JWT 문자열
     * @return Claims (payload)
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
