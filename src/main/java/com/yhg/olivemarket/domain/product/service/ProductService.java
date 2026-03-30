package com.yhg.olivemarket.domain.product.service;

import com.yhg.olivemarket.domain.product.dto.request.CreateProductRequest;
import com.yhg.olivemarket.domain.product.dto.request.ProductSearchRequest;
import com.yhg.olivemarket.domain.product.dto.response.ProductResponse;
import com.yhg.olivemarket.domain.product.entity.Category;
import com.yhg.olivemarket.domain.product.entity.Product;
import com.yhg.olivemarket.domain.product.repository.CategoryRepository;
import com.yhg.olivemarket.domain.product.repository.ProductQueryRepository;
import com.yhg.olivemarket.domain.product.repository.ProductRepository;
import com.yhg.olivemarket.global.exception.CustomException;
import com.yhg.olivemarket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 서비스
 *
 * 비즈니스 로직 담당:
 * 1. 상품 목록 동적 검색 (QueryDSL)
 * 2. 상품 상세 조회
 * 3. 상품 등록 (ADMIN 전용)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본 조회 전용 트랜잭션 (성능 최적화)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductQueryRepository productQueryRepository; // QueryDSL 동적 검색
    private final CategoryRepository categoryRepository;

    /**
     * 상품 목록 동적 검색
     *
     * ProductQueryRepository에 검색 조건을 위임한다.
     * 조건이 없으면 전체 상품 반환, 있으면 해당 조건으로 필터링
     *
     * @param request 검색 조건 (keyword, categoryId, minPrice, maxPrice)
     * @return 조건에 맞는 상품 목록 DTO
     */
    public List<ProductResponse> search(ProductSearchRequest request) {
        List<Product> products = productQueryRepository.search(request);

        // 엔티티 리스트 → DTO 리스트 변환
        // stream().map().collect(): Java 스트림으로 리스트 변환
        return products.stream()
                .map(ProductResponse::from) // 각 Product → ProductResponse 변환
                .collect(Collectors.toList());
    }

    /**
     * 상품 상세 조회
     *
     * fetch join으로 카테고리를 함께 조회 (N+1 방지)
     * 없는 상품 ID 요청 시 CustomException 발생
     *
     * @param id 상품 ID
     * @return 상품 상세 정보 DTO
     */
    public ProductResponse findById(Long id) {
        // fetch join으로 카테고리 포함 조회
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        return ProductResponse.from(product);
    }

    /**
     * 상품 등록 (ADMIN 전용)
     *
     * SecurityConfig에서 POST /api/products는 ADMIN만 접근 가능하도록 설정되어 있음
     *
     * 처리 순서:
     * 1. categoryId로 Category 조회 (없으면 예외)
     * 2. Product 엔티티 생성
     * 3. DB 저장
     * 4. DTO 변환 후 반환
     *
     * @Transactional: DB 쓰기 작업 → readOnly 아닌 일반 트랜잭션
     *
     * @param request 상품 등록 요청 DTO
     * @return 저장된 상품 정보 DTO
     */
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        // 1. 카테고리 조회 (없으면 예외)
        // CATEGORY_NOT_FOUND: 상품 등록 시 존재하지 않는 카테고리 ID를 요청한 경우
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 2. Product 엔티티 생성
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .description(request.getDescription())
                .category(category)
                .build();

        // 3. DB 저장
        Product savedProduct = productRepository.save(product);

        return ProductResponse.from(savedProduct);
    }
}
