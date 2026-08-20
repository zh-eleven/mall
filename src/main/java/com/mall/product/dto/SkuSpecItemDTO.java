package com.mall.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SkuSpecItemDTO {

    @NotNull(message = "规格属性ID不能为空")
    @Positive(message = "规格属性ID必须大于0")
    private Long productAttributeId;

    @NotBlank(message = "规格值不能为空")
    @Size(max = 255, message = "规格值长度不能超过255个字符")
    private String value;
}