package com.mall.seckill.redis;

public record SeckillPendingReservation(
        String requestId,
        Long seckillSkuId,
        Long memberId,
        Long addressId,
        Integer quantity,
        Long requestedAt
) {
}
