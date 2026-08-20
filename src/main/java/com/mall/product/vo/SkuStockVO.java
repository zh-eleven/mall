package com.mall.product.vo;

import com.mall.product.entity.PmsSkuStock;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuStockVO {

    private Long id;
    private Long productId;
    private String skuCode;
    private BigDecimal price;
    private Integer stock;
    private Integer lockedStock;
    private Integer availableStock;
    private Integer lowStock;
    private String pic;
    private String specKey;
    private String specData;

    public static SkuStockVO from(PmsSkuStock entity) {
        SkuStockVO vo = new SkuStockVO();

        vo.setId(entity.getId());
        vo.setProductId(entity.getProductId());
        vo.setSkuCode(entity.getSkuCode());
        vo.setPrice(entity.getPrice());
        vo.setStock(entity.getStock());
        vo.setLockedStock(entity.getLockedStock());
        vo.setAvailableStock(
                entity.getStock() - entity.getLockedStock()
        );
        vo.setLowStock(entity.getLowStock());
        vo.setPic(entity.getPic());
        vo.setSpecKey(entity.getSpecKey());
        vo.setSpecData(entity.getSpecData());

        return vo;
    }
}