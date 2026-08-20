package com.mall.product.controller;

import com.mall.common.api.ApiResult;
import com.mall.product.dto.SkuStockUpdateDTO;
import com.mall.product.service.PmsSkuStockService;
import com.mall.product.vo.SkuStockVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products/{productId}/skus")
@RequiredArgsConstructor
@Validated
public class PmsSkuStockController {

    private final PmsSkuStockService skuStockService;

    @GetMapping
    @PreAuthorize("hasAuthority('product:read')")
    public ApiResult<List<SkuStockVO>> list(
            @PathVariable
            @Positive(message = "商品ID必须大于0")
            Long productId) {

        return ApiResult.success(
                skuStockService.listByProductId(productId)
        );
    }

    @PutMapping
    @PreAuthorize("hasAuthority('product:write')")
    public ApiResult<List<SkuStockVO>> replace(
            @PathVariable
            @Positive(message = "商品ID必须大于0")
            Long productId,
            @Valid @RequestBody
            SkuStockUpdateDTO dto) {

        return ApiResult.success(
                skuStockService.replace(
                        productId,
                        dto.getSkus()
                ),
                "商品SKU设置成功"
        );
    }
}