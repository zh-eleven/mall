package com.mall.product.service;

import com.mall.product.dto.ProductAttributeValueItemDTO;
import com.mall.product.vo.ProductAttributeValueVO;

import java.util.List;

public interface PmsProductAttributeValueService {

    /**
     * 查询某个商品的全部属性值。
     */
    List<ProductAttributeValueVO> listByProductId(
            Long productId
    );

    /**
     * 整体替换某个商品的属性值。
     * 传入空列表表示清空全部属性值。
     */
    List<ProductAttributeValueVO> replace(
            Long productId,
            List<ProductAttributeValueItemDTO> values
    );
}