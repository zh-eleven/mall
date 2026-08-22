package com.mall.portal.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemSelectedUpdateDTO {

    @NotNull(message = "选中状态不能为空")
    private Boolean selected;
}