package com.mall.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminOrderQueryDTO {

    @Size(max = 64, message = "订单号长度不能超过64个字符")
    private String orderSn;

    @Positive(message = "会员ID必须大于0")
    private Long memberId;

    @Min(value = 0, message = "订单状态不能小于0")
    @Max(value = 4, message = "订单状态不能大于4")
    private Integer status;

    @Min(value = 1, message = "页码不能小于1")
    private int pageNum = 1;

    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private int pageSize = 10;
}
