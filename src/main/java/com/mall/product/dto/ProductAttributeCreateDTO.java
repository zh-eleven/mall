package com.mall.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductAttributeCreateDTO {

    @NotNull(message = "属性分类ID不能为空")
    @Positive(message = "属性分类ID必须大于0")
    private Long productAttributeCategoryId;

    @NotBlank(message = "属性名称不能为空")
    @Size(max = 64, message = "属性名称长度不能超过64个字符")
    private String name;

    @Min(value = 0, message = "选择类型只能为0至2")
    @Max(value = 2, message = "选择类型只能为0至2")
    private Integer selectType;

    @Min(value = 0, message = "录入方式只能为0或1")
    @Max(value = 1, message = "录入方式只能为0或1")
    private Integer inputType;

    @Size(max = 500, message = "可选值长度不能超过500个字符")
    private String inputList;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    @Min(value = 0, message = "筛选类型只能为0或1")
    @Max(value = 1, message = "筛选类型只能为0或1")
    private Integer filterType;

    @Min(value = 0, message = "搜索类型只能为0至2")
    @Max(value = 2, message = "搜索类型只能为0至2")
    private Integer searchType;

    @Min(value = 0, message = "关联状态只能为0或1")
    @Max(value = 1, message = "关联状态只能为0或1")
    private Integer relatedStatus;

    @Min(value = 0, message = "手工新增状态只能为0或1")
    @Max(value = 1, message = "手工新增状态只能为0或1")
    private Integer handAddStatus;

    @NotNull(message = "属性类型不能为空")
    @Min(value = 0, message = "属性类型只能为0或1")
    @Max(value = 1, message = "属性类型只能为0或1")
    private Integer type;
}