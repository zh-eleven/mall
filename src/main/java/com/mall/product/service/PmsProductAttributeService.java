package com.mall.product.service;

import com.mall.common.api.PageResult;
import com.mall.product.dto.ProductAttributeCreateDTO;
import com.mall.product.dto.ProductAttributeUpdateDTO;
import com.mall.product.vo.ProductAttributeVO;

public interface PmsProductAttributeService {

    ProductAttributeVO create(ProductAttributeCreateDTO dto);

    PageResult<ProductAttributeVO> page(
            Long categoryId,
            Integer type,
            String keyword,
            int pageNum,
            int pageSize
    );

    ProductAttributeVO getById(Long attributeId);

    ProductAttributeVO update(
            Long attributeId,
            ProductAttributeUpdateDTO dto
    );

    void delete(Long attributeId);
}