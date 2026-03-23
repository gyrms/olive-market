package com.yhg.olivemarket.domain.member.repository;

import com.yhg.olivemarket.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 회원 Repository
 *
 * JpaRepository<Member, Long>:
 * - Member: 관리할 엔티티 타입
 * - Long: 기본키(PK) 타입
 *
 * JpaRepository 상속만으로 기본 CRUD 메서드를 자동 제공받는다:
 * - save(), findById(), findAll(), delete() 등
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 이메일로 회원 조회
     *
     * Spring Data JPA 메서드 네이밍 규칙:
     * findBy + 필드명(Email) → WHERE email = ? 쿼리 자동 생성
     *
     * Optional로 감싸서 null 대신 빈 Optional 반환
     * → NullPointerException 방지, orElseThrow()로 처리
     *
     * @param email 조회할 이메일
     * @return Optional<Member>
     */
    Optional<Member> findByEmail(String email);

    /**
     * 이메일 중복 여부 확인
     *
     * existsBy + 필드명(Email) → SELECT COUNT(*) > 0 쿼리 자동 생성
     * findByEmail보다 가볍다 (실제 데이터를 가져오지 않음)
     *
     * @param email 중복 확인할 이메일
     * @return true: 이미 존재, false: 사용 가능
     */
    boolean existsByEmail(String email);
}
