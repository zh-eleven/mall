package com.mall.product.service;

import com.mall.product.dto.ProductCategoryCreateDTO;
import com.mall.product.dto.ProductCategoryUpdateDTO;
import com.mall.product.vo.ProductCategoryTreeVO;

import java.util.List;

public interface PmsProductCategoryService {

    List<ProductCategoryTreeVO> tree();
    ProductCategoryTreeVO create(ProductCategoryCreateDTO dto);
    ProductCategoryTreeVO update(
            Long categoryId,
            ProductCategoryUpdateDTO dto
    );
    void delete(Long categoryId);
    ProductCategoryTreeVO getById(Long categoryId);
}