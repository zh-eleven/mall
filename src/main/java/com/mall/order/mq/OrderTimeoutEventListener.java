package com.mall.order.mq;

import com.mall.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutEventListener {

    private final OrderTimeoutMessagePublisher
            messagePublisher;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(OrderCreatedEvent event) {
        try {
            messagePublisher.publish(
                    event.orderId(),
                    event.expireTime()
            );
        } catch (RuntimeException exception) {
            /*
             * 此时订单事务已经提交，不能让 MQ 故障
             * 导致接口显示下单失败。
             * 未发送成功的订单由定时扫描补偿。
             */
            log.error(
                    "订单已创建，但超时消息发送失败: orderId={}",
                    event.orderId(),
                    exception
            );
        }
    }
}