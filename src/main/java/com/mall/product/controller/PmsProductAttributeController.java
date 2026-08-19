package com.mall.product.controller;

import com.mall.common.api.ApiResult;
import com.mall.common.api.PageResult;
import com.mall.product.dto.ProductAttributeCreateDTO;
import com.mall.product.dto.ProductAttributeUpdateDTO;
import com.mall.product.service.PmsProductAttributeService;
import com.mall.product.vo.ProductAttributeVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/product-attributes")
@RequiredArgsConstructor
@Validated
public class PmsProductAttributeController {

    private final PmsProductAttributeService attributeService;

    @PostMapping
    @PreAuthorize("hasAuthority('attribute:write')")
    public ApiResult<ProductAttributeVO> create(
            @Valid @RequestBody ProductAttributeCreateDTO dto) {

        return ApiResult.success(
                attributeService.create(dto),
                "商品属性创建成功"
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('attribute:read')")
    public ApiResult<PageResult<ProductAttributeVO>> page(
            @RequestParam(required = false)
            @Positive(message = "属性分类ID必须大于0")
            Long categoryId,

            @RequestParam(required = false)
            @Min(value = 0, message = "属性类型只能为0或1")
            @Max(value = 1, message = "属性类型只能为0或1")
            Integer type,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "页码必须大于0")
            int pageNum,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量必须大于0")
            @Max(value = 100, message = "每页数量不能超过100")
            int pageSize) {

        return ApiResult.success(
                attributeService.page(
                        categoryId,
                        type,
                        keyword,
                        pageNum,
                        pageSize
                )
        );
    }

    @GetMapping("/{attributeId}")
    @PreAuthorize("hasAuthority('attribute:read')")
    public ApiResult<ProductAttributeVO> getById(
            @PathVariable
            @Positive(message = "属性ID必须大于0")
            Long attributeId) {

        return ApiResult.success(
                attributeService.getById(attributeId)
        );
    }

    @PatchMapping("/{attributeId}")
    @PreAuthorize("hasAuthority('attribute:write')")
    public ApiResult<ProductAttributeVO> update(
            @PathVariable
            @Positive(message = "属性ID必须大于0")
            Long attributeId,
            @Valid @RequestBody ProductAttributeUpdateDTO dto) {

        return ApiResult.success(
                attributeService.update(attributeId, dto),
                "商品属性修改成功"
        );
    }

    @DeleteMapping("/{attributeId}")
    @PreAuthorize("hasAuthority('attribute:write')")
    public ApiResult<Void> delete(
            @PathVariable
            @Positive(message = "属性ID必须大于0")
            Long attributeId) {

        attributeService.delete(attributeId);

        return ApiResult.success(
                null,
                "商品属性删除成功"
        );
    }
}