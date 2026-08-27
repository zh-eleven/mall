package com.mall.seckill.scheduler;

import com.mall.seckill.config.SeckillProperties;
import com.mall.seckill.service
        .SeckillFailureCompensationService;
import com.mall.seckill.service.SeckillActivityFinalizationService;
import com.mall.seckill.service.SeckillReservationRecoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillCompensationScheduler {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Shanghai");

    private final SeckillFailureCompensationService
            compensationService;

    private final SeckillProperties seckillProperties;

    private final SeckillReservationRecoveryService
            reservationRecoveryService;

    private final SeckillActivityFinalizationService
            activityFinalizationService;

    @Scheduled(
            fixedDelayString =
                    "${seckill.compensation-scan-delay:60000}",
            initialDelayString =
                    "${seckill.compensation-scan-delay:60000}"
    )
    public void retryPendingCompensations() {

        int compensated =
                compensationService.retryPending(
                        seckillProperties
                                .getCompensationBatchSize()
                );

        if (compensated > 0) {
            log.info(
                    "秒杀失败记录定时补偿完成，"
                            + "成功数量={}",
                    compensated
            );
        }

        int recovered = reservationRecoveryService
                .recoverStaleReservations(
                        System.currentTimeMillis()
                                - seckillProperties
                                .getReservationStaleMillis(),
                        seckillProperties
                                .getReservationSkuBatchSize(),
                        seckillProperties
                                .getReservationRequestBatchSize()
                );

        if (recovered > 0) {
            log.info(
                    "秒杀孤儿预扣恢复完成，补投数量={}",
                    recovered
            );
        }

        LocalDateTime now =
                LocalDateTime.now(BUSINESS_ZONE);

        LocalDateTime cutoff = now.minus(
                Duration.ofMillis(
                        seckillProperties
                                .getActivityCloseGraceMillis()
                )
        );

        for (Long activityId
                : activityFinalizationService
                .findExpiredActivityIds(
                        cutoff,
                        seckillProperties
                                .getActivityCloseBatchSize()
                )) {
            try {
                if (activityFinalizationService
                        .finalizeExpiredActivity(
                                activityId,
                                now
                        )) {
                    log.info(
                            "秒杀活动收尾完成，activityId={}",
                            activityId
                    );
                }
            } catch (RuntimeException exception) {
                log.error(
                        "秒杀活动收尾失败，activityId={}",
                        activityId,
                        exception
                );
            }
        }
    }
}
