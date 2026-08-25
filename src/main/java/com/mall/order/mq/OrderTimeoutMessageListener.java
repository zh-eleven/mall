package com.mall.order.mq;

import com.mall.order.config.OrderProperties;
import com.mall.order.service.OrderCancellationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutMessageListener {

    private final OrderCancellationService
            orderCancellationService;

    private final OrderProperties orderProperties;

    @RabbitListener(
            queues = RabbitOrderConfig.CANCEL_QUEUE
    )
    public void handle(OrderTimeoutMessage message) {

        LocalDateTime cutoffTime =
                LocalDateTime.now()
                        .minusMinutes(
                                orderProperties
                                        .getTimeoutMinutes()
                        );

        boolean canceled =
                orderCancellationService
                        .cancelTimedOutOrder(
                                message.orderId(),
                                cutoffTime
                        );

        if (canceled) {
            log.info(
                    "RabbitMQ取消超时订单成功: orderId={}",
                    message.orderId()
            );
        } else {
            /*
             * 已支付、已取消或已被定时任务处理，
             * 属于正常的幂等结果。
             */
            log.info(
                    "订单无需重复取消: orderId={}, expireTime={}",
                    message.orderId(),
                    message.expireTime()
            );
        }
    }
}