package com.mall.seckill.mq;

import com.mall.seckill.service.SeckillFailureCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillOrderMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    private final SeckillFailureCompensationService compensationService;

    public void publish(SeckillOrderMessage message) {

        CorrelationData correlationData =
                new CorrelationData(
                        message.requestId()
                );

        try {
            rabbitTemplate.convertAndSend(
                    SeckillRabbitConfig.ORDER_EXCHANGE,
                    SeckillRabbitConfig.ORDER_ROUTING_KEY,
                    message,
                    rabbitMessage -> {

                        rabbitMessage
                                .getMessageProperties()
                                .setDeliveryMode(
                                        MessageDeliveryMode.PERSISTENT
                                );

                        rabbitMessage
                                .getMessageProperties()
                                .setMessageId(
                                        message.requestId()
                                );

                        return rabbitMessage;
                    },
                    correlationData
            );
        } catch (RuntimeException exception) {

            rollback(
                    message,
                    "消息同步发送失败",
                    exception
            );

            throw exception;
        }

        correlationData.getFuture()
                .whenComplete((confirm, exception) -> {

                    ReturnedMessage returned =
                            correlationData.getReturned();

                    boolean failed =
                            exception != null
                                    || confirm == null
                                    || !confirm.isAck()
                                    || returned != null;

                    if (!failed) {
                        log.debug(
                                "秒杀订单消息发送成功，requestId={}",
                                message.requestId()
                        );

                        return;
                    }

                    String reason;

                    if (exception != null) {
                        reason = exception.getMessage();
                    } else if (returned != null) {
                        reason =
                                returned.getReplyCode()
                                        + ":"
                                        + returned.getReplyText();
                    } else if (confirm != null) {
                        reason = confirm.getReason();
                    } else {
                        reason = "未收到Publisher Confirm";
                    }

                    rollback(
                            message,
                            reason,
                            exception
                    );
                });
    }

    private void rollback(
            SeckillOrderMessage message,
            String reason,
            Throwable exception) {

        try {
            compensationService.recordAndCompensate(
                    message,
                    reason
            );

            log.warn(
                    "秒杀消息发送失败，已记录并执行补偿，"
                            + "requestId={}，seckillSkuId={}，原因={}",
                    message.requestId(),
                    message.seckillSkuId(),
                    reason,
                    exception
            );
        } catch (RuntimeException compensationException) {

            log.error(
                    "秒杀消息发送失败且补偿记录失败，"
                            + "requestId={}，seckillSkuId={}，原因={}",
                    message.requestId(),
                    message.seckillSkuId(),
                    reason,
                    compensationException
            );
        }
    }
}