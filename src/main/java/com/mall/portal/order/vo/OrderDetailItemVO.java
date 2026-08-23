package com.mall.portal.order.vo;

import com.mall.order.entity.OmsOrderItem;

import java.math.BigDecimal;

public record OrderDetailItemVO(
        Long id,
        Long productId,
        Long skuId,
        String skuCode,
        String productName,
        String productPic,
        String specData,
        BigDecimal productPrice,
        Integer quantity,
        BigDecimal subtotal
) {

    public static OrderDetailItemVO from(
            OmsOrderItem item) {

        return new OrderDetailItemVO(
                item.getId(),
                item.getProductId(),
                item.getSkuId(),
                item.getSkuCode(),
                item.getProductName(),
                item.getProductPic(),
                item.getSpecData(),
                item.getProductPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}