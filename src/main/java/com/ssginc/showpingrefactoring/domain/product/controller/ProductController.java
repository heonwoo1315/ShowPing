package com.ssginc.showpingrefactoring.domain.product.controller;

import com.ssginc.showpingrefactoring.domain.product.dto.object.ProductDto;
import com.ssginc.showpingrefactoring.domain.review.dto.object.ReviewDto;
import com.ssginc.showpingrefactoring.domain.product.service.ProductService;
import com.ssginc.showpingrefactoring.domain.review.service.implement.ReviewServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "상품 API", description = "상품 목록, 상세 조회, 정렬, 리뷰 관련 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ReviewServiceImpl reviewService;

    @Operation(summary = "카테고리별 상품 조회", description = "카테고리 번호에 따라 페이징 및 정렬된 상품 목록을 반환합니다.")
    @GetMapping("/{categoryNo}")
    public Page<ProductDto> getProductsByCategory(
            @Parameter(description = "카테고리 번호") @PathVariable Long categoryNo,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam int page,
            @Parameter(description = "페이지당 상품 수") @RequestParam int size,
            @Parameter(description = "정렬 옵션: quantity-desc, price-desc, price-asc, sale-desc, sale-asc") @RequestParam String sort) {

        Pageable pageable = PageRequest.of(page, size, getSort(sort));
        return productService.getProductsByCategory(categoryNo, pageable);
    }

    @Operation(summary = "판매 수량 기준 인기 상품 조회", description = "카테고리 내 판매 수량이 많은 상위 상품을 반환합니다.")
    @GetMapping("/{cateogryNo}/saleQuantity")
    public List<ProductDto> getTopProductsBySaleQuantity(
            @Parameter(description = "카테고리 번호") @PathVariable("cateogryNo") Long cateogryNo) {
        return productService.getTopProductsBySaleQuantity(cateogryNo);
    }

    @Operation(summary = "할인율 기준 인기 상품 조회", description = "카테고리 내 할인율이 높은 상위 상품을 반환합니다.")
    @GetMapping("/{cateogryNo}/sale")
    public List<ProductDto> getTopProductsBySale(
            @Parameter(description = "카테고리 번호") @PathVariable("cateogryNo") Long cateogryNo) {
        return productService.getTopProductsBySale(cateogryNo);
    }

    @Operation(summary = "상품 상세 조회", description = "상품 번호에 해당하는 상품 상세 정보를 반환합니다.")
    @GetMapping("/detail/{productNo}")
    public ProductDto getProductDetail(
            @Parameter(description = "상품 번호") @PathVariable Long productNo) {
        return productService.getProductById(productNo);
    }

    @Operation(summary = "상품 리뷰 목록 조회", description = "해당 상품에 대한 모든 리뷰를 반환합니다.")
    @GetMapping("/reviews/{productNo}")
    public List<ReviewDto> getProductReviews(
            @Parameter(description = "상품 번호") @PathVariable Long productNo) {
        return reviewService.getReviewsByProductNo(productNo);
    }

    // 내부에서 사용되는 정렬 옵션 매핑 메서드
    private Sort getSort(String sortOption) {
        switch (sortOption) {
            case "quantity-desc":
                return Sort.by(Sort.Order.desc("productSaleQuantity"));
            case "price-desc":
                return Sort.by(Sort.Order.desc("productPrice"));
            case "price-asc":
                return Sort.by(Sort.Order.asc("productPrice"));
            case "sale-desc":
                return Sort.by(Sort.Order.desc("productSale"));
            case "sale-asc":
                return Sort.by(Sort.Order.asc("productSale"));
            default:
                return Sort.by(Sort.Order.desc("productSaleQuantity"));
        }
    }
}
