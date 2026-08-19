package com.mall.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class ProductCategoryAttributeUpdateDTO {

    @NotNull(message = "属性ID列表不能为空")
    private List<
            @NotNull(message = "属性ID不能为空")
            @Positive(message = "属性ID必须大于0")
                    Long> attributeIds;
}