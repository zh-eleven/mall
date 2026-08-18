package com.mall.product.service;

import com.mall.common.api.PageResult;
import com.mall.product.dto.BrandUpdateDTO;
import com.mall.product.vo.BrandVO;

import java.util.List;
import com.mall.product.dto.BrandCreateDTO;

public interface PmsBrandService {
    BrandVO create(BrandCreateDTO dto);
    PageResult<BrandVO> page(
            String keyword,
            int pageNum,
            int pageSize
    );
    BrandVO update(Long brandId, BrandUpdateDTO dto);
    void delete(Long brandId);
    BrandVO getById(Long brandId);
}