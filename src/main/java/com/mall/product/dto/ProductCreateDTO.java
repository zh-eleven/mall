package com.mall.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateDTO {

    @Positive(message = "品牌ID必须大于0")
    private Long brandId;

    @NotNull(message = "商品分类ID不能为空")
    @Positive(message = "商品分类ID必须大于0")
    private Long productCategoryId;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称长度不能超过200个字符")
    private String name;

    @Size(max = 255, message = "副标题长度不能超过255个字符")
    private String subTitle;

    @NotBlank(message = "商品货号不能为空")
    @Size(max = 64, message = "商品货号长度不能超过64个字符")
    @Pattern(
            regexp = "^\\s*[A-Za-z0-9_-]+\\s*$",
            message = "商品货号只能包含字母、数字、下划线和横线"
    )
    private String productSn;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.00", message = "商品价格不能小于0")
    @Digits(
            integer = 8,
            fraction = 2,
            message = "商品价格最多8位整数和2位小数"
    )
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "市场价格不能小于0")
    @Digits(
            integer = 8,
            fraction = 2,
            message = "市场价格最多8位整数和2位小数"
    )
    private BigDecimal originalPrice;

    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;

    @Min(value = 0, message = "库存预警值不能小于0")
    private Integer lowStock;

    @Size(max = 16, message = "商品单位长度不能超过16个字符")
    private String unit;

    @DecimalMin(value = "0.00", message = "商品重量不能小于0")
    @Digits(
            integer = 8,
            fraction = 2,
            message = "商品重量最多8位整数和2位小数"
    )
    private BigDecimal weight;


    @Min(value = 0, message = "新品状态只能为0或1")
    @Max(value = 1, message = "新品状态只能为0或1")
    private Integer newStatus;

    @Min(value = 0, message = "推荐状态只能为0或1")
    @Max(value = 1, message = "推荐状态只能为0或1")
    private Integer recommendStatus;

    @Min(value = 0, message = "审核状态只能为0或1")
    @Max(value = 1, message = "审核状态只能为0或1")
    private Integer verifyStatus;

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    @Size(max = 255, message = "商品主图地址长度不能超过255个字符")
    private String pic;

    @Size(max = 2000, message = "商品相册内容不能超过2000个字符")
    private String albumPics;

    @Size(max = 10000, message = "商品描述不能超过10000个字符")
    private String description;

    @Size(max = 255, message = "详情标题不能超过255个字符")
    private String detailTitle;

    @Size(max = 1000, message = "详情描述不能超过1000个字符")
    private String detailDesc;

    @Size(max = 1000000, message = "商品详情内容过长")
    private String detailHtml;
}
