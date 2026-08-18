package com.mall.product.vo;

import com.mall.product.entity.PmsProductCategory;

import java.util.List;

public record ProductCategoryTreeVO(
        Long id,
        Long parentId,
        String name,
        Integer level,
        Integer productCount,
        String productUnit,
        Integer navStatus,
        Integer showStatus,
        Integer sort,
        String icon,
        String keywords,
        String description,
        List<ProductCategoryTreeVO> children
) {

    public static ProductCategoryTreeVO from(
            PmsProductCategory category,
            List<ProductCategoryTreeVO> children) {

        return new ProductCategoryTreeVO(
                category.getId(),
                category.getParentId(),
                category.getName(),
                category.getLevel(),
                category.getProductCount(),
                category.getProductUnit(),
                category.getNavStatus(),
                category.getShowStatus(),
                category.getSort(),
                category.getIcon(),
                category.getKeywords(),
                category.getDescription(),
                children
        );
    }
}