package com.mall.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProductAttributeValueUpdateDTO {

    @NotNull(message = "商品属性值列表不能为空")
    @Size(max = 100, message = "商品属性值不能超过100项")
    private List<@Valid ProductAttributeValueItemDTO> values;
}