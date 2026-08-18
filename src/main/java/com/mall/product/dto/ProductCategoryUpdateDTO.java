package com.mall.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductCategoryUpdateDTO {

    @Min(value = 0, message = "父分类ID不能小于0")
    private Long parentId;

    @Pattern(regexp = ".*\\S.*", message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称长度不能超过64个字符")
    private String name;

    @Size(max = 16, message = "商品单位长度不能超过16个字符")
    private String productUnit;

    @Min(value = 0, message = "导航状态只能为0或1")
    @Max(value = 1, message = "导航状态只能为0或1")
    private Integer navStatus;

    @Min(value = 0, message = "显示状态只能为0或1")
    @Max(value = 1, message = "显示状态只能为0或1")
    private Integer showStatus;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    @Size(max = 255, message = "分类图标地址长度不能超过255个字符")
    private String icon;

    @Size(max = 255, message = "关键词长度不能超过255个字符")
    private String keywords;

    @Size(max = 500, message = "分类描述长度不能超过500个字符")
    private String description;
}
