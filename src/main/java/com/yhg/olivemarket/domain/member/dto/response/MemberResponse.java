package com.yhg.olivemarket.domain.member.dto.response;

import com.yhg.olivemarket.domain.member.entity.Member;
import com.yhg.olivemarket.domain.member.entity.Role;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 회원 정보 응답 DTO
 *
 * 클라이언트에 반환할 회원 데이터
 * Member 엔티티를 직접 반환하지 않는다 → 보안상 민감한 필드(password) 노출 방지
 *
 * 응답 JSON 예시:
 * {
 *   "id": 1,
 *   "email": "user@test.com",
 *   "name": "홍길동",
 *   "role": "ROLE_USER",
 *   "createdAt": "2024-03-20T10:00:00"
 * }
 */
@Getter
public class MemberResponse {

    /** 회원 ID */
    private final Long id;

    /** 이메일 */
    private final String email;

    /** 이름 */
    private final String name;

    /** 권한 */
    private final Role role;

    /** 가입 일시 */
    private final LocalDateTime createdAt;

    /**
     * Member 엔티티 → MemberResponse DTO 변환
     *
     * 정적 팩토리 메서드 패턴:
     * 생성자 대신 의미 있는 이름(from)으로 변환 의도를 명확히 표현
     *
     * @param member Member 엔티티
     * @return MemberResponse
     */
    public static MemberResponse from(Member member) {
        return new MemberResponse(member);
    }

    private MemberResponse(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.role = member.getRole();
        this.createdAt = member.getCreatedAt();
    }
}
