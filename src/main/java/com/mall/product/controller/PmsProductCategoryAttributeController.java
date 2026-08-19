package com.mall.product.controller;

import com.mall.common.api.ApiResult;
import com.mall.product.dto.ProductCategoryAttributeUpdateDTO;
import com.mall.product.service.PmsProductCategoryAttributeService;
import com.mall.product.vo.ProductAttributeVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/admin/product-categories/{productCategoryId}/attributes"
)
@RequiredArgsConstructor
@Validated
public class PmsProductCategoryAttributeController {

    private final PmsProductCategoryAttributeService relationService;

    @GetMapping
    @PreAuthorize("hasAuthority('attribute:read')")
    public ApiResult<List<ProductAttributeVO>> list(
            @PathVariable
            @Positive(message = "商品分类ID必须大于0")
            Long productCategoryId) {

        return ApiResult.success(
                relationService.listByCategoryId(productCategoryId)
        );
    }

    @PutMapping
    @PreAuthorize("hasAuthority('attribute:write')")
    public ApiResult<List<ProductAttributeVO>> replace(
            @PathVariable
            @Positive(message = "商品分类ID必须大于0")
            Long productCategoryId,
            @Valid @RequestBody
            ProductCategoryAttributeUpdateDTO dto) {

        return ApiResult.success(
                relationService.replace(
                        productCategoryId,
                        dto.getAttributeIds()
                ),
                "商品分类属性设置成功"
        );
    }
}