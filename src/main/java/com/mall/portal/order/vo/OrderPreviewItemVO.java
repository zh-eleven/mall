package com.mall.portal.order.vo;

import com.mall.order.entity.OmsCartItem;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsSkuStock;

import java.math.BigDecimal;

public record OrderPreviewItemVO(
        Long cartItemId,
        Long productId,
        Long skuId,
        String skuCode,
        String productName,
        String pic,
        String specData,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal
) {

    public static OrderPreviewItemVO from(
            OmsCartItem cartItem,
            PmsProduct product,
            PmsSkuStock sku) {

        String pic = sku.getPic() != null
                && !sku.getPic().isBlank()
                ? sku.getPic()
                : product.getPic();

        BigDecimal subtotal = sku.getPrice().multiply(
                BigDecimal.valueOf(cartItem.getQuantity())
        );

        return new OrderPreviewItemVO(
                cartItem.getId(),
                product.getId(),
                sku.getId(),
                sku.getSkuCode(),
                product.getName(),
                pic,
                sku.getSpecData(),
                sku.getPrice(),
                cartItem.getQuantity(),
                subtotal
        );
    }
}