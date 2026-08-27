package com.mall.seckill.mq;

import com.mall.seckill.service.SeckillOrderCreationService;
import com.mall.seckill.redis.SeckillRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderMessageListener {

    private final SeckillOrderCreationService
            orderCreationService;

    private final SeckillRedisService seckillRedisService;

    @RabbitListener(
            queues = SeckillRabbitConfig.ORDER_QUEUE
    )
    public void handle(
            SeckillOrderMessage message) {

        /*
         * 不在这里捕获异常：
         * 创建失败时交给Spring Rabbit重试，
         * 重试耗尽后进入死信队列。
         */
        Long orderId =
                orderCreationService.createOrder(
                        message
                );

        try {
            seckillRedisService.markCompleted(
                    message.seckillSkuId(),
                    message.requestId()
            );
        } catch (RuntimeException exception) {
            /*
             * 订单已经提交，不能因为Redis暂时不可用而把成功消息
             * 送入死信补偿；pending索引会由恢复任务按MySQL结果清理。
             */
            log.warn(
                    "秒杀订单已创建但pending索引清理失败，requestId={}",
                    message.requestId(),
                    exception
            );
        }

        log.info(
                "秒杀订单创建成功，"
                        + "requestId={}，orderId={}",
                message.requestId(),
                orderId
        );
    }
}
