package com.mall.seckill.service;

import java.time.LocalDateTime;
import java.util.List;

public interface SeckillActivityFinalizationService {

    List<Long> findExpiredActivityIds(
            LocalDateTime cutoffTime,
            int batchSize
    );

    boolean finalizeExpiredActivity(
            Long activityId,
            LocalDateTime now
    );
}
