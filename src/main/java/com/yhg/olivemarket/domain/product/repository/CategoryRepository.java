package com.yhg.olivemarket.domain.product.repository;

import com.yhg.olivemarket.domain.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 카테고리 Repository
 *
 * JpaRepository<Category, Long>:
 * - Category: 관리할 엔티티 타입
 * - Long: 기본키(PK) 타입
 *
 * 기본 CRUD 메서드 자동 제공 (save, findById, findAll, delete 등)
 * 현재는 기본 메서드만으로 충분하므로 추가 메서드 없음
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
