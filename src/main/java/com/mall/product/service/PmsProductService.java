package com.mall.product.service;

import com.mall.common.api.PageResult;
import com.mall.product.dto.ProductCreateDTO;
import com.mall.product.dto.ProductUpdateDTO;
import com.mall.product.vo.ProductVO;
import com.mall.product.vo.ProductDetailVO;

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

    ProductDetailVO getDetail(Long productId);

    ProductVO update(
            Long productId,
            ProductUpdateDTO dto
    );

    ProductVO updatePublishStatus(
            Long productId,
            Integer publishStatus
    );

    void delete(Long productId);
}