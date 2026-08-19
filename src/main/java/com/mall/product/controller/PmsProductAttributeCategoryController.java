package com.mall.product.controller;

import com.mall.common.api.ApiResult;
import com.mall.common.api.PageResult;
import com.mall.product.dto.ProductAttributeCategoryCreateDTO;
import com.mall.product.dto.ProductAttributeCategoryUpdateDTO;
import com.mall.product.service.PmsProductAttributeCategoryService;
import com.mall.product.vo.ProductAttributeCategoryVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/product-attribute-categories")
@RequiredArgsConstructor
@Validated
public class PmsProductAttributeCategoryController {

    private final PmsProductAttributeCategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('attribute:write')")
    public ApiResult<ProductAttributeCategoryVO> create(
            @Valid @RequestBody
            ProductAttributeCategoryCreateDTO dto) {

        return ApiResult.success(
                categoryService.create(dto),
                "商品属性分类创建成功"
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('attribute:read')")
    public ApiResult<PageResult<ProductAttributeCategoryVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "页码必须大于0")
            int pageNum,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量必须大于0")
            @Max(value = 100, message = "每页数量不能超过100")
            int pageSize) {

        return ApiResult.success(
                categoryService.page(keyword, pageNum, pageSize)
        );
    }

    @GetMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('attribute:read')")
    public ApiResult<ProductAttributeCategoryVO> getById(
            @PathVariable
            @Positive(message = "属性分类ID必须大于0")
            Long categoryId) {

        return ApiResult.success(
                categoryService.getById(categoryId)
        );
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('attribute:write')")
    public ApiResult<ProductAttributeCategoryVO> update(
            @PathVariable
            @Positive(message = "属性分类ID必须大于0")
            Long categoryId,
            @Valid @RequestBody
            ProductAttributeCategoryUpdateDTO dto) {

        return ApiResult.success(
                categoryService.update(categoryId, dto),
                "商品属性分类修改成功"
        );
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('attribute:write')")
    public ApiResult<Void> delete(
            @PathVariable
            @Positive(message = "属性分类ID必须大于0")
            Long categoryId) {

        categoryService.delete(categoryId);

        return ApiResult.success(
                null,
                "商品属性分类删除成功"
        );
    }
}