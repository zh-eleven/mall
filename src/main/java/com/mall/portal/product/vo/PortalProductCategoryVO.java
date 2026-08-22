package com.mall.portal.product.vo;

import com.mall.product.entity.PmsProductCategory;

import java.util.List;

public record PortalProductCategoryVO(
        Long id,
        String name,
        String icon,
        List<PortalProductCategoryVO> children
) {

    public static PortalProductCategoryVO from(
            PmsProductCategory category,
            List<PortalProductCategoryVO> children) {

        return new PortalProductCategoryVO(
                category.getId(),
                category.getName(),
                category.getIcon(),
                List.copyOf(children)
        );
    }
}