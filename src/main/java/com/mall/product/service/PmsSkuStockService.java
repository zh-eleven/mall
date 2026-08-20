package com.mall.product.service;

import com.mall.product.dto.SkuStockItemDTO;
import com.mall.product.vo.SkuStockVO;

import java.util.List;

public interface PmsSkuStockService {

    /**
     * 查询商品的全部 SKU。
     */
    List<SkuStockVO> listByProductId(
            Long productId
    );

    /**
     * 整体替换商品 SKU。
     *
     * 只允许未上架商品执行；
     * 空列表表示清空全部 SKU。
     */
    List<SkuStockVO> replace(
            Long productId,
            List<SkuStockItemDTO> skus
    );
}