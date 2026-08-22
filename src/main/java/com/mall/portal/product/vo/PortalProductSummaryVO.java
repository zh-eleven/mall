package com.mall.portal.product.vo;

import com.mall.product.entity.PmsProduct;

import java.math.BigDecimal;

public record PortalProductSummaryVO(
        Long id,
        Long brandId,
        Long productCategoryId,
        String name,
        String subTitle,
        BigDecimal price,
        BigDecimal originalPrice,
        String pic
) {

    public static PortalProductSummaryVO from(
            PmsProduct product) {

        return new PortalProductSummaryVO(
                product.getId(),
                product.getBrandId(),
                product.getProductCategoryId(),
                product.getName(),
                product.getSubTitle(),
                product.getPrice(),
                product.getOriginalPrice(),
                product.getPic()
        );
    }
}