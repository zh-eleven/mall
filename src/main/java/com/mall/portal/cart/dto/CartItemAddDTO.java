package com.mall.portal.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartItemAddDTO {

    @NotNull(message = "SKU ID不能为空")
    @Positive(message = "SKU ID必须大于0")
    private Long skuId;

    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量不能小于1")
    @Max(value = 999, message = "商品数量不能超过999")
    private Integer quantity;
}