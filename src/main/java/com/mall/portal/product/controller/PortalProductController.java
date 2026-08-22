package com.mall.portal.product.controller;

import com.mall.common.api.ApiResult;
import com.mall.common.api.PageResult;
import com.mall.portal.product.service.PortalProductService;
import com.mall.portal.product.vo.PortalProductDetailVO;
import com.mall.portal.product.vo.PortalProductSummaryVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class PortalProductController {

    private final PortalProductService productService;

    @GetMapping
    public ApiResult<PageResult<PortalProductSummaryVO>> page(
            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            @Positive(message = "品牌ID必须大于0")
            Long brandId,

            @RequestParam(required = false)
            @Positive(message = "商品分类ID必须大于0")
            Long categoryId,

            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "页码必须大于0")
            int pageNum,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量必须大于0")
            @Max(value = 100, message = "每页数量不能超过100")
            int pageSize) {

        return ApiResult.success(
                productService.page(
                        keyword,
                        brandId,
                        categoryId,
                        pageNum,
                        pageSize
                )
        );
    }

    @GetMapping("/{productId}")
    public ApiResult<PortalProductDetailVO> getDetail(
            @PathVariable
            @Positive(message = "商品ID必须大于0")
            Long productId) {

        return ApiResult.success(
                productService.getDetail(productId)
        );
    }
}