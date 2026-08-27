package com.mall.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.seckill.entity.OmsSeckillFailure;
import com.mall.seckill.entity.OmsSeckillOrder;
import com.mall.seckill.entity.SmsSeckillActivity;
import com.mall.seckill.entity.SmsSeckillSku;
import com.mall.seckill.enums.SeckillActivityStatus;
import com.mall.seckill.mapper.OmsSeckillFailureMapper;
import com.mall.seckill.mapper.OmsSeckillOrderMapper;
import com.mall.seckill.mapper.SmsSeckillActivityMapper;
import com.mall.seckill.mapper.SmsSeckillSkuMapper;
import com.mall.seckill.mq.SeckillOrderMessage;
import com.mall.seckill.mq.SeckillOrderMessagePublisher;
import com.mall.seckill.redis.SeckillPendingReservation;
import com.mall.seckill.redis.SeckillRedisService;
import com.mall.seckill.service.SeckillReservationRecoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillReservationRecoveryServiceImpl
        implements SeckillReservationRecoveryService {

    private final SmsSeckillActivityMapper activityMapper;
    private final SmsSeckillSkuMapper seckillSkuMapper;
    private final OmsSeckillOrderMapper seckillOrderMapper;
    private final OmsSeckillFailureMapper failureMapper;
    private final SeckillRedisService seckillRedisService;
    private final SeckillOrderMessagePublisher messagePublisher;

    @Override
    public int recoverStaleReservations(
            long staleBeforeTimestamp,
            int skuBatchSize,
            int requestBatchSize) {

        List<Long> activeActivityIds =
                activityMapper.selectList(
                                new LambdaQueryWrapper
                                        <SmsSeckillActivity>()
                                        .eq(
                                                SmsSeckillActivity::getStatus,
                                                SeckillActivityStatus.ENABLED
                                                        .getCode()
                                        )
                        )
                        .stream()
                        .map(SmsSeckillActivity::getId)
                        .toList();

        if (activeActivityIds.isEmpty()) {
            return 0;
        }

        List<SmsSeckillSku> seckillSkus =
                seckillSkuMapper.selectPage(
                                new Page<>(1, skuBatchSize, false),
                                new LambdaQueryWrapper<SmsSeckillSku>()
                                        .in(
                                                SmsSeckillSku::getActivityId,
                                                activeActivityIds
                                        )
                                        .orderByAsc(
                                                SmsSeckillSku::getId
                                        )
                        )
                        .getRecords();

        int recovered = 0;

        for (SmsSeckillSku seckillSku : seckillSkus) {
            List<SeckillPendingReservation> pending =
                    seckillRedisService.findPendingBefore(
                            seckillSku.getId(),
                            staleBeforeTimestamp,
                            requestBatchSize
                    );

            for (SeckillPendingReservation reservation : pending) {
                if (hasOrder(reservation)) {
                    seckillRedisService.markCompleted(
                            reservation.seckillSkuId(),
                            reservation.requestId()
                    );
                    continue;
                }

                if (hasFailure(reservation)) {
                    continue;
                }

                try {
                    /*
                     * 先推进时间戳，进程在补投前再次崩溃时，
                     * 下一轮仍会在超时后继续恢复。
                     */
                    seckillRedisService.touchPending(
                            reservation.seckillSkuId(),
                            reservation.requestId(),
                            System.currentTimeMillis()
                    );

                    messagePublisher.publish(
                            new SeckillOrderMessage(
                                    reservation.requestId(),
                                    reservation.seckillSkuId(),
                                    reservation.memberId(),
                                    reservation.addressId(),
                                    reservation.quantity(),
                                    reservation.requestedAt()
                            )
                    );
                    recovered++;
                } catch (RuntimeException exception) {
                    log.warn(
                            "秒杀孤儿预扣补投失败，requestId={}",
                            reservation.requestId(),
                            exception
                    );
                }
            }
        }

        return recovered;
    }

    private boolean hasOrder(
            SeckillPendingReservation reservation) {
        Long count = seckillOrderMapper.selectCount(
                new LambdaQueryWrapper<OmsSeckillOrder>()
                        .eq(
                                OmsSeckillOrder::getRequestId,
                                reservation.requestId()
                        )
                        .eq(
                                OmsSeckillOrder::getSeckillSkuId,
                                reservation.seckillSkuId()
                        )
                        .eq(
                                OmsSeckillOrder::getMemberId,
                                reservation.memberId()
                        )
        );
        return count != null && count > 0;
    }

    private boolean hasFailure(
            SeckillPendingReservation reservation) {
        Long count = failureMapper.selectCount(
                new LambdaQueryWrapper<OmsSeckillFailure>()
                        .eq(
                                OmsSeckillFailure::getRequestId,
                                reservation.requestId()
                        )
                        .eq(
                                OmsSeckillFailure::getSeckillSkuId,
                                reservation.seckillSkuId()
                        )
                        .eq(
                                OmsSeckillFailure::getMemberId,
                                reservation.memberId()
                        )
        );
        return count != null && count > 0;
    }
}
