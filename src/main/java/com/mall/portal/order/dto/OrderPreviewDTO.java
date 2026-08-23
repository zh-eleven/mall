package com.mall.portal.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderPreviewDTO {

    @NotNull(message = "收货地址ID不能为空")
    @Positive(message = "收货地址ID必须大于0")
    private Long addressId;
}