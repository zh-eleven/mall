package com.mall.seckill.vo;

public record SeckillOrderStatusVO(
        String requestId,
        String status,
        Long orderId
) {

    public static SeckillOrderStatusVO queued(
            String requestId) {

        return new SeckillOrderStatusVO(
                requestId,
                "QUEUED",
                null
        );
    }

    public static SeckillOrderStatusVO created(
            String requestId,
            Long orderId) {

        return new SeckillOrderStatusVO(
                requestId,
                "CREATED",
                orderId
        );
    }

    public static SeckillOrderStatusVO compensating(
            String requestId) {

        return new SeckillOrderStatusVO(
                requestId,
                "COMPENSATING",
                null
        );
    }

    public static SeckillOrderStatusVO failed(
            String requestId) {

        return new SeckillOrderStatusVO(
                requestId,
                "FAILED",
                null
        );
    }

    public static SeckillOrderStatusVO unknown(
            String requestId) {

        return new SeckillOrderStatusVO(
                requestId,
                "UNKNOWN",
                null
        );
    }


}