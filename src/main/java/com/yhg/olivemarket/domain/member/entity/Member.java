package com.yhg.olivemarket.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 회원 엔티티
 *
 * ERD:
 *   Member (id, email, password, name, role, created_at)
 *
 * 설계 포인트:
 * - @NoArgsConstructor(PROTECTED): JPA 스펙상 기본 생성자 필요, 외부에서 직접 생성 방지
 * - @Builder: 객체 생성 시 가독성 향상 (new Member() 대신 Member.builder()...build())
 * - Auditing: @EntityListeners로 created_at 자동 세팅
 */
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자, 외부 직접 사용 금지
@EntityListeners(AuditingEntityListener.class)      // Spring Data Auditing 활성화
public class Member {

    /**
     * 기본키 (PK)
     * IDENTITY 전략: MySQL의 AUTO_INCREMENT 사용
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이메일 (로그인 아이디)
     * - 중복 불가 (UNIQUE 제약)
     * - nullable=false: DB에 NOT NULL 제약 추가
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 비밀번호 (BCrypt 해시값)
     * 절대 평문으로 저장하지 않는다.
     * PasswordEncoder.encode()로 인코딩 후 저장
     */
    @Column(nullable = false)
    private String password;

    /**
     * 회원 이름
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 권한 역할
     * - ROLE_USER: 일반 회원 (장바구니, 주문 가능)
     * - ROLE_ADMIN: 관리자 (상품 등록 가능)
     *
     * @Enumerated(STRING): DB에 "ROLE_USER" 문자열로 저장
     * (EnumType.ORDINAL은 Enum 순서 변경 시 데이터 오염 위험)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * 가입 일시
     * @CreatedDate: 엔티티 최초 저장 시 자동으로 현재 시간 세팅
     * updatable=false: 이후 수정 불가
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Builder 패턴으로 Member 생성
     *
     * 사용 예시:
     *   Member member = Member.builder()
     *       .email("test@test.com")
     *       .password(encodedPassword)
     *       .name("홍길동")
     *       .role(Role.ROLE_USER)
     *       .build();
     */
    @Builder
    public Member(String email, String password, String name, Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }
}
