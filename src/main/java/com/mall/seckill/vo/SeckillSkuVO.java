package com.mall.seckill.vo;

import com.mall.seckill.entity.SmsSeckillSku;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SeckillSkuVO(
        Long id,
        Long activityId,
        Long productId,
        Long skuId,
        BigDecimal seckillPrice,
        Integer totalStock,
        Integer availableStock,
        Integer perUserLimit,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {

    public static SeckillSkuVO from(
            SmsSeckillSku seckillSku) {

        return new SeckillSkuVO(
                seckillSku.getId(),
                seckillSku.getActivityId(),
                seckillSku.getProductId(),
                seckillSku.getSkuId(),
                seckillSku.getSeckillPrice(),
                seckillSku.getTotalStock(),
                seckillSku.getAvailableStock(),
                seckillSku.getPerUserLimit(),
                seckillSku.getCreateTime(),
                seckillSku.getUpdateTime()
        );
    }
}