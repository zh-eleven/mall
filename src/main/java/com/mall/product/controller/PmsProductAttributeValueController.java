package com.mall.product.controller;

import com.mall.common.api.ApiResult;
import com.mall.product.dto.ProductAttributeValueUpdateDTO;
import com.mall.product.service.PmsProductAttributeValueService;
import com.mall.product.vo.ProductAttributeValueVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/admin/products/{productId}/attribute-values"
)
@RequiredArgsConstructor
@Validated
public class PmsProductAttributeValueController {

    private final PmsProductAttributeValueService attributeValueService;

    @GetMapping
    @PreAuthorize("hasAuthority('product:read')")
    public ApiResult<List<ProductAttributeValueVO>> list(
            @PathVariable
            @Positive(message = "商品ID必须大于0")
            Long productId) {

        return ApiResult.success(
                attributeValueService.listByProductId(productId)
        );
    }

    @PutMapping
    @PreAuthorize("hasAuthority('product:write')")
    public ApiResult<List<ProductAttributeValueVO>> replace(
            @PathVariable
            @Positive(message = "商品ID必须大于0")
            Long productId,
            @Valid @RequestBody
            ProductAttributeValueUpdateDTO dto) {

        return ApiResult.success(
                attributeValueService.replace(
                        productId,
                        dto.getValues()
                ),
                "商品属性值设置成功"
        );
    }
}