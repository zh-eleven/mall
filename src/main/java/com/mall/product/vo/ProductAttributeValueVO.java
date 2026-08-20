package com.mall.product.vo;

import com.mall.product.entity.PmsProductAttributeValue;
import lombok.Data;

@Data
public class ProductAttributeValueVO {

    private Long id;
    private Long productId;
    private Long productAttributeId;
    private String value;

    public static ProductAttributeValueVO from(
            PmsProductAttributeValue entity) {

        ProductAttributeValueVO vo =
                new ProductAttributeValueVO();

        vo.setId(entity.getId());
        vo.setProductId(entity.getProductId());
        vo.setProductAttributeId(
                entity.getProductAttributeId()
        );
        vo.setValue(entity.getValue());

        return vo;
    }
}