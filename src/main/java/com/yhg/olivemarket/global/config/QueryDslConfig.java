package com.yhg.olivemarket.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 설정 클래스
 *
 * JPAQueryFactory를 Spring Bean으로 등록한다.
 * Repository에서 @RequiredArgsConstructor로 주입받아 사용한다.
 *
 * 사용 예시:
 *   private final JPAQueryFactory queryFactory;
 *   queryFactory.selectFrom(QProduct.product).where(...).fetch();
 */
@Configuration
public class QueryDslConfig {

    /**
     * EntityManager: JPA 영속성 컨텍스트와 상호작용하는 핵심 인터페이스
     * @PersistenceContext: 컨테이너가 관리하는 EntityManager를 주입
     * (트랜잭션 범위의 EntityManager가 자동으로 바인딩됨)
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JPAQueryFactory Bean 등록
     *
     * @return JPAQueryFactory - QueryDSL 쿼리 실행의 진입점
     *
     * 내부적으로 EntityManager를 사용해 JPQL을 생성하고 실행한다.
     * 멀티스레드 환경에서 안전하다 (EntityManager는 스레드 로컬로 관리됨).
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
