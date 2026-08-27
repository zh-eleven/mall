package com.mall.seckill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SeckillSubmitDTO {

    @NotBlank(message = "请求ID不能为空")
    @Pattern(
            regexp =
                    "^[0-9a-fA-F]{8}-"
                            + "[0-9a-fA-F]{4}-"
                            + "[1-5][0-9a-fA-F]{3}-"
                            + "[89abAB][0-9a-fA-F]{3}-"
                            + "[0-9a-fA-F]{12}$",
            message = "请求ID必须是有效的UUID"
    )
    private String requestId;

    @NotNull(message = "收货地址ID不能为空")
    @Positive(message = "收货地址ID必须大于0")
    private Long addressId;
}