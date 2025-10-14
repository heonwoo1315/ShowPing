package com.ssginc.showpingrefactoring.domain.product.controller;

import com.ssginc.showpingrefactoring.domain.product.entity.Category;
import com.ssginc.showpingrefactoring.domain.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "카테고리 API", description = "상품 카테고리 관련 API입니다.")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "전체 카테고리 조회", description = "모든 상품 카테고리를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "카테고리 조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping
    public List<Category> getCategories() {
        return categoryService.getAllCategories();
    }
}
