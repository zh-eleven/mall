package com.mall.product.service;

import com.mall.common.api.PageResult;
import com.mall.product.dto.ProductCreateDTO;
import com.mall.product.dto.ProductUpdateDTO;
import com.mall.product.vo.ProductVO;

public interface PmsProductService {

    ProductVO create(ProductCreateDTO dto);

    PageResult<ProductVO> page(
            String keyword,
            Long brandId,
            Long categoryId,
            Integer publishStatus,
            int pageNum,
            int pageSize
    );

    ProductVO getById(Long productId);

    ProductVO update(
            Long productId,
            ProductUpdateDTO dto
    );

    void delete(Long productId);
}