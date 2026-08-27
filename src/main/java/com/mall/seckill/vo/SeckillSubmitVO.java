package com.mall.seckill.vo;

public record SeckillSubmitVO(
        String requestId,
        String status
) {

    public static SeckillSubmitVO queued(
            String requestId) {

        return new SeckillSubmitVO(
                requestId,
                "QUEUED"
        );
    }
}