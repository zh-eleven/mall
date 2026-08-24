package com.mall.portal.product.service;

import com.mall.portal.product.vo.PortalProductDetailCacheVO;

public interface PortalProductDetailCacheService {

    PortalProductDetailCacheVO getStaticDetail(Long productId);
}