package com.mall.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderShipDTO {

    @NotBlank(message = "物流公司不能为空")
    @Size(max = 64, message = "物流公司长度不能超过64个字符")
    private String deliveryCompany;

    @NotBlank(message = "物流单号不能为空")
    @Size(max = 64, message = "物流单号长度不能超过64个字符")
    private String deliverySn;
}
