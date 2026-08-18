package com.mall.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BrandUpdateDTO {

    @Pattern(
            regexp = ".*\\S.*",
            message = "品牌名称不能为空"
    )
    @Size(max = 64, message = "品牌名称长度不能超过64个字符")
    private String name;

    @Pattern(
            regexp = "^[A-Za-z]$",
            message = "品牌首字母必须是单个英文字母"
    )
    private String firstLetter;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    @Min(value = 0, message = "厂家状态只能为0或1")
    @Max(value = 1, message = "厂家状态只能为0或1")
    private Integer factoryStatus;

    @Min(value = 0, message = "显示状态只能为0或1")
    @Max(value = 1, message = "显示状态只能为0或1")
    private Integer showStatus;

    @Size(max = 255, message = "品牌Logo地址长度不能超过255个字符")
    private String logo;

    @Size(max = 255, message = "品牌专区图片地址长度不能超过255个字符")
    private String bigPic;

    @Size(max = 2000, message = "品牌故事长度不能超过2000个字符")
    private String brandStory;
}
