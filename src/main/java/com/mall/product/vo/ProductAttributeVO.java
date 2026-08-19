package com.mall.product.vo;

import com.mall.product.entity.PmsProductAttribute;

public record ProductAttributeVO(
        Long id,
        Long productAttributeCategoryId,
        String name,
        Integer selectType,
        Integer inputType,
        String inputList,
        Integer sort,
        Integer filterType,
        Integer searchType,
        Integer relatedStatus,
        Integer handAddStatus,
        Integer type
) {

    public static ProductAttributeVO from(
            PmsProductAttribute attribute) {

        return new ProductAttributeVO(
                attribute.getId(),
                attribute.getProductAttributeCategoryId(),
                attribute.getName(),
                attribute.getSelectType(),
                attribute.getInputType(),
                attribute.getInputList(),
                attribute.getSort(),
                attribute.getFilterType(),
                attribute.getSearchType(),
                attribute.getRelatedStatus(),
                attribute.getHandAddStatus(),
                attribute.getType()
        );
    }
}