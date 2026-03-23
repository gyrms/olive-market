package com.yhg.olivemarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * olive-market 애플리케이션 진입점
 *
 * @SpringBootApplication: 아래 3개 어노테이션의 조합
 * - @SpringBootConfiguration: 설정 클래스로 등록
 * - @EnableAutoConfiguration: classpath 기반 자동 설정 활성화
 * - @ComponentScan: 현재 패키지 하위의 @Component, @Service 등 자동 스캔
 *
 * @EnableJpaAuditing: Spring Data JPA Auditing 활성화
 * - Member 엔티티의 @CreatedDate가 동작하려면 이 어노테이션이 필요
 * - 엔티티 저장 시 created_at 자동 세팅
 */
@SpringBootApplication
@EnableJpaAuditing  // @CreatedDate, @LastModifiedDate 자동 처리
public class OliveMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(OliveMarketApplication.class, args);
    }
}
