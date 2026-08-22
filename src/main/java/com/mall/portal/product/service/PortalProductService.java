package com.mall.portal.product.service;

import com.mall.common.api.PageResult;
import com.mall.portal.product.vo.PortalProductCategoryVO;
import com.mall.portal.product.vo.PortalProductDetailVO;
import com.mall.portal.product.vo.PortalProductSummaryVO;

import java.util.List;

public interface PortalProductService {

    PageResult<PortalProductSummaryVO> page(
            String keyword,
            Long brandId,
            Long categoryId,
            int pageNum,
            int pageSize
    );

    PortalProductDetailVO getDetail(Long productId);


    List<PortalProductCategoryVO> getCategoryTree();
}