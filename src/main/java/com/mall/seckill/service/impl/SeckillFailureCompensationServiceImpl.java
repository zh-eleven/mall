package com.mall.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.seckill.entity.OmsSeckillFailure;
import com.mall.seckill.entity.OmsSeckillOrder;
import com.mall.seckill.mapper.OmsSeckillFailureMapper;
import com.mall.seckill.mapper.OmsSeckillOrderMapper;
import com.mall.seckill.mq.SeckillOrderMessage;
import com.mall.seckill.redis.SeckillRedisService;
import com.mall.seckill.service.SeckillFailureCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillFailureCompensationServiceImpl
        implements SeckillFailureCompensationService {

    private static final int PENDING = 0;
    private static final int COMPENSATED = 1;

    private final OmsSeckillFailureMapper failureMapper;

    private final OmsSeckillOrderMapper seckillOrderMapper;

    private final SeckillRedisService seckillRedisService;

    @Override
    public void recordAndCompensate(
            SeckillOrderMessage message,
            String failureReason) {

        OmsSeckillOrder existingOrder =
                findOrder(
                        message.requestId(),
                        message.seckillSkuId(),
                        message.memberId()
                );

        if (existingOrder != null) {
            seckillRedisService.markCompleted(
                    message.seckillSkuId(),
                    message.requestId()
            );
            return;
        }

        OmsSeckillFailure failure =
                findFailure(
                        message.requestId(),
                        message.seckillSkuId(),
                        message.memberId()
                );

        if (failure == null) {
            failure = new OmsSeckillFailure();

            failure.setRequestId(
                    message.requestId()
            );

            failure.setSeckillSkuId(
                    message.seckillSkuId()
            );

            failure.setMemberId(
                    message.memberId()
            );

            failure.setFailureReason(
                    normalizeReason(failureReason)
            );

            failure.setStatus(PENDING);
            failure.setRetryCount(0);

            try {
                int inserted =
                        failureMapper.insert(failure);

                if (inserted != 1) {
                    throw new BusinessException(
                            ErrorCode.DATA_CONFLICT
                    );
                }
            } catch (DuplicateKeyException exception) {

                /*
                 * 多个死信消费者并发处理同一请求时，
                 * 查询另一线程已经创建的记录。
                 */
                failure = findFailure(
                        message.requestId(),
                        message.seckillSkuId(),
                        message.memberId()
                );

                if (failure == null) {
                    throw exception;
                }
            }
        }

        compensate(failure.getId());
    }

    @Override
    public boolean compensate(Long failureId) {

        OmsSeckillFailure failure =
                failureMapper.selectById(failureId);

        if (failure == null) {
            return false;
        }

        if (Integer.valueOf(COMPENSATED)
                .equals(failure.getStatus())) {
            return true;
        }

        OmsSeckillOrder existingOrder =
                findOrder(
                        failure.getRequestId(),
                        failure.getSeckillSkuId(),
                        failure.getMemberId()
                );

        if (existingOrder != null) {
            try {
                seckillRedisService.markCompleted(
                        failure.getSeckillSkuId(),
                        failure.getRequestId()
                );
            } catch (RuntimeException exception) {
                recordAttempt(failure.getId());
                return false;
            }

            return markCompensated(failure);
        }

        boolean rolledBack;

        try {
            rolledBack =
                    seckillRedisService
                            .rollbackReservation(
                                    failure.getSeckillSkuId(),
                                    failure.getRequestId()
                            );
        } catch (RuntimeException exception) {

            log.error(
                    "秒杀Redis补偿异常，failureId={}，requestId={}",
                    failure.getId(),
                    failure.getRequestId(),
                    exception
            );

            recordAttempt(failure.getId());

            return false;
        }

        if (!rolledBack) {
            log.error(
                    "秒杀Redis补偿失败，failureId={}，requestId={}",
                    failure.getId(),
                    failure.getRequestId()
            );

            recordAttempt(failure.getId());

            return false;
        }

        return markCompensated(failure);
    }

    private boolean markCompensated(
            OmsSeckillFailure failure) {

        int updated = failureMapper.update(
                null,
                new LambdaUpdateWrapper<OmsSeckillFailure>()
                        .eq(
                                OmsSeckillFailure::getId,
                                failure.getId()
                        )
                        .eq(
                                OmsSeckillFailure::getStatus,
                                PENDING
                        )
                        .set(
                                OmsSeckillFailure::getStatus,
                                COMPENSATED
                        )
                        .set(
                                OmsSeckillFailure::getLastRetryTime,
                                LocalDateTime.now()
                        )
                        .setSql(
                                "retry_count = retry_count + 1"
                        )
        );

        /*
         * updated为0可能是另一个线程已经完成补偿。
         */
        if (updated == 0) {
            OmsSeckillFailure latest =
                    failureMapper.selectById(
                            failure.getId()
                    );

            return latest != null
                    && Integer.valueOf(COMPENSATED)
                    .equals(latest.getStatus());
        }

        log.warn(
                "秒杀失败请求补偿成功，failureId={}，requestId={}",
                failure.getId(),
                failure.getRequestId()
        );

        return true;
    }

    private void recordAttempt(Long failureId) {

        failureMapper.update(
                null,
                new LambdaUpdateWrapper<OmsSeckillFailure>()
                        .eq(
                                OmsSeckillFailure::getId,
                                failureId
                        )
                        .eq(
                                OmsSeckillFailure::getStatus,
                                PENDING
                        )
                        .set(
                                OmsSeckillFailure::getLastRetryTime,
                                LocalDateTime.now()
                        )
                        .setSql(
                                "retry_count = retry_count + 1"
                        )
        );
    }

    private OmsSeckillFailure findFailure(
            String requestId,
            Long seckillSkuId,
            Long memberId) {

        return failureMapper.selectOne(
                new LambdaQueryWrapper<OmsSeckillFailure>()
                        .eq(
                                OmsSeckillFailure::getRequestId,
                                requestId
                        )
                        .eq(
                                OmsSeckillFailure::getSeckillSkuId,
                                seckillSkuId
                        )
                        .eq(
                                OmsSeckillFailure::getMemberId,
                                memberId
                        )
                        .last("LIMIT 1")
        );
    }

    private OmsSeckillOrder findOrder(
            String requestId,
            Long seckillSkuId,
            Long memberId) {

        return seckillOrderMapper.selectOne(
                new LambdaQueryWrapper<OmsSeckillOrder>()
                        .eq(
                                OmsSeckillOrder::getRequestId,
                                requestId
                        )
                        .eq(
                                OmsSeckillOrder::getSeckillSkuId,
                                seckillSkuId
                        )
                        .eq(
                                OmsSeckillOrder::getMemberId,
                                memberId
                        )
                        .last("LIMIT 1")
        );
    }

    private String normalizeReason(String reason) {

        if (!StringUtils.hasText(reason)) {
            return "秒杀订单消息重试耗尽";
        }

        String normalized = reason.trim();

        return normalized.length() <= 1000
                ? normalized
                : normalized.substring(0, 1000);
    }

    @Override
    public int retryPending(int batchSize) {

        Page<OmsSeckillFailure> page =
                failureMapper.selectPage(
                        new Page<>(1, batchSize),
                        new LambdaQueryWrapper
                                <OmsSeckillFailure>()
                                .eq(
                                        OmsSeckillFailure::getStatus,
                                        PENDING
                                )
                                .orderByAsc(
                                        OmsSeckillFailure::getId
                                )
                );

        int compensatedCount = 0;

        for (OmsSeckillFailure failure
                : page.getRecords()) {

            try {
                if (compensate(failure.getId())) {
                    compensatedCount++;
                }
            } catch (RuntimeException exception) {

                /*
                 * 单条失败不能阻止后续记录继续补偿。
                 */
                log.error(
                        "定时补偿秒杀失败记录异常，"
                                + "failureId={}，requestId={}",
                        failure.getId(),
                        failure.getRequestId(),
                        exception
                );
            }
        }

        return compensatedCount;
    }
}
