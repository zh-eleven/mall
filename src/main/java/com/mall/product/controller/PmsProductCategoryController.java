package com.mall.product.controller;

import com.mall.common.api.ApiResult;
import com.mall.product.dto.ProductCategoryCreateDTO;
import com.mall.product.dto.ProductCategoryUpdateDTO;
import com.mall.product.service.PmsProductCategoryService;
import com.mall.product.vo.ProductCategoryTreeVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/product-categories")
@RequiredArgsConstructor
@Validated
public class PmsProductCategoryController {

    private final PmsProductCategoryService categoryService;

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('category:read')")
    public ApiResult<List<ProductCategoryTreeVO>> tree() {
        return ApiResult.success(categoryService.tree());
    }
    @PostMapping
    @PreAuthorize("hasAuthority('category:write')")
    public ApiResult<ProductCategoryTreeVO> create(
            @Valid @RequestBody ProductCategoryCreateDTO dto) {

        return ApiResult.success(
                categoryService.create(dto),
                "商品分类创建成功"
        );
    }
    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('category:write')")
    public ApiResult<ProductCategoryTreeVO> update(
            @PathVariable
            @Positive(message = "分类ID必须大于0") Long categoryId,
            @Valid @RequestBody ProductCategoryUpdateDTO dto) {

        return ApiResult.success(
                categoryService.update(categoryId, dto),
                "商品分类修改成功"
        );
    }
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('category:write')")
    public ApiResult<Void> delete(
            @PathVariable
            @Positive(message = "分类ID必须大于0") Long categoryId) {

        categoryService.delete(categoryId);

        return ApiResult.success(
                null,
                "商品分类删除成功"
        );
    }
    @GetMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('category:read')")
    public ApiResult<ProductCategoryTreeVO> getById(
            @PathVariable
            @Positive(message = "分类ID必须大于0") Long categoryId) {

        return ApiResult.success(
                categoryService.getById(categoryId)
        );
    }
}
