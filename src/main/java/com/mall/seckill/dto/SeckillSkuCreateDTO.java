package com.mall.seckill.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuCreateDTO {

    @NotNull(message = "SKU ID不能为空")
    @Positive(message = "SKU ID必须大于0")
    private Long skuId;

    @NotNull(message = "秒杀价格不能为空")
    @DecimalMin(
            value = "0.01",
            message = "秒杀价格必须大于0"
    )
    private BigDecimal seckillPrice;

    @NotNull(message = "秒杀库存不能为空")
    @Min(value = 1, message = "秒杀库存必须大于0")
    private Integer totalStock;

    @Min(
            value = 1,
            message = "每人限购数量必须为1"
    )
    @Max(
            value = 1,
            message = "当前秒杀只支持一人一单"
    )
    private Integer perUserLimit;
}