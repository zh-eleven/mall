package com.mall.seckill.service;

import com.mall.seckill.mq.SeckillOrderMessage;

public interface SeckillFailureCompensationService {

    void recordAndCompensate(
            SeckillOrderMessage message,
            String failureReason
    );

    boolean compensate(Long failureId);

    int retryPending(int batchSize);
}