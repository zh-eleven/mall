package com.mall.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductAttributeCategoryCreateDTO {

    @NotBlank(message = "属性分类名称不能为空")
    @Size(max = 64, message = "属性分类名称长度不能超过64个字符")
    private String name;
}