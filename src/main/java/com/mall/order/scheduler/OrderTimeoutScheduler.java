package com.mall.order.scheduler;

import com.mall.order.config.OrderProperties;
import com.mall.order.enums.OrderStatus;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.order.service.OrderCancellationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OmsOrderMapper orderMapper;

    private final OrderCancellationService
            orderCancellationService;

    private final OrderProperties orderProperties;

    @Scheduled(
            fixedDelayString =
                    "${order.timeout-scan-delay:60000}",
            initialDelayString =
                    "${order.timeout-scan-delay:60000}"
    )
    public void cancelTimedOutOrders() {

        LocalDateTime cutoffTime = LocalDateTime.now()
                .minusMinutes(
                        orderProperties.getTimeoutMinutes()
                );

        List<Long> orderIds =
                orderMapper.selectTimedOutPendingOrderIds(
                        OrderStatus.PENDING_PAYMENT.getCode(),
                        cutoffTime,
                        orderProperties.getTimeoutBatchSize()
                );

        for (Long orderId : orderIds) {
            try {
                orderCancellationService.cancelTimedOutOrder(
                        orderId,
                        cutoffTime
                );
            } catch (RuntimeException exception) {
                log.error(
                        "超时订单取消失败: orderId={}",
                        orderId,
                        exception
                );
            }
        }
    }
}
