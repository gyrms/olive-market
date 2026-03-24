package com.yhg.olivemarket.domain.product.controller;

import com.yhg.olivemarket.domain.product.dto.request.CreateProductRequest;
import com.yhg.olivemarket.domain.product.dto.request.ProductSearchRequest;
import com.yhg.olivemarket.domain.product.dto.response.ProductResponse;
import com.yhg.olivemarket.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상품 컨트롤러
 *
 * SecurityConfig 인가 설정:
 * - GET /api/products/**  → 인증 없이 접근 가능 (permitAll)
 * - POST /api/products    → ADMIN만 접근 가능 (hasRole("ADMIN"))
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 목록 동적 검색 API
     *
     * GET /api/products
     * GET /api/products?keyword=크림
     * GET /api/products?categoryId=1&minPrice=10000&maxPrice=50000
     * GET /api/products?keyword=크림&categoryId=1&minPrice=10000&maxPrice=50000
     *
     * @ModelAttribute: 쿼리 파라미터를 DTO 객체로 자동 바인딩
     *   ?keyword=크림&minPrice=10000 → ProductSearchRequest 객체로 변환
     *   (@RequestBody는 JSON 바디용, @ModelAttribute는 쿼리 파라미터용)
     *
     * @param request 검색 조건 (쿼리 파라미터)
     * @return 200 OK + 상품 목록
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> search(@ModelAttribute ProductSearchRequest request) {
        List<ProductResponse> response = productService.search(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 상세 조회 API
     *
     * GET /api/products/{id}
     *
     * @PathVariable: URL 경로의 {id} 값을 파라미터로 바인딩
     *   /api/products/1 → id = 1L
     *
     * @param id 상품 ID
     * @return 200 OK + 상품 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        ProductResponse response = productService.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 등록 API (ADMIN 전용)
     *
     * POST /api/products
     * Authorization: Bearer {ADMIN JWT 토큰} 필요
     *
     * @param request 상품 등록 요청 DTO
     * @return 201 Created + 등록된 상품 정보
     */
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.status(201).body(response);
    }
}
