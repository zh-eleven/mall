package com.mall.portal.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class OrderSubmitDTO {

    @NotBlank(message = "下单令牌不能为空")
    @Size(
            min = 16,
            max = 64,
            message = "下单令牌长度必须在16到64个字符之间"
    )
    private String submitToken;

    @NotNull(message = "收货地址ID不能为空")
    @Positive(message = "收货地址ID必须大于0")
    private Long addressId;

    @Size(
            max = 500,
            message = "订单备注不能超过500个字符"
    )
    private String note;
}