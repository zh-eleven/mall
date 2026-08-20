package com.mall.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuStockItemDTO {

    @NotBlank(message = "SKU编码不能为空")
    @Size(max = 64, message = "SKU编码长度不能超过64个字符")
    @Pattern(
            regexp = "^\\s*[A-Za-z0-9_-]+\\s*$",
            message = "SKU编码只能包含字母、数字、下划线和横线"
    )
    private String skuCode;

    @NotNull(message = "SKU价格不能为空")
    @DecimalMin(value = "0.00", message = "SKU价格不能小于0")
    @Digits(
            integer = 8,
            fraction = 2,
            message = "SKU价格最多8位整数和2位小数"
    )
    private BigDecimal price;

    @NotNull(message = "SKU库存不能为空")
    @Min(value = 0, message = "SKU库存不能小于0")
    private Integer stock;

    @Min(value = 0, message = "库存预警值不能小于0")
    private Integer lowStock;

    @Size(max = 255, message = "SKU图片地址长度不能超过255个字符")
    private String pic;

    @NotNull(message = "SKU规格列表不能为空")
    @Size(max = 20, message = "SKU规格不能超过20项")
    private List<@Valid SkuSpecItemDTO> specs;
}