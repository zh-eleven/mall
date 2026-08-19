package com.mall.product.service;

import com.mall.product.vo.ProductAttributeVO;

import java.util.List;

public interface PmsProductCategoryAttributeService {

    List<ProductAttributeVO> listByCategoryId(
            Long productCategoryId
    );

    List<ProductAttributeVO> replace(
            Long productCategoryId,
            List<Long> attributeIds
    );
}