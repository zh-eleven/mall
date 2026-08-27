package com.mall.seckill.mq;

public record SeckillOrderMessage(
        String requestId,
        Long seckillSkuId,
        Long memberId,
        Long addressId,
        Integer quantity,
        Long requestedAt
) {
}