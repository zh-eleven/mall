package com.mall.order.vo;

import com.mall.order.entity.OmsOrderItem;

import java.math.BigDecimal;

public record AdminOrderItemVO(
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

    public static AdminOrderItemVO from(
            OmsOrderItem item) {

        return new AdminOrderItemVO(
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
