package com.mall.portal.product.controller;

import com.mall.common.api.ApiResult;
import com.mall.portal.product.service.PortalProductService;
import com.mall.portal.product.vo.PortalProductCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
@RequiredArgsConstructor
public class PortalProductCategoryController {

    private final PortalProductService productService;

    @GetMapping("/tree")
    public ApiResult<List<PortalProductCategoryVO>>
    getCategoryTree() {

        return ApiResult.success(
                productService.getCategoryTree()
        );
    }
}