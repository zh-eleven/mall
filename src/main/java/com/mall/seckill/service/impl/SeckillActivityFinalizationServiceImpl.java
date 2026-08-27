package com.mall.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.seckill.entity.SmsSeckillActivity;
import com.mall.seckill.entity.SmsSeckillSku;
import com.mall.seckill.enums.SeckillActivityStatus;
import com.mall.seckill.mapper.SmsSeckillActivityMapper;
import com.mall.seckill.mapper.SmsSeckillSkuMapper;
import com.mall.seckill.redis.SeckillRedisService;
import com.mall.seckill.service.SeckillActivityFinalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillActivityFinalizationServiceImpl
        implements SeckillActivityFinalizationService {

    private final SmsSeckillActivityMapper activityMapper;
    private final SmsSeckillSkuMapper seckillSkuMapper;
    private final PmsSkuStockMapper skuStockMapper;
    private final SeckillRedisService seckillRedisService;

    @Override
    public List<Long> findExpiredActivityIds(
            LocalDateTime cutoffTime,
            int batchSize) {

        return activityMapper.selectPage(
                        new Page<>(1, batchSize, false),
                        new LambdaQueryWrapper<SmsSeckillActivity>()
                                .eq(
                                        SmsSeckillActivity::getStatus,
                                        SeckillActivityStatus.ENABLED
                                                .getCode()
                                )
                                .le(
                                        SmsSeckillActivity::getEndTime,
                                        cutoffTime
                                )
                                .orderByAsc(
                                        SmsSeckillActivity::getId
                                )
                )
                .getRecords()
                .stream()
                .map(SmsSeckillActivity::getId)
                .toList();
    }

    @Override
    @Transactional
    public boolean finalizeExpiredActivity(
            Long activityId,
            LocalDateTime now) {

        SmsSeckillActivity activity =
                activityMapper.selectByIdForUpdate(activityId);

        if (activity == null
                || !Integer.valueOf(
                        SeckillActivityStatus.ENABLED.getCode()
                ).equals(activity.getStatus())
                || activity.getEndTime().isAfter(now)) {
            return false;
        }

        List<SmsSeckillSku> seckillSkus =
                seckillSkuMapper.selectList(
                        new LambdaQueryWrapper<SmsSeckillSku>()
                                .eq(
                                        SmsSeckillSku::getActivityId,
                                        activityId
                                )
                                .orderByAsc(
                                        SmsSeckillSku::getSkuId
                                )
                );

        for (SmsSeckillSku seckillSku : seckillSkus) {
            int remaining = seckillSku.getAvailableStock();

            if (remaining == 0) {
                continue;
            }

            int released = skuStockMapper.releaseLockedStock(
                    seckillSku.getSkuId(),
                    remaining
            );

            if (released != 1) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT
                );
            }
        }

        int updated = activityMapper.update(
                null,
                new LambdaUpdateWrapper<SmsSeckillActivity>()
                        .eq(
                                SmsSeckillActivity::getId,
                                activityId
                        )
                        .eq(
                                SmsSeckillActivity::getStatus,
                                SeckillActivityStatus.ENABLED
                                        .getCode()
                        )
                        .set(
                                SmsSeckillActivity::getStatus,
                                SeckillActivityStatus.ENDED
                                        .getCode()
                        )
        );

        if (updated != 1) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        removeRedisAfterCommit(List.copyOf(seckillSkus));
        return true;
    }

    private void removeRedisAfterCommit(
            List<SmsSeckillSku> seckillSkus) {

        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                try {
                                    seckillRedisService.remove(
                                            seckillSkus
                                    );
                                } catch (RuntimeException exception) {
                                    log.warn(
                                            "秒杀活动已收尾但Redis清理失败，"
                                                    + "等待TTL自动清理",
                                            exception
                                    );
                                }
                            }
                        }
                );
    }
}
