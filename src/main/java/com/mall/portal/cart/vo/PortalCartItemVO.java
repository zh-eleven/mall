package com.mall.portal.cart.vo;

import com.mall.order.entity.OmsCartItem;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsSkuStock;

import java.math.BigDecimal;

public record PortalCartItemVO(
        Long id,
        Long productId,
        Long skuId,
        String productName,
        String pic,
        String specData,
        BigDecimal price,
        Integer quantity,
        Integer availableStock,
        boolean selected,
        boolean available,
        BigDecimal subtotal
) {

    public static PortalCartItemVO from(
            OmsCartItem cartItem,
            PmsProduct product,
            PmsSkuStock sku) {

        int availableStock = Math.max(
                sku.getStock() - sku.getLockedStock(),
                0
        );

        String pic = sku.getPic() != null
                && !sku.getPic().isBlank()
                ? sku.getPic()
                : product.getPic();

        boolean available =
                Integer.valueOf(1).equals(
                        product.getPublishStatus()
                )
                        && availableStock >= cartItem.getQuantity();

        BigDecimal subtotal = sku.getPrice().multiply(
                BigDecimal.valueOf(cartItem.getQuantity())
        );

        return new PortalCartItemVO(
                cartItem.getId(),
                product.getId(),
                sku.getId(),
                product.getName(),
                pic,
                sku.getSpecData(),
                sku.getPrice(),
                cartItem.getQuantity(),
                availableStock,
                Integer.valueOf(1).equals(
                        cartItem.getSelected()
                ),
                available,
                subtotal
        );
    }
}