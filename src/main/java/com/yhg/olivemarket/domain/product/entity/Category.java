package com.yhg.olivemarket.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 카테고리 엔티티
 *
 * ERD:
 *   Category (id, name)
 *
 * 카테고리 예시:
 * - 스킨케어
 * - 선케어
 * - 마스크팩
 * - 클렌징
 * - 헤어케어
 *
 * Product와의 관계:
 * - Category 1 : Product N (일대다)
 * - 하나의 카테고리에 여러 상품이 속할 수 있음
 * - Product 엔티티에서 @ManyToOne으로 Category를 참조
 */
@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자, 외부 직접 생성 금지
public class Category {

    /**
     * 기본키 (PK)
     * IDENTITY 전략: MySQL의 AUTO_INCREMENT 사용
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 카테고리명
     * - unique: 중복 카테고리명 방지
     * - length 50: 50자 제한
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Builder 패턴으로 Category 생성
     *
     * 사용 예시:
     *   Category category = Category.builder()
     *       .name("스킨케어")
     *       .build();
     */
    @Builder
    public Category(String name) {
        this.name = name;
    }
}
