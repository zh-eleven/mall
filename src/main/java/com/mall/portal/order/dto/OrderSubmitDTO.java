package com.mall.portal.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderSubmitDTO {

    @NotNull(message = "收货地址ID不能为空")
    @Positive(message = "收货地址ID必须大于0")
    private Long addressId;

    @Size(
            max = 500,
            message = "订单备注不能超过500个字符"
    )
    private String note;
}