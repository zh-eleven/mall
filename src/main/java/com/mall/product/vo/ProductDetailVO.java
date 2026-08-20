package com.mall.product.vo;

import java.util.List;

public record ProductDetailVO(
        ProductVO product,
        List<ProductAttributeValueVO> attributeValues,
        List<SkuStockVO> skus
) {
}