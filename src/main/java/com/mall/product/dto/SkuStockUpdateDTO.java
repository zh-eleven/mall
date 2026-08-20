package com.mall.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SkuStockUpdateDTO {

    @NotNull(message = "SKU列表不能为空")
    @Size(max = 200, message = "单个商品的SKU不能超过200个")
    private List<@Valid SkuStockItemDTO> skus;
}