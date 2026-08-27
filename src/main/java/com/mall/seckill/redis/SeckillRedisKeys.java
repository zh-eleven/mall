package com.mall.seckill.redis;

public final class SeckillRedisKeys {

    private static final String PREFIX =
            "mall:v1:seckill:";

    private SeckillRedisKeys() {
    }

    private static String slot(Long seckillSkuId) {
        return PREFIX + "{" + seckillSkuId + "}:";
    }

    public static String sku(Long seckillSkuId) {
        return slot(seckillSkuId) + "sku";
    }

    public static String users(Long seckillSkuId) {
        return slot(seckillSkuId) + "users";
    }

    public static String reservations(Long seckillSkuId) {
        return slot(seckillSkuId) + "reservations";
    }

    public static String pending(Long seckillSkuId) {
        return slot(seckillSkuId) + "pending";
    }
}
