package com.yhg.olivemarket.domain.member.service;

import com.yhg.olivemarket.domain.member.dto.request.JoinRequest;
import com.yhg.olivemarket.domain.member.dto.request.LoginRequest;
import com.yhg.olivemarket.domain.member.dto.response.MemberResponse;
import com.yhg.olivemarket.domain.member.entity.Member;
import com.yhg.olivemarket.domain.member.entity.Role;
import com.yhg.olivemarket.domain.member.repository.MemberRepository;
import com.yhg.olivemarket.global.auth.JwtTokenProvider;
import com.yhg.olivemarket.global.exception.CustomException;
import com.yhg.olivemarket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 서비스
 *
 * 비즈니스 로직 담당:
 * 1. 회원가입: 이메일 중복 확인 → 비밀번호 인코딩 → DB 저장
 * 2. 로그인: 회원 조회 → 비밀번호 검증 → JWT 발급
 *
 * @Transactional:
 * - 메서드 실행 전 트랜잭션 시작, 정상 종료 시 commit, 예외 시 rollback
 * - readOnly=true: 조회 전용 트랜잭션 (영속성 컨텍스트 flush 생략 → 성능 최적화)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;        // BCryptPasswordEncoder
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     *
     * 처리 순서:
     * 1. 이메일 중복 확인 (이미 존재하면 CustomException 던짐)
     * 2. 비밀번호 BCrypt 인코딩
     * 3. Member 엔티티 생성 및 DB 저장
     * 4. 저장된 회원 정보를 DTO로 변환해 반환
     *
     * @Transactional: DB 쓰기 작업이므로 readOnly 아닌 일반 트랜잭션 사용
     *
     * @param request 회원가입 요청 DTO (email, password, name)
     * @return MemberResponse (저장된 회원 정보)
     */
    @Transactional
    public MemberResponse join(JoinRequest request) {
        // 1. 이메일 중복 확인
        // existsByEmail: SELECT COUNT(*) > 0 쿼리 실행 (가볍고 빠름)
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 비밀번호 BCrypt 인코딩 (단방향 해시, 복호화 불가)
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Member 엔티티 생성 (Builder 패턴)
        Member member = Member.builder()
                .email(request.getEmail())
                .password(encodedPassword)     // 인코딩된 비밀번호 저장
                .name(request.getName())
                .role(Role.ROLE_USER)          // 회원가입 시 기본 권한: USER
                .build();

        // 4. DB 저장 (save 호출 시 INSERT 쿼리 실행)
        Member savedMember = memberRepository.save(member);

        // 5. 엔티티 → 응답 DTO 변환 (password 필드 제외)
        return MemberResponse.from(savedMember);
    }

    /**
     * 로그인 → JWT 토큰 발급
     *
     * 처리 순서:
     * 1. 이메일로 회원 조회 (없으면 예외)
     * 2. 비밀번호 검증 (BCrypt matches)
     * 3. JWT 액세스 토큰 생성 및 반환
     *
     * @param request 로그인 요청 DTO (email, password)
     * @return JWT 액세스 토큰 문자열
     */
    public String login(LoginRequest request) {
        // 1. 이메일로 회원 조회
        // orElseThrow: Optional이 비어있으면 예외 던짐 (null 체크 대신 사용)
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 비밀번호 검증
        // matches(평문, 해시값): 평문을 BCrypt로 해시해서 저장된 해시값과 비교
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 3. JWT 토큰 생성 (이메일 + 권한 포함)
        return jwtTokenProvider.generateToken(
                member.getEmail(),
                member.getRole().name()   // "ROLE_USER" 또는 "ROLE_ADMIN"
        );
    }
}
