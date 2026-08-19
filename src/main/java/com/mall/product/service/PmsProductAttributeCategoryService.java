package com.mall.product.service;

import com.mall.common.api.PageResult;
import com.mall.product.dto.ProductAttributeCategoryCreateDTO;
import com.mall.product.dto.ProductAttributeCategoryUpdateDTO;
import com.mall.product.vo.ProductAttributeCategoryVO;

public interface PmsProductAttributeCategoryService {

    ProductAttributeCategoryVO create(
            ProductAttributeCategoryCreateDTO dto
    );

    PageResult<ProductAttributeCategoryVO> page(
            String keyword,
            int pageNum,
            int pageSize
    );

    ProductAttributeCategoryVO getById(Long categoryId);

    ProductAttributeCategoryVO update(
            Long categoryId,
            ProductAttributeCategoryUpdateDTO dto
    );

    void delete(Long categoryId);
}