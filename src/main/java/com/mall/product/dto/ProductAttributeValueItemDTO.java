package com.mall.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductAttributeValueItemDTO {

    @NotNull(message = "商品属性ID不能为空")
    @Positive(message = "商品属性ID必须大于0")
    private Long productAttributeId;

    @NotBlank(message = "商品属性值不能为空")
    @Size(max = 1000, message = "商品属性值长度不能超过1000个字符")
    private String value;
}