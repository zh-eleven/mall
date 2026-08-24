package com.mall.portal.product.vo;

import com.mall.product.entity.PmsSkuStock;

import java.math.BigDecimal;

public record PortalSkuCacheVO(
        Long id,
        BigDecimal price,
        String pic,
        String specData
) {

    public static PortalSkuCacheVO from(PmsSkuStock sku) {
        return new PortalSkuCacheVO(
                sku.getId(),
                sku.getPrice(),
                sku.getPic(),
                sku.getSpecData()
        );
    }
}