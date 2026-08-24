package com.mall.portal.product.vo;

import com.mall.product.entity.PmsSkuStock;

import java.math.BigDecimal;

public record PortalSkuVO(
        Long id,
        BigDecimal price,
        Integer availableStock,
        String pic,
        String specData
) {

    public static PortalSkuVO from(PmsSkuStock sku) {

        int availableStock = Math.max(
                sku.getStock() - sku.getLockedStock(),
                0
        );

        return new PortalSkuVO(
                sku.getId(),
                sku.getPrice(),
                availableStock,
                sku.getPic(),
                sku.getSpecData()
        );
    }

    public static PortalSkuVO from(
            PortalSkuCacheVO sku,
            int stock,
            int lockedStock) {

        int availableStock = Math.max(
                stock - lockedStock,
                0
        );

        return new PortalSkuVO(
                sku.id(),
                sku.price(),
                availableStock,
                sku.pic(),
                sku.specData()
        );
    }
}
