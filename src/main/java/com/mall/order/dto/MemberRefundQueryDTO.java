package com.mall.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class MemberRefundQueryDTO {

    @Min(value = 0, message = "退款状态不能小于0")
    @Max(value = 2, message = "退款状态不能大于2")
    private Integer status;

    @Min(value = 1, message = "页码不能小于1")
    private int pageNum = 1;

    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private int pageSize = 10;
}
