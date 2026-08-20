package com.mall.product.controller;

import com.mall.common.api.ApiResult;
import com.mall.common.api.PageResult;
import com.mall.product.dto.ProductCreateDTO;
import com.mall.product.dto.ProductUpdateDTO;
import com.mall.product.service.PmsProductService;
import com.mall.product.vo.ProductVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.mall.product.dto.ProductPublishStatusDTO;
import com.mall.product.vo.ProductDetailVO;


@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Validated
public class PmsProductController {

    private final PmsProductService productService;

    @PostMapping
    @PreAuthorize("hasAuthority('product:write')")
    public ApiResult<ProductVO> create(
            @Valid @RequestBody ProductCreateDTO dto) {

        return ApiResult.success(
                productService.create(dto),
                "商品创建成功"
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('product:read')")
    public ApiResult<PageResult<ProductVO>> page(
            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            @Positive(message = "品牌ID必须大于0")
            Long brandId,

            @RequestParam(required = false)
            @Positive(message = "商品分类ID必须大于0")
            Long categoryId,

            @RequestParam(required = false)
            @Min(value = 0, message = "上架状态只能为0或1")
            @Max(value = 1, message = "上架状态只能为0或1")
            Integer publishStatus,

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
                        publishStatus,
                        pageNum,
                        pageSize
                )
        );
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('product:read')")
    public ApiResult<ProductVO> getById(
            @PathVariable
            @Positive(message = "商品ID必须大于0")
            Long productId) {

        return ApiResult.success(
                productService.getById(productId)
        );
    }

    @PatchMapping("/{productId}")
    @PreAuthorize("hasAuthority('product:write')")
    public ApiResult<ProductVO> update(
            @PathVariable
            @Positive(message = "商品ID必须大于0")
            Long productId,
            @Valid @RequestBody ProductUpdateDTO dto) {

        return ApiResult.success(
                productService.update(productId, dto),
                "商品修改成功"
        );
    }

    @PutMapping("/{productId}/publish-status")
    @PreAuthorize("hasAuthority('product:write')")
    public ApiResult<ProductVO> updatePublishStatus(
            @PathVariable
            @Positive(message = "商品ID必须大于0")
            Long productId,
            @Valid @RequestBody
            ProductPublishStatusDTO dto) {

        return ApiResult.success(
                productService.updatePublishStatus(
                        productId,
                        dto.getPublishStatus()
                ),
                dto.getPublishStatus() == 1
                        ? "商品上架成功"
                        : "商品下架成功"
        );
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('product:write')")
    public ApiResult<Void> delete(
            @PathVariable
            @Positive(message = "商品ID必须大于0")
            Long productId) {

        productService.delete(productId);

        return ApiResult.success(
                null,
                "商品删除成功"
        );
    }

    @GetMapping("/{productId}/detail")
    @PreAuthorize("hasAuthority('product:read')")
    public ApiResult<ProductDetailVO> getDetail(
            @PathVariable
            @Positive(message = "商品ID必须大于0")
            Long productId) {

        return ApiResult.success(
                productService.getDetail(productId)
        );
    }
}