package com.mall.seckill.service;

public interface SeckillReservationRecoveryService {

    int recoverStaleReservations(
            long staleBeforeTimestamp,
            int skuBatchSize,
            int requestBatchSize
    );
}
