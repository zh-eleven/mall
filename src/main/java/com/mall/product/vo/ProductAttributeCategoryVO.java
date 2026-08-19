package com.mall.product.vo;

import com.mall.product.entity.PmsProductAttributeCategory;

public record ProductAttributeCategoryVO(
        Long id,
        String name,
        Integer attributeCount,
        Integer paramCount
) {

    public static ProductAttributeCategoryVO from(
            PmsProductAttributeCategory category) {

        return new ProductAttributeCategoryVO(
                category.getId(),
                category.getName(),
                category.getAttributeCount(),
                category.getParamCount()
        );
    }
}