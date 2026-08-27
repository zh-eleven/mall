package com.mall.seckill.mq;

import com.mall.seckill.service
        .SeckillFailureCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation
        .RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderDeadLetterListener {

    private final SeckillFailureCompensationService
            compensationService;

    @RabbitListener(
            queues = SeckillRabbitConfig.DEAD_QUEUE
    )
    public void handle(
            SeckillOrderMessage message) {

        log.error(
                "秒杀订单消息重试耗尽，开始补偿，"
                        + "requestId={}，seckillSkuId={}",
                message.requestId(),
                message.seckillSkuId()
        );

        /*
         * 先持久化失败记录，再回滚Redis。
         * 如果数据库写入失败，异常继续抛出；
         * 消息重试耗尽后会进入停车队列。
         */
        compensationService.recordAndCompensate(
                message,
                "RabbitMQ消费重试耗尽"
        );
    }
}