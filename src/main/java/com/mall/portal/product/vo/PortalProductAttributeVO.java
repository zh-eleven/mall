package com.mall.portal.product.vo;

import com.mall.product.entity.PmsProductAttribute;
import com.mall.product.entity.PmsProductAttributeValue;

public record PortalProductAttributeVO(
        Long attributeId,
        String name,
        String value
) {

    public static PortalProductAttributeVO from(
            PmsProductAttribute attribute,
            PmsProductAttributeValue attributeValue) {

        return new PortalProductAttributeVO(
                attribute.getId(),
                attribute.getName(),
                attributeValue.getValue()
        );
    }
}